package com.project.reach.data.respository

import com.project.reach.data.local.dao.ContactDao
import com.project.reach.data.local.dao.MessageDao
import com.project.reach.data.local.entity.ContactEntity
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.models.MessagePreview
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MessageRepository(
    private val messageDao: MessageDao,
    private val contactDao: ContactDao
): IMessageRepository {
    override suspend fun sendMessage(message: String, userId: String) {
        messageDao.insertMessage(
            messageEntity = MessageEntity(
                text = message,
                userId = UUID.fromString(userId),
                isFromPeer = false,
                messageState = MessageState.PENDING
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
}