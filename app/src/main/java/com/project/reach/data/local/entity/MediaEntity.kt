package com.project.reach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val mediaId: String,
    val uri: String,
    val filename: String,
    val mimeType: String,
    val size: Long
)