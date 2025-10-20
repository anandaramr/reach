package com.project.reach.data.respository

import com.project.reach.data.local.dao.MessageDao
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.contracts.IMessageRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MessageRepository(
    private val messageDao: MessageDao
): IMessageRepository {
    override suspend fun sendMessage(message: String, userId: String) {
        messageDao.insertMessage(
            messageEntity = MessageEntity(
                text = message,
                peerId = UUID.fromString(userId),
                isFromPeer = false,
            )
        )
    }

    // TODO: Paging
    override fun getMessages(userId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessageByUser(UUID.fromString(userId))
    }
}