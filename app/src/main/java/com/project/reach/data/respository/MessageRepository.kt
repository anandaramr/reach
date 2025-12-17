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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

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

    init {
        scope.launch {
            handlePackets()
        }

        scope.launch {
            messageRetryHandler()
        }

        scope.launch {
            startMessageDispatcher()
        }
    }

    private suspend fun messageRetryHandler(): Unit = coroutineScope {
        networkController.newDevices.collect { device ->
            messageDao.resetOutgoingPausedMessages(device.uuid)
        }
    }

    private val activeDispatchJobs = ConcurrentHashMap<UUID, Job>()

    private suspend fun startMessageDispatcher() {
        messageDao.getUserIdsOfPendingMessages()
            .distinctUntilChanged()
            .collect { userIds ->
                userIds.forEach { userId ->
                    val job = activeDispatchJobs[userId]
                    if (job != null && job.isActive) return@forEach
                    activeDispatchJobs[userId] = scope.launch { handlePendingMessages(userId) }
                }

                val userIdSet = userIds.toSet()
                activeDispatchJobs.keys.toList().forEach { user ->
                    if (user !in userIdSet) {
                        val job = activeDispatchJobs.remove(user)
                        job?.cancel()
                    }
                }
            }
    }

    private suspend fun handlePendingMessages(userId: UUID) {
        messageDao.getNextPendingMessage(userId)
            .filterNotNull()
            .distinctUntilChanged()
            .collect { message ->
                val result = dispatchMessage(message)
                if (!result) {
                    return@collect
                }
            }
    }

    private suspend fun dispatchMessage(message: MessageWithMedia): Boolean {
        return sendMessageToUser(message)
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
                text = text.trim(),
                messageId = messageId,
                timeStamp = timeStamp,
                isFromPeer = false,
                messageState = MessageState.PENDING
            )
        } else {
            persistMessageWithMedia(
                userId = userId,
                caption = text.trim(),
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
    ): Boolean {
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

        if (!result) {
            messageDao.updateMessageState(message.messageId, MessageState.PAUSED)
        } else if (media == null) {
            // only mark as sent if the message has no attachments
            // if there are attachments its message status should be
            // updated only after the file has been sent through wire
            messageDao.updateMessageState(message.messageId, MessageState.DELIVERED)
        }
        return result
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

                is Packet.FileComplete -> {
                    messageDao.updateOutgoingFileState(
                        senderId = packet.senderId.toUUID(),
                        mediaId = packet.fileHash,
                        messageState = MessageState.DELIVERED
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
                messageState = MessageState.DELIVERED
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
                messageState = MessageState.PENDING
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

        if (fileRepository.isTransferOngoing(packet.media.fileHash)) {
            debug("Transfer already ongoing")
            return false
        }

        val fileSizeInStorage = fileRepository.getFileSize(packet.media.fileHash)
        if (fileSizeInStorage == packet.media.fileSize) {
            val fileComplete = Packet.FileComplete(
                senderId = myUserId,
                fileHash = packet.media.fileHash
            )
            networkController.sendPacket(packet.senderId.toUUID(), fileComplete)
            messageDao.updateMessageState(packet.messageId.toUUID(), MessageState.DELIVERED)
            return true
        }

        messageDao.updateIncomingFileState(packet.media.fileHash, MessageState.PENDING)

        var isTransferSuccessful = false
        val safeOffset = getSafeOffset(fileSizeInStorage)
        fileRepository.useFileOutputStream(packet.media.fileHash, safeOffset) { outputStream ->
            isTransferSuccessful = networkController.acceptFile(
                peerId = packet.senderId.toUUID(),
                fileId = packet.media.fileHash,
                outputStream = outputStream,
                fileSize = packet.media.fileSize,
                offset = safeOffset
            ) { progress ->
                updateTransferProgress(packet.media.fileHash, progress)
            }
        }

        val messageState = if (isTransferSuccessful) MessageState.DELIVERED else MessageState.PAUSED
        messageDao.updateIncomingFileState(packet.media.fileHash, messageState)
        fileRepository.markAsNotInProgress(packet.media.fileHash)
        return isTransferSuccessful
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

        if (!messageDao.validateFileRequest(packet.senderId.toUUID(), packet.fileHash)) {
            debug("Unauthorized file request")
            return
        }

        messageDao.updateOutgoingFileState(
            senderId = packet.senderId.toUUID(),
            mediaId = packet.fileHash,
            messageState = MessageState.PENDING
        )
        var isTransferSuccessful = false
        fileRepository.useFileInputStream(file.mediaId, packet.offset) { inputStream ->
            isTransferSuccessful = networkController.sendFile(
                peerId = packet.senderId.toUUID(),
                inputStream = inputStream,
                bytesToSend = file.size - packet.offset,
                fileAccept = packet,
                onProgress = { progress ->
                    updateTransferProgress(packet.fileHash, progress)
                }
            )
        }

        val messageState = if (isTransferSuccessful) MessageState.DELIVERED else MessageState.PAUSED
        messageDao.updateOutgoingFileState(packet.senderId.toUUID(), packet.fileHash, messageState)
        fileRepository.markAsNotInProgress(packet.fileHash)
    }

    private suspend fun getUnreadMessagesFromUser(userId: String): List<MessageNotification> {
        return messageDao
            .getUnreadMessagesById(userId.toUUID(), NUM_MESSAGES_IN_NOTIFICATION)
            .first()
            .map { entity -> entity.toMessageNotification() }
            .reversed()
    }

    private fun MessageEntity.toMessageNotification(): MessageNotification {
        return MessageNotification(
            text = content.ifBlank { "${if (isFromPeer) "sent a" else "You sent a"} file" }.truncate(60),
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
            relativePath = mediaEntity.uri
        )
    }

    private fun getSafeOffset(fileSize: Long): Long {
        return max(0, fileSize - SAFETY_BLOCK)
    }

    companion object {
        // represents the number of last unread messages to be shown in notification
        private const val NUM_MESSAGES_IN_NOTIFICATION = 6

        // safe offset to resume file transfer
        const val SAFETY_BLOCK = 256 * 1024
    }
}