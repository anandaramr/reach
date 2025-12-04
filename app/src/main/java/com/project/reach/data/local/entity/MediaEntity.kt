package com.project.reach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val mediaId: UUID,
    val uri: String,
    val mimeType: String,
    val fileSize: Long
)