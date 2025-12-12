package com.project.reach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val userId: UUID,
    val username: String,
    val nickname: String = ""
)