package com.project.reach.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.project.reach.data.local.entity.MediaEntity
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.data.model.MessageWithMedia
import com.project.reach.domain.models.MessagePreview
import com.project.reach.domain.models.MessageState
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(messageEntity: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedia(mediaEntity: MediaEntity)

    @Transaction
    suspend fun insertMessageWithMedia(messageEntity: MessageEntity, mediaEntity: MediaEntity) {
        insertMedia(mediaEntity)
        insertMessage(messageEntity)
    }

    @Transaction
    @Query("select * from messages where userId = :userId order by timeStamp desc")
    fun getMessagesByUserPaged(userId: UUID): PagingSource<Int, MessageWithMedia>

    @Query(
        value = """
            SELECT m.userId, c.username, m.messageType, m.content as "lastMessage", m.timeStamp, m.messageState
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
    fun getMessagesPreviewPaged(): PagingSource<Int, MessagePreview>

    @Transaction
    @Query("select * from messages where userId = :userId and messageState = \"PENDING\"")
    fun getPendingMessagesById(userId: UUID): Flow<List<MessageWithMedia>>

    @Query("select distinct userId from messages where messageState = \"PENDING\"")
    fun getUserIdsOfPendingMessages(): Flow<List<UUID>>

    @Query("update messages set messageState = :messageState where messageId = :messageId")
    suspend fun updateMessageState(messageId: UUID, messageState: MessageState)

    @Query("select * from messages where userId = :userId and messageState = \"RECEIVED\" order by timeStamp limit :limit")
    fun getUnreadMessagesById(userId: UUID, limit: Int): Flow<List<MessageEntity>>

    @Query("update messages set messageState = \"SENT\" where userId = :senderId and mediaId = :mediaId")
    suspend fun completeFileTransfer(senderId: UUID, mediaId: String)
}