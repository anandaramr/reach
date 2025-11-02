package com.project.reach.domain.contracts

import androidx.paging.PagingData
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.models.Message
import com.project.reach.domain.models.MessagePreview
import com.project.reach.domain.models.NotificationEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

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
    @Deprecated(message = "Switch to paging for better performance")
    fun getMessages(userId: String): Flow<List<MessageEntity>>

    /**
     * Returns a paginated flow of messages for a specific user, ordered in
     * descending order of timestamp.
     *
     * Messages are loaded incrementally as the user scrolls, improving performance for large
     * message histories. The returned flow observes database changes and automatically updates
     * when messages are inserted, updated, or deleted
     *
     * Note: The returned flow of [PagingData] should be cached in the `viewModelScope` and
     * it should be collected in a composable using `collectAsLazyPagingItems()`
     *
     * The optional arguments should only be modified if necessary. Reducing certain values
     * can cause unexpected behaviour like flickers or jumps
     *
     * Example usage in ViewModel:
     * ```
     * val messages = repository
     *      .getMessagesPaged(userId)
     *      .cachedIn(viewModelScope)
     * ```
     *
     * Example usage in Compose UI:
     * ```
     * val messages = viewModel.messages.collectAsLazyPagingItems()
     * // ...
     * items(messages.itemCount) { idx ->
     *     messages[idx]?.let { message ->
     *         // display message data
     *     }
     * }
     * ```
     *
     * @param userId The unique identifier of the user whose messages to retrieve
     * @param pageSize Number of messages to load per page when scrolling. Default is 20
     * @param initialLoadSize Number of messages to load on initial fetch. Should be larger than
     * pageSize to fill the screen
     * @param prefetchDistance Number of items from the end of loaded content that triggers
     * loading the next page. Default is 5
     *
     * @return A [Flow] of [PagingData] containing [Message] objects
     *
     * @see Message
     */
    fun getMessagesPaged(
        userId: String,
        pageSize: Int = 20,
        initialLoadSize: Int = 60,
        prefetchDistance: Int = 5
    ): Flow<PagingData<Message>>

    /**
     * Returns preview of chats
     *
     * Contains a list of users and the last message sent to them
     */
    @Deprecated(message = "Switch to paging for better performance")
    fun getMessagesPreview(): Flow<List<MessagePreview>>

    /**
     * Returns a paginated flow of message previews (last message for each conversation),
     * ordered in descending order of timestamp.
     *
     * Message previews are loaded incrementally as the user scrolls, improving performance
     * when there are many conversations. The returned flow observes database changes and
     * automatically updates when messages are inserted, updated, or deleted in any conversation.
     *
     * Note: The returned flow of [PagingData] should be cached in the `viewModelScope` and
     * it should be collected in a composable using `collectAsLazyPagingItems()`
     *
     * The optional arguments should only be modified if necessary. Reducing certain values
     * can cause unexpected behaviour like flickers or jumps
     *
     * Example usage in ViewModel:
     * ```
     * val messagePreviews = repository
     *      .getMessagePreviewsPaged()
     *      .cachedIn(viewModelScope)
     * ```
     *
     * Example usage in Compose UI:
     * ```
     * val previews = viewModel.messagePreviews.collectAsLazyPagingItems()
     * // ...
     * items(previews.itemCount) { idx ->
     *     previews[idx]?.let { preview ->
     *         // display conversation preview
     *     }
     * }
     * ```
     *
     * @param pageSize Number of conversation previews to load per page when scrolling.
     * Default is 20
     * @param initialLoadSize Number of previews to load on initial fetch. Defaults to pageSize
     * to fill a typical conversation list screen
     * @param prefetchDistance Number of items from the end of loaded content that triggers
     * loading the next page. Default is 5
     *
     * @return A [Flow] of [PagingData] containing [MessagePreview] objects
     */
    fun getMessagePreviewsPaged(
        pageSize: Int = 20,
        initialLoadSize: Int = pageSize,
        prefetchDistance: Int = 5
    ): Flow<PagingData<MessagePreview>>

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
     * Observes all active "is typing" states received from peers.
     *
     * This [StateFlow] continuously emits the current set of user IDs
     * who are actively typing in their chat with the local user.
     *
     * Usage:
     * In viewModel:
     * ```
     * val typingUsers = repository.typingUsers
     * ```
     *
     * In UI:
     * ```
     * val typingUsers by viewModel.typingUsers.collectAsState()
     * ```
     *
     * @return A [StateFlow] emitting the set of user IDs currently typing.
     *         The initial value is an empty set.
     */
    val typingUsers: StateFlow<Set<String>>


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