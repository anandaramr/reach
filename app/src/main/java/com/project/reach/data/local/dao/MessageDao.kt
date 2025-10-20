package com.project.reach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.reach.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(messageEntity: MessageEntity)

    @Query("select * from messages where peerId = :peerId order by timeStamp")
    fun getMessageByUser(peerId: UUID): Flow<List<MessageEntity>>
}