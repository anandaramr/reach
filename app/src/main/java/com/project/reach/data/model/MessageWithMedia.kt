package com.project.reach.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.project.reach.data.local.entity.MediaEntity
import com.project.reach.data.local.entity.MessageEntity

data class MessageWithMedia(
    @Embedded val messageEntity: MessageEntity,

    @Relation(
        parentColumn = "mediaId",
        entityColumn = "mediaId"
    )
    val mediaEntity: MediaEntity?
)
