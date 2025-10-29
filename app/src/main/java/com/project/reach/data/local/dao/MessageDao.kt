package com.project.reach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.models.MessagePreview
import com.project.reach.domain.models.MessageState
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(messageEntity: MessageEntity): Long

    @Query("select * from messages where userId = :userId order by timeStamp")
    fun getMessageByUser(userId: UUID): Flow<List<MessageEntity>>

    @Query(
        value = """
            SELECT m.userId, c.username, m.text as "lastMessage", m.timeStamp, m.messageState
            FROM messages AS m
            JOIN
            contacts AS c ON c.userId = m.userId
            WHERE m.timeStamp = (
                SELECT MAX(m2.timeStamp)
                FROM messages AS m2
                WHERE m2.userId = m.userId
            )
            order by m.timeStamp desc
        """
    )
    fun getMessagesPreview(): Flow<List<MessagePreview>>

    @Query("select * from messages where userId = :userId and messageState = \"PENDING\"")
    fun getPendingMessagesById(userId: UUID): Flow<List<MessageEntity>>

    @Query("update messages set messageState = :messageState where messageId = :messageId")
    suspend fun updateMessageState(messageId: Long, messageState: MessageState)

    @Query("select * from messages where userId = :userId and messageState = \"RECEIVED\" order by timeStamp")
    fun getUnreadMessagesById(userId: UUID): Flow<List<MessageEntity>>
}