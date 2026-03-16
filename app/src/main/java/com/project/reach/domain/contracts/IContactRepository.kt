package com.project.reach.domain.contracts

import com.project.reach.data.model.ContactUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing contact data with reactive updates.
 *
 * This repository maintains an in-memory cache of contacts via [contactsList]
 * which is automatically synchronized with the database. All queries benefit from
 * this cache, reducing unnecessary database operations.
 *
 * Contacts can exist in two states:
 * - **Unsaved**: Basic contact information (userId, username) stored for reference
 * - **Saved**: Full contact with user-defined nickname, marked with isSaved = true
 *
 * @see com.project.reach.data.local.dao.ContactDao for underlying database operations
 */
interface IContactRepository {

    /**
     * Retrieves the username for a specific user as a reactive stream.
     *
     * This flow emits the current username immediately and updates whenever
     * the contact's username changes in the database.
     *
     * @param userId The user's ID as a string (will be converted to UUID internally)
     * @return A [Flow] emitting the username, or no emissions if the contact doesn't exist
     *
     * @see addToContactsIfNotExists to ensure a contact exists before observing
     */
    fun getDisplayName(userId: String): Flow<String>

    /**
     * Retrieves all saved contacts as a reactive stream.
     *
     * Returns only "saved" contacts, ordered by nickname.
     * The flow emits the complete list whenever any saved contact changes.
     *
     * @return A [Flow] emitting lists of [ContactUser] with display names (nickname or username)
     */
    fun getSavedContacts(): Flow<List<ContactUser>>

    /**
     * Promotes an existing contact to "saved" status with a nickname.
     *
     * This marks a contact as saved and assigns a user-defined nickname for display.
     *
     * @param userId The user's ID as a string
     * @param username The contact's username
     * @param nickname The user-defined display name for this contact
     *
     * @throws IllegalStateException if the contact is already saved
     *
     * @see updateSavedContactNickname to modify an existing saved contact's nickname
     */
    suspend fun saveNewContact(userId: String, username: String, nickname: String)

    /**
     * Updates the nickname of an already-saved contact.
     *
     * @param userId The user's ID as a string
     * @param nickname The new nickname to assign
     *
     * @throws IllegalStateException if the contact is not currently saved
     *
     * @see saveNewContact to save a contact for the first time
     */
    suspend fun updateSavedContactNickname(userId: String, nickname: String)

    /**
     * Adds an unsaved contact to the database if it doesn't already exist.
     *
     * The contact won't appear in the saved contacts list until [saveNewContact] is called.
     *
     * This is an idempotent operation optimized for performance:
     * - Uses in-memory cache to check existence (no database query if cached)
     * - Only inserts if the contact doesn't exist
     * - Safe to call multiple times with the same userId
     *
     * **Use case**: Create contact records before interacting with the user
     *
     * @param userId The user's ID as a string
     * @param username The username to store for this new unsaved contact
     *
     * @see saveNewContact to promote an unsaved contact to saved status with a nickname
     */
    suspend fun addToContactsIfNotExists(userId: String, username: String)

    /**
     * Updates a contact's username if the contact exists and the username differs.
     *
     * This is a selective update that only writes to the database when necessary:
     * - No-op if contact doesn't exist (doesn't create a new contact)
     * - No-op if the username is already correct (avoids redundant writes)
     *
     * Use this to keep contact usernames synchronized with external sources while
     * avoiding unnecessary database operations.
     *
     * @param userId The user's ID as a string
     * @param username The new username (no update if already matches)
     */
    suspend fun updateContactIfItExists(userId: String, username: String)
}