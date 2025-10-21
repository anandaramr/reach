package com.project.reach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.models.MessagePreview
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(messageEntity: MessageEntity)

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

}