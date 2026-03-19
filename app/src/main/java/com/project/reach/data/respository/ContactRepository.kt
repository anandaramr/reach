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

    val contactsList = contactDao.getAllContacts()
        .map { contacts -> contacts.associateBy { it.userId } }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    override fun getContact(userId: String): Flow<ContactUser> {
        return contactsList.mapNotNull { contacts ->
            contacts[userId.toUUID()]?.toContactUser()
        }
    }

    override fun getSavedContacts(): Flow<List<ContactUser>> {
        return contactDao.getAllSavedContacts()
            .map { it.map { contact -> contact.toContactUser() } }
    }

    override suspend fun userEntryExists(userId: UUID): Boolean {
        return contactDao.entryExists(userId)
    }

    override suspend fun isContactSaved(userId: UUID): Boolean {
        return contactDao.isContactSaved(userId)
    }

    override suspend fun saveNewContact(userId: String, username: String, nickname: String) {
        val userUuid = userId.toUUID()
        val contact = contactsList.value[userUuid]
        if (contact?.isSaved == true) {
            throw IllegalStateException("Contact already saved")
        }

        contactDao.insertContactEntity(
            ContactEntity(
                userId = userUuid,
                username = username,
                nickname = nickname,
                isSaved = true
            )
        )
    }

    override suspend fun updateSavedContactNickname(userId: String, nickname: String) {
        val userUuid = userId.toUUID()
        val contact = contactsList.value[userUuid]
        if (contact?.isSaved != true) {
            throw IllegalStateException("Contact not saved")
        }

        contactDao.updateContactNickname(userUuid, nickname)
    }

    override suspend fun addToContactsIfNotExists(userId: String, username: String) {
        val userUuid = userId.toUUID()
        val contact = contactsList.value[userUuid]
        if (contact != null) return

        // only insert if it already doesn't exist
        contactDao.insertContactEntity(
            ContactEntity(
                userId = userUuid,
                username = username
            )
        )
    }

    override suspend fun updateContactIfItExists(userId: String, username: String) {
        val userUuid = userId.toUUID()
        val contact = contactsList.value[userUuid]
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