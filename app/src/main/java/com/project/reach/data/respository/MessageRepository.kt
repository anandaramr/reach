package com.project.reach.data.respository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.project.reach.data.local.IdentityManager
import com.project.reach.data.local.dao.MediaDao
import com.project.reach.data.local.dao.MessageDao
import com.project.reach.data.local.entity.MediaEntity
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.data.model.MessageWithMedia
import com.project.reach.data.utils.PrivateFile
import com.project.reach.data.utils.TypingStateHandler
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.domain.contracts.IFileRepository
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
import kotlinx.coroutines.coroutineScope
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
    private val mediaDao: MediaDao,
    private val contactRepository: IContactRepository,
    private val networkController: INetworkController,
    private val fileRepository: IFileRepository,
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
    private suspend fun startMessageDispatcher() = coroutineScope {
        val pendingFlow = messageDao.getUserIdsOfPendingMessages()

        combine(foundDevices, pendingFlow) { onlineUsers, usersWithPendingMessages ->
            onlineUsers.intersect(usersWithPendingMessages)
        }.distinctUntilChanged()
            .collect { userIds ->
                userIds.forEach { userId ->
                    launch { dispatchPendingMessages(userId) }
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

    private suspend fun dispatchMessage(message: MessageWithMedia) {
        sendMessageToUser(message)
    }

    override suspend fun sendMessage(userId: String, text: String, file: PrivateFile?) {
        saveOutgoingMessage(userId, text, file)
        // Message dispatcher handles sending the message
    }

    private suspend fun saveOutgoingMessage(userId: String, text: String, file: PrivateFile?) {
        val messageId = UUID.randomUUID()
        val timeStamp = System.currentTimeMillis()

        if (file == null) {
            persistTextMessage(
                userId = userId,
                text = text,
                messageId = messageId,
                timeStamp = timeStamp,
                isFromPeer = false,
                messageState = MessageState.PENDING
            )
        } else {
            persistMessageWithMedia(
                userId = userId,
                caption = text,
                privateUri = file.location,
                isFromPeer = false,
                messageId = messageId,
                mediaId = file.hash,
                mimeType = file.mimeType,
                fileSize = file.file.length(),
                filename = file.filename,
                messageState = MessageState.PENDING
            )
        }
    }

    private suspend fun persistTextMessage(
        userId: String,
        text: String,
        messageId: UUID,
        timeStamp: Long,
        isFromPeer: Boolean,
        messageState: MessageState
    ) {
        messageDao.insertMessage(
            messageEntity = MessageEntity(
                messageId = messageId,
                mediaId = null,
                content = text,
                userId = userId.toUUID(),
                isFromPeer = isFromPeer,
                messageState = messageState,
                messageType = MessageType.TEXT,
                timeStamp = timeStamp
            )
        )
    }

    private suspend fun persistMessageWithMedia(
        userId: String,
        caption: String,
        privateUri: String,
        messageId: UUID,
        mediaId: String,
        isFromPeer: Boolean,
        mimeType: String,
        fileSize: Long,
        filename: String,
        messageState: MessageState
    ) {
        messageDao.insertMessageWithMedia(
            messageEntity = MessageEntity(
                messageId = messageId,
                mediaId = mediaId,
                content = caption,
                userId = userId.toUUID(),
                isFromPeer = isFromPeer,
                messageState = messageState,
                messageType = MessageType.FILE
            ),
            mediaEntity = MediaEntity(
                mediaId = mediaId,
                uri = privateUri,
                mimeType = mimeType,
                size = fileSize,
                filename = filename
            )
        )
    }

    private suspend fun sendMessageToUser(
        chat: MessageWithMedia
    ) {
        // reset typing state timer so that throttling works as intended
        typingStateHandler.resetSelfIsTyping()

        val message = chat.messageEntity
        val media = chat.mediaEntity
        val result = networkController.sendPacket(
            userId = message.userId,
            Packet.Message(
                senderId = myUserId,
                senderUsername = myUsername.value,
                text = message.content,
                messageId = message.messageId.toString(),
                timeStamp = message.timeStamp,
                media = media?.let {
                    Packet.FileMetadata(
                        fileHash = it.mediaId,
                        filename = it.filename,
                        mimeType = it.mimeType,
                        fileSize = it.size
                    )
                },
            )
        )

        // only mark as sent if the message has no attachments
        // if there are attachments it should be marked as sent only after file has been sent
        if (result && media == null) {
            messageDao.updateMessageState(message.messageId, MessageState.SENT)
        }
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
            pagingSourceFactory = { messageDao.getMessagesByUserPaged(userId.toUUID()) }
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
                is Packet.Message -> handleMessagePacket(packet)
                is Packet.Typing -> typingStateHandler.setIsTyping(packet.senderId)

                is Packet.Heartbeat -> {
                    contactRepository.updateContactIfItExists(
                        userId = packet.senderId,
                        username = packet.senderUsername
                    )
                }

                is Packet.Hello -> {
                    contactRepository.updateContactIfItExists(
                        userId = packet.senderId,
                        username = packet.senderUsername
                    )
                }

                is Packet.GoodBye -> {}
                is Packet.FileAccept -> handleFileAccept(packet)
            }
        }
    }

    private suspend fun handleMessagePacket(packet: Packet.Message) {
        contactRepository.addToContacts(packet.senderId, packet.senderUsername)
        saveIncomingMessage(packet)

        // receive media if attachment exists
        var showNotification = true
        if (packet.media != null) {
            showNotification = handleIncomingFile(packet)
        }

        // stop typing indicator
        typingStateHandler.resetPeerIsTyping(packet.senderId)
        if (showNotification) dispatchNewMessageNotification(packet)
    }

    private suspend fun saveIncomingMessage(packet: Packet.Message) {
        if (packet.media == null) {
            persistTextMessage(
                userId = packet.senderId,
                text = packet.text,
                messageId = packet.messageId.toUUID(),
                timeStamp = packet.timeStamp,
                isFromPeer = true,
                messageState = MessageState.RECEIVED
            )
        } else {
            persistMessageWithMedia(
                userId = packet.senderId,
                caption = packet.text,
                privateUri = fileRepository.getDownloadLocation(packet.media.fileHash),
                messageId = packet.messageId.toUUID(),
                mediaId = packet.media.fileHash,
                isFromPeer = true,
                mimeType = packet.media.mimeType,
                fileSize = packet.media.fileSize,
                filename = packet.media.filename,
                messageState = MessageState.RECEIVING
            )
        }
    }

    private suspend fun dispatchNewMessageNotification(packet: Packet.Message) {
        _notifications.emit(
            NotificationEvent.Message(
                userId = packet.senderId,
                username = packet.senderUsername,
                messages = getUnreadMessagesFromUser(packet.senderId),
            )
        )
    }

    private suspend fun handleIncomingFile(packet: Packet.Message): Boolean {
        if (packet.media == null) {
            debug("Couldn't accept file: Missing file metadata")
            return false
        }

        var result = false
        fileRepository.useFileOutputStream(packet.media.fileHash) { outputStream ->
            result = networkController.acceptFile(
                peerId = packet.senderId.toUUID(),
                fileId = packet.media.fileHash,
                outputStream = outputStream,
                size = packet.media.fileSize,
                onProgress = { progress ->
                    updateTransferProgress(packet.media.fileHash, progress)
                }
            )
        }

        if (result) {
            messageDao.updateMessageState(packet.messageId.toUUID(), MessageState.RECEIVED)
        }
        fileRepository.markAsNotInProgress(packet.media.fileHash)
        return result
    }

    private fun updateTransferProgress(fileHash: String, progress: Long) {
        scope.launch {
            fileRepository.updateFileTransferProgress(
                fileHash = fileHash,
                bytesRead = progress
            )
        }
    }

    private suspend fun handleFileAccept(packet: Packet.FileAccept) {
        val file = mediaDao.getFileByHash(packet.fileHash)

        var result = false
        fileRepository.useFileInputStream(file.uri) { inputStream ->
            result = networkController.sendFile(
                peerId = packet.senderId.toUUID(),
                inputStream = inputStream,
                size = file.size,
                fileAccept = packet,
                onProgress = { progress ->
                    updateTransferProgress(packet.fileHash, progress)
                }
            )
        }

        if (result) {
            messageDao.completeFileTransfer(packet.senderId.toUUID(), packet.fileHash)
        }
        fileRepository.markAsNotInProgress(packet.fileHash)
    }

    private suspend fun getUnreadMessagesFromUser(userId: String): List<MessageNotification> {
        return messageDao
            .getUnreadMessagesById(userId.toUUID(), NUM_MESSAGES_IN_NOTIFICATION)
            .first()
            .map { entity -> entity.toMessageNotification() }
    }

    private fun MessageEntity.toMessageNotification(): MessageNotification {
        return MessageNotification(
            text = content.ifBlank { "file" }.truncate(60),
            timeStamp = timeStamp
        )
    }

    private fun MessageWithMedia.toMessage(): Message {
        val messageId = messageEntity.messageId.toString()
        val text = messageEntity.content
        val isFromSelf = !messageEntity.isFromPeer
        val messageState = messageEntity.messageState
        val timeStamp = messageEntity.timeStamp

        if (mediaEntity == null) {
            return Message.TextMessage(
                messageId = messageId,
                text = text,
                isFromSelf = isFromSelf,
                messageState = messageState,
                timeStamp = timeStamp
            )
        }

        val fileHash = mediaEntity.mediaId
        val size = mediaEntity.size
        val mimeType = mediaEntity.mimeType

        return Message.FileMessage(
            messageId = messageId,
            text = text,
            isFromSelf = isFromSelf,
            timeStamp = timeStamp,
            messageState = messageState,
            fileHash = fileHash,
            filename = mediaEntity.filename,
            size = size,
            mimeType = mimeType,
            contentUri = fileRepository.getContentUri(mediaEntity.uri)
        )
    }

    companion object {
        // represents the number of last unread messages to be shown in notification
        private const val NUM_MESSAGES_IN_NOTIFICATION = 6
    }
}