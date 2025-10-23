package com.project.reach.data.respository

import com.project.reach.data.local.IdentityManager
import com.project.reach.data.local.dao.ContactDao
import com.project.reach.data.local.dao.MessageDao
import com.project.reach.data.local.entity.ContactEntity
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.domain.models.MessagePreview
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.NotificationEvent
import com.project.reach.network.model.Packet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    init {
        scope.launch {
            handlePackets()
        }
    }

    override suspend fun sendMessage(userId: String, message: String) {
        messageDao.insertMessage(
            messageEntity = MessageEntity(
                text = message,
                userId = UUID.fromString(userId),
                isFromPeer = false,
                messageState = MessageState.PENDING
            )
        )

        sendMessageToUser(userId, message)
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
                userId = UUID.fromString(userId),
                isFromPeer = true,
                messageState = MessageState.RECEIVED,
                timeStamp = timestamp
            )
        )
    }

    // TODO: Paging
    override fun getMessages(userId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessageByUser(UUID.fromString(userId))
    }

    override fun getMessagesPreview(): Flow<List<MessagePreview>> {
        return messageDao.getMessagesPreview()
    }

    override suspend fun saveNewContact(userId: String, username: String) {
        return contactDao.insertContact(
            contact = ContactEntity(
                userId = UUID.fromString(userId),
                username = username
            )
        )
    }

    override fun getUsername(userId: String): Flow<String> {
        return contactDao.getUsername(
            userId = UUID.fromString(userId),
        )
    }

    private fun sendMessageToUser(userId: String, message: String): Boolean {
        return wifiController.send(
            uuid = UUID.fromString(userId),
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
                            message = packet.message,
                            timeStamp = packet.timeStamp
                        )
                    )
                }

                is Packet.Typing -> {}
            }
        }
    }
}