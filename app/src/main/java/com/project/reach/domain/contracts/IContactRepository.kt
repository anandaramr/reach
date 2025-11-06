package com.project.reach.domain.contracts

import kotlinx.coroutines.flow.Flow

/**
 * Provides methods to retrieve, add, and update
 * contact information with reactive data access.
 */
interface IContactRepository {

    /**
     * Retrieves the username for a given user as a reactive Flow.
     *
     * @param userId The string representation of the user's ID (will be converted to UUID)
     * @return A [Flow] that emits the username whenever the contact data changes
     * @throws IllegalArgumentException if the user is not found in contacts
     */
    fun getUsername(userId: String): Flow<String>

    /**
     * Adds a new contact to the database if it doesn't already exist.
     *
     * This operation is idempotent - calling it multiple times with the same userId
     * will not create duplicate entries.
     *
     * @param userId The string representation of the user's ID
     * @param username The username to associate with this contact
     */
    suspend fun addToContacts(userId: String, username: String)

    /**
     * Updates an existing contact's username if the contact exists and the username has changed.
     *
     * This is a no-op if:
     * - The contact doesn't exist
     * - The username is already the same as the provided value
     *
     * @param userId The string representation of the user's ID
     * @param username The new username to update
     */
    suspend fun updateContactIfItExists(userId: String, username: String)
}