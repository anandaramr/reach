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
import com.project.reach.util.debug
import com.project.reach.util.toUUID
import com.project.reach.util.truncate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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

    private val foundDevices = networkController.foundDevices
        .map { devices -> devices.map { it.uuid }.toSet() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    init {
        scope.launch {
            handlePackets()
        }

        scope.launch {
            startMessageDispatcher()
        }
    }

    // Message dispatcher keeps track of new messages and pending ones
    // and dispatches them if the recipient is discoverable
    private suspend fun startMessageDispatcher() {
        val pendingFlow = messageDao.getUserIdsOfPendingMessages()

        combine(foundDevices, pendingFlow) { onlineUsers, usersWithPendingMessages ->
            onlineUsers.intersect(usersWithPendingMessages)
        }.distinctUntilChanged()
            .collect { userIds ->
                userIds.forEach { userId ->
                    dispatchPendingMessages(userId)
                }
            }
    }

    private val sendLocks = ConcurrentHashMap<UUID, Mutex>()
    private suspend fun dispatchPendingMessages(userId: UUID) {
        val sendLock = sendLocks.computeIfAbsent(userId) { Mutex() }

        sendLock.withLock {
            if (userId !in foundDevices.value) return

            val pendingMessages = messageDao.getPendingMessagesById(userId).first()
            pendingMessages.forEach { message ->
                dispatchMessage(message)
            }
        }
    }

    private suspend fun dispatchMessage(message: MessageEntity) {
        sendTextMessageToUser(message)
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
        // Message dispatcher handles sending the message
    }

    private suspend fun sendTextMessageToUser(
        message: MessageEntity
    ) {
        // reset self typing state so that throttling works as intended
        typingStateHandler.resetSelfIsTyping()

        val successful = networkController.sendPacket(
            userId = message.userId,
            Packet.Message(
                senderId = myUserId,
                senderUsername = myUsername.value,
                message = message.data,
                messageId = message.messageId.toString(),
                timeStamp = message.timeStamp
            )
        )
        if (successful) {
            messageDao.updateMessageState(message.messageId, MessageState.SENT)
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
                    contactRepository.updateContactIfItExists(
                        packet.senderId,
                        packet.senderUsername
                    )
                }

                is Packet.Hello -> {
                    contactRepository.updateContactIfItExists(
                        packet.senderId,
                        packet.senderUsername
                    )
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