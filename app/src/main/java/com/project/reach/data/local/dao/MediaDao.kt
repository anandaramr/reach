package com.project.reach.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import java.util.UUID

@Dao
interface MediaDao {
    @Query("select uri from media where mediaId = :fileId")
    fun getUriByFileId(fileId: UUID): String
}