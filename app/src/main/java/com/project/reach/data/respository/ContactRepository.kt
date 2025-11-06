package com.project.reach.data.respository

import com.project.reach.data.local.dao.ContactDao
import com.project.reach.data.local.entity.ContactEntity
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.util.toUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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

    override fun getUsername(userId: String): Flow<String> {
        return contactsList.map { contacts ->
            contacts[userId.toUUID()]?.username ?: throw IllegalArgumentException("User not found")
        }
    }

    override suspend fun addToContacts(userId: String, username: String) {
        val userUuid = userId.toUUID()
        val contact = contactsList.value[userUuid]
        if (contact != null) return

        // only insert if it already doesn't exist
        contactDao.insertContact(
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
}