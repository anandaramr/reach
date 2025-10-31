package com.project.reach.domain.contracts

import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.models.MessagePreview
import com.project.reach.domain.models.NotificationEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Repository interface for handling messaging-related operations.
 *
 * This interface defines methods for sending messages, retrieving messages,
 * fetching message previews, and managing contacts.
 */
interface IMessageRepository {

    /**
     * Emits [NotificationEvent] which should be shown to the user
     */
    val notifications: SharedFlow<NotificationEvent>

    /**
     * Sends message with content [text] to user with user ID [userId]
     */
    suspend fun sendMessage(userId: String, text: String)

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

    /**
     * Get username for user using UserId
     */
    fun getUsername(userId: String): Flow<String>

    /**
     * Mark message as read
     *
     * @param messageId The ID corresponding to the read message
     */
    suspend fun onReadMessage(messageId: String)

    /**
     * Observes the typing state of a specific user in the current chat.
     *
     * Emits `true` when the user starts typing and automatically reverts to `false`
     * after a period of inactivity. The timeout resets each time a new typing
     * indicator is received from the user.
     *
     * Usage:
     * ```
     *  viewModelScope.launch {
     *      isTyping(userId).collect { isPeerTyping ->
     *          if (isPeerTyping) showTypingUI()
     *      }
     *  }
     * ```
     *
     * @param userId The user ID of the user whose typing state to observe
     * @return A [Flow] that emits the current typing state of the
     * specified user. Emits `true` when typing, `false` when not typing or after timeout.
     *
     * @see emitTyping For sending typing indicators to a peer
     */
    fun isTyping(userId: String): Flow<Boolean>

    /**
     * Sends a typing indicator to notify other users that the current user is typing.
     *
     * This function is automatically throttled to prevent excessive network requests.
     * Subsequent calls within the throttle window will be ignored, as typing indicators
     * are automatically cleared on the receiving end after a timeout period.
     *
     * @param userId The user ID of the current user who is typing
     *
     * @see isTyping For observing typing state of other users
     */
    fun emitTyping(userId: String)
}