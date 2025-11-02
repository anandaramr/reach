package com.project.reach.data.respository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.project.reach.data.local.IdentityManager
import com.project.reach.data.local.dao.ContactDao
import com.project.reach.data.local.dao.MessageDao
import com.project.reach.data.local.entity.ContactEntity
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.data.utils.TypingStateHandler
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.domain.models.Message
import com.project.reach.domain.models.MessageNotification
import com.project.reach.domain.models.MessagePreview
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.NotificationEvent
import com.project.reach.network.model.Packet
import com.project.reach.util.toUUID
import com.project.reach.util.truncate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class MessageRepository(
    private val messageDao: MessageDao,
    private val contactDao: ContactDao,
    private val wifiController: IWifiController,
    private val identityManager: IdentityManager
): IMessageRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _notifications =
        MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 8, replay = 0)
    override val notifications = _notifications.asSharedFlow()

    private val typingStateHandler = TypingStateHandler(scope)

    init {
        scope.launch {
            handlePackets()
        }

        scope.launch {
            wifiController.newDevices.collect { deviceInfo ->
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
        sendPendingMessageToUser(message.userId.toString(), message.messageId, message.text)
    }

    override suspend fun sendMessage(userId: String, text: String) {
        val messageId = messageDao.insertMessage(
            messageEntity = MessageEntity(
                text = text,
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
        messageId: Long,
        message: String
    ) {
        val successful = sendStream(userId, message)
        if (successful) {
            messageDao.updateMessageState(messageId, MessageState.SENT)
        }
    }

    private suspend fun receiveMessage(
        userId: String,
        username: String,
        message: String,
        timestamp: Long
    ) {
        saveNewContact(userId, username)
        messageDao.insertMessage(
            messageEntity = MessageEntity(
                text = message,
                userId = userId.toUUID(),
                isFromPeer = true,
                messageState = MessageState.RECEIVED,
                timeStamp = timestamp
            )
        )
    }

    // TODO: Paging
    @Deprecated(
        "getMessages fetches all messages from database at once which causes performance issues. Switch to paging for better performance",
        replaceWith = ReplaceWith("getMessagesPaged(userId)")
    )
    override fun getMessages(userId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessageByUser(userId.toUUID())
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
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { messageDao.getMessageByUserPaged(userId.toUUID()) }
        ).flow.map { pagingData ->
            pagingData.map { msg -> msg.toMessage() }
        }
    }

    @Deprecated("Switch to paging for better performance")
    override fun getMessagesPreview(): Flow<List<MessagePreview>> {
        return messageDao.getMessagesPreview()
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
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { messageDao.getMessagesPreviewPaged() }
        ).flow
    }

    override suspend fun saveNewContact(userId: String, username: String) {
        return contactDao.insertContact(
            contact = ContactEntity(
                userId = userId.toUUID(),
                username = username
            )
        )
    }

    override fun getUsername(userId: String): Flow<String> {
        userId in typingStateHandler.typingUsers.value
        return contactDao.getUsername(
            userId = userId.toUUID(),
        )
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
                wifiController.send(
                    userId.toUUID(),
                    Packet.Typing(identityManager.getUserUUID().toString())
                )
            }
        }
    }

    private suspend fun sendStream(userId: String, message: String): Boolean {
        return wifiController.sendStream(
            uuid = userId.toUUID(),
            packet = Packet.Message(
                userId = identityManager.getUserUUID().toString(),
                username = identityManager.getUsernameIdentity().toString(),
                message = message
            )
        )
    }

    private suspend fun handlePackets() {
        wifiController.packets.collect { packet ->
            when (packet) {
                is Packet.Message -> {
                    receiveMessage(
                        userId = packet.userId,
                        username = packet.username,
                        message = packet.message,
                        timestamp = packet.timeStamp
                    )
                    _notifications.emit(
                        NotificationEvent.Message(
                            userId = packet.userId,
                            username = packet.username,
                            messages = getUnreadMessagesFromUser(packet.userId),
                        )
                    )
                }

                is Packet.Typing -> {
                    typingStateHandler.setIsTyping(packet.userId)
                }

                is Packet.GoodBye -> {}
                is Packet.Heartbeat -> {}
                is Packet.Hello -> {}
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
            text = text.truncate(60),
            timeStamp = timeStamp
        )
    }

    private fun MessageEntity.toMessage(): Message {
        return Message(
            text = text,
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