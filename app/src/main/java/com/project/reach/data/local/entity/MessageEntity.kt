package com.project.reach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    var messageId: Int = 0,
    val text: String,
    val peerId: UUID,
    val isFromPeer: Boolean,
    val timeStamp: Long = System.currentTimeMillis()
)