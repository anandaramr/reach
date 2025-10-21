package com.project.reach.domain.contracts

import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.models.MessagePreview
import kotlinx.coroutines.flow.Flow

interface IMessageRepository {
    suspend fun sendMessage(message: String, userId: String)

    fun getMessages(userId: String): Flow<List<MessageEntity>>

    fun getMessagesPreview(): Flow<List<MessagePreview>>

    suspend fun saveNewContact(userId: String, username: String)
}