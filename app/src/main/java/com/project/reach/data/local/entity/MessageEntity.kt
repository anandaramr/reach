package com.project.reach.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.project.reach.domain.models.MessageState
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
        )
    ],
    indices = [ Index(value = ["userId"]) ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    var messageId: Long = 0,
    val text: String,
    val userId: UUID,
    val isFromPeer: Boolean,
    val messageState: MessageState = MessageState.PENDING,
    val timeStamp: Long = System.currentTimeMillis()
)