package com.mu.social.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mu.social.data.local.dao.PostDao
import com.mu.social.data.local.entity.PostEntity

@Database(
    entities = [PostEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MuSocialDatabase : RoomDatabase() {
    abstract val postDao: PostDao
}
