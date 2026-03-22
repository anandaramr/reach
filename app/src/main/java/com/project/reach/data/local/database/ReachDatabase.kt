package com.project.reach.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.reach.data.local.dao.ContactDao
import com.project.reach.data.local.dao.MediaDao
import com.project.reach.data.local.dao.MessageDao
import com.project.reach.data.local.entity.ContactEntity
import com.project.reach.data.local.entity.MediaEntity
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.util.debug
import java.util.concurrent.Executors

@Database(
    version = 8,
    entities = [MessageEntity::class, ContactEntity::class, MediaEntity::class],
    exportSchema = false
)
abstract class ReachDatabase: RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var Instance: ReachDatabase? = null

        fun getDatabase(context: Context): ReachDatabase {
            return Instance ?: synchronized(this) {
                Room
                    .databaseBuilder(context, ReachDatabase::class.java, "reach_database")
                    .setQueryCallback({query,arg ->
                        debug("query: $query")
                        debug("arg: $arg")
                    }, executor = Executors.newSingleThreadExecutor())
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
        }
    }

}