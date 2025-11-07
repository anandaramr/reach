package com.project.reach.data.respository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.project.reach.data.local.IdentityManager
import com.project.reach.data.local.dao.MessageDao
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.data.utils.TypingStateHandler
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.INetworkController
import com.project.reach.domain.models.Message
import com.project.reach.domain.models.MessageNotification
import com.project.reach.domain.models.MessagePreview
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.MessageType
import com.project.reach.domain.models.NotificationEvent
import com.project.reach.network.model.Packet
import com.project.reach.util.toUUID
import com.project.reach.util.truncate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class MessageRepository(
    private val messageDao: MessageDao,
    private val contactRepository: IContactRepository,
    private val networkController: INetworkController,
    identityManager: IdentityManager
): IMessageRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _notifications =
        MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 8, replay = 0)
    override val notifications = _notifications.asSharedFlow()

    private val typingStateHandler = TypingStateHandler(scope)

    private val myUserId = identityManager.userId
    private val myUsername = identityManager.username

    init {
        scope.launch {
            handlePackets()
        }

        scope.launch {
            networkController.newDevices.collect { deviceInfo ->
                launch {
                    retryPendingMessages(deviceInfo.uuid)
                }
            }
        }
    }

    private suspend fun retryPendingMessages(uuid: UUID) {
        val pendingMessages = messageDao.getPendingMessagesById(uuid).first()
        pendingMessages.forEach { message ->
            retryMessage(message)
        }
    }

    private suspend fun retryMessage(message: MessageEntity) {
        sendPendingMessageToUser(message.userId.toString(), message.messageId, message.data)
    }

    override suspend fun sendMessage(userId: String, text: String) {
        val messageId = UUID.randomUUID()
        messageDao.insertMessage(
            messageEntity = MessageEntity(
                messageId = messageId,
                messageType = MessageType.TEXT,
                data = text,
                userId = userId.toUUID(),
                isFromPeer = false,
                messageState = MessageState.PENDING
            )
        )

        scope.launch {
            sendPendingMessageToUser(userId, messageId, text)
        }
    }

    private suspend fun sendPendingMessageToUser(
        userId: String,
        messageId: UUID,
        message: String
    ) {
        // reset self typing state so that throttle works as intended
        typingStateHandler.resetSelfIsTyping()

        val successful = networkController.sendPacket(
            userId = userId.toUUID(),
            Packet.Message(
                senderId = myUserId,
                senderUsername = myUsername.value,
                message = message,
                messageId = messageId.toString()
            )
        )
        if (successful) {
            messageDao.updateMessageState(messageId, MessageState.SENT)
        }
    }

    private suspend fun receiveMessage(
        userId: String,
        username: String,
        messageId: String,
        message: String,
        timestamp: Long
    ) {
        contactRepository.addToContacts(userId, username)
        messageDao.insertMessage(
            messageEntity = MessageEntity(
                messageId = messageId.toUUID(),
                data = message,
                messageType = MessageType.TEXT,
                userId = userId.toUUID(),
                isFromPeer = true,
                messageState = MessageState.RECEIVED,
                timeStamp = timestamp
            )
        )
    }

    override fun getMessagesPaged(
        userId: String,
        pageSize: Int,
        initialLoadSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Message>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = initialLoadSize,
                pageSize = pageSize,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = { messageDao.getMessageByUserPaged(userId.toUUID()) }
        ).flow.map { pagingData ->
            pagingData.map { msg -> msg.toMessage() }
        }
    }

    override fun getMessagePreviewsPaged(
        pageSize: Int,
        initialLoadSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<MessagePreview>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = initialLoadSize,
                pageSize = pageSize,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = { messageDao.getMessagesPreviewPaged() }
        ).flow
    }

    @Deprecated("Use updateContact from IContactRepository instead")
    override suspend fun saveNewContact(userId: String, username: String) {
        return contactRepository.addToContacts(userId, username)
    }

    @Deprecated("Use updateContact from IContactRepository instead")
    override fun getUsername(userId: String): Flow<String> {
        return contactRepository.getUsername(userId)
    }

    override suspend fun onReadMessage(messageId: String) {
        // TODO
    }

    override val typingUsers = typingStateHandler.typingUsers

    override fun isTyping(userId: String): Flow<Boolean> {
        return typingStateHandler.getIsTyping(userId)
    }

    override fun emitTyping(userId: String) {
        typingStateHandler.throttledSend {
            scope.launch {
                networkController.sendPacket(
                    userId = userId.toUUID(),
                    Packet.Typing(myUserId)
                )
            }
        }
    }

    private suspend fun handlePackets() {
        networkController.packets.collect { packet ->
            when (packet) {
                is Packet.Message -> {
                    receiveMessage(
                        userId = packet.senderId,
                        username = packet.senderUsername,
                        messageId = packet.messageId,
                        message = packet.message,
                        timestamp = packet.timeStamp
                    )
                    _notifications.emit(
                        NotificationEvent.Message(
                            userId = packet.senderId,
                            username = packet.senderUsername,
                            messages = getUnreadMessagesFromUser(packet.senderId),
                        )
                    )
                    // stop the typing indicator
                    typingStateHandler.resetPeerIsTyping(packet.senderId)
                }

                is Packet.Typing -> {
                    typingStateHandler.setIsTyping(packet.senderId)
                }

                is Packet.Heartbeat -> {
                    contactRepository.updateContactIfItExists(packet.senderId, packet.senderUsername)
                }

                is Packet.Hello -> {
                    contactRepository.updateContactIfItExists(packet.senderId, packet.senderUsername)
                }

                is Packet.GoodBye -> {}
            }
        }
    }

    private suspend fun getUnreadMessagesFromUser(userId: String): List<MessageNotification> {
        return messageDao
            .getUnreadMessagesById(userId.toUUID())
            .first()
            .takeLast(NUM_MESSAGES_IN_NOTIFICATION)
            .map { entity -> entity.toMessageNotification() }
    }

    private fun MessageEntity.toMessageNotification(): MessageNotification {
        return MessageNotification(
            text = data.truncate(60),
            timeStamp = timeStamp
        )
    }

    private fun MessageEntity.toMessage(): Message {
        return Message(
            messageId = messageId.toString(),
            text = data,
            messageType = messageType,
            metadata = metadata,
            isFromSelf = !isFromPeer,
            userId = userId.toString(),
            messageState = messageState,
            timeStamp = timeStamp
        )
    }

    companion object {
        // represents the number of last unread messages to be shown in notification
        private const val NUM_MESSAGES_IN_NOTIFICATION = 6
    }
}