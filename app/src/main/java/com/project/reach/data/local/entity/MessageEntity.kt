package com.project.reach.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.MessageType
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MediaEntity::class,
            parentColumns = ["mediaId"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "timeStamp"]), Index(value = ["mediaId"])]
)
data class MessageEntity(
    @PrimaryKey var messageId: UUID,
    val content: String,
    val messageType: MessageType,
    val mediaId: String? = null,
    val userId: UUID,
    val isFromPeer: Boolean,
    val messageState: MessageState,
    val timeStamp: Long = System.currentTimeMillis()
)