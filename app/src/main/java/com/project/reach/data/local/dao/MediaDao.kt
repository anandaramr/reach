package com.project.reach.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.project.reach.data.local.entity.MediaEntity

@Dao
interface MediaDao {
    @Query("select * from media where mediaId = :fileHash")
    fun getFileByHash(fileHash: String): MediaEntity
}