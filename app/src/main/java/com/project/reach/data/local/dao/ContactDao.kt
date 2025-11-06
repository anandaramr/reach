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
    suspend fun insertContact(contact: ContactEntity)

    @Query("select username from contacts where userId = :userId")
    fun getUsername(userId: UUID): Flow<String>

    @Query("update contacts set username = :username where userId = :userId")
    fun updateUsername(userId: UUID, username: String)

    @Query("select * from contacts")
    fun getAllContacts(): Flow<List<ContactEntity>>
}