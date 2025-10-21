package com.project.reach.domain.contracts

import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.models.MessagePreview
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for handling messaging-related operations.
 *
 * This interface defines methods for sending messages, retrieving messages,
 * fetching message previews, and managing contacts.
 */
interface IMessageRepository {

    /**
     * Sends message with content [message] to user with user ID [userId]
     */
    suspend fun sendMessage(userId: String, message: String)

    /**
     * Returns messages to and from user with user ID [userId]
     */
    fun getMessages(userId: String): Flow<List<MessageEntity>>

    /**
     * Returns preview of chats
     *
     * Contains a list of users and the last message sent to them
     */
    fun getMessagesPreview(): Flow<List<MessagePreview>>

    /**
     * Saves contact with credentials [userId] [username]
     *
     * Users need to be saved to contacts before any communication
     * can be performed
     */
    suspend fun saveNewContact(userId: String, username: String)
}