package com.project.reach.data.respository

import com.project.reach.data.local.dao.ContactDao
import com.project.reach.data.local.entity.ContactEntity
import com.project.reach.data.model.ContactUser
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.util.toUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class ContactRepository(
    private val contactDao: ContactDao
): IContactRepository {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Cached contacts list may not reflect actual db state at the beginning
    // Also check db if the cached contacts list is empty
    val contactsCache = contactDao.getAllContacts()
        .map { contacts -> contacts.associateBy { it.userId } }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    override fun getContact(userId: String): Flow<ContactUser> {
        return contactsCache.mapNotNull { contacts ->
            contacts[userId.toUUID()]?.toContactUser()
        }
    }

    override fun getSavedContacts(): Flow<List<ContactUser>> {
        return contactDao.getAllSavedContacts()
            .map { it.map { contact -> contact.toContactUser() } }
    }

    override suspend fun userEntryExists(userId: UUID): Boolean {
        return contactDao.isUserEntryExists(userId)
    }

    override suspend fun isContactSaved(userId: UUID): Boolean {
        return contactDao.isUserSavedAsContact(userId)
    }

    override suspend fun saveNewContact(userId: String, username: String, nickname: String) {
        val userUuid = userId.toUUID()
        val userExists = contactDao.isUserEntryExists(userUuid)

        if (userExists){
            contactDao.setContactAsSaved(userId.toUUID(), nickname)
        } else {
            contactDao.insertContactEntity(
                ContactEntity(
                    userId = userUuid, username = username,
                    nickname = nickname, isSaved = true
                )
            )
        }
    }

    override suspend fun updateSavedContactNickname(userId: String, nickname: String) {
        val userUuid = userId.toUUID()
        contactDao.updateContactNickname(userUuid, nickname)
    }

    override suspend fun addToContactsIfNotExists(userId: String, username: String) {
        val userUuid = userId.toUUID()
        val contact = contactsCache.value[userUuid]
        if (contact != null) return

        // only insert if it doesn't exist in cache
        contactDao.insertContactEntity(
            ContactEntity(
                userId = userUuid,
                username = username
            )
        )
    }

    override suspend fun updateContactIfItExists(userId: String, username: String) {
        val userUuid = userId.toUUID()
        val contact = contactsCache.value[userUuid]
        if (contact == null || contact.username == username) return
        contactDao.updateUsername(userUuid, username)
    }

    private fun ContactEntity.toContactUser(): ContactUser {
        return ContactUser(
            userId = userId.toString(),
            username = username,
            nickname = nickname
        )
    }
}