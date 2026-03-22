package com.project.reach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.reach.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContactEntity(contact: ContactEntity)

    @Query("update contacts set nickname = :nickname, isSaved = 1 where userId = :userId")
    suspend fun setContactAsSaved(userId: UUID, nickname: String)

    @Query("select username from contacts where userId = :userId")
    fun getUsername(userId: UUID): Flow<String>

    @Query("update contacts set username = :username where userId = :userId")
    suspend fun updateUsername(userId: UUID, username: String)

    @Query("select * from contacts")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("select * from contacts where isSaved = 1 order by nickname")
    fun getAllSavedContacts(): Flow<List<ContactEntity>>

    @Query("update contacts set nickname = :nickname where userId = :userId")
    suspend fun updateContactNickname(userId: UUID, nickname: String)

    @Query("select exists(select 1 from contacts where userId = :userId)")
    suspend fun isUserEntryExists(userId: UUID): Boolean

    @Query("select exists(select userId from contacts where userId = :userId and isSaved)")
    suspend fun isUserSavedAsContact(userId: UUID): Boolean
}