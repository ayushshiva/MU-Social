package com.mu.social.di

import android.content.Context
import androidx.room.Room
import com.mu.social.data.local.MuSocialDatabase
import com.mu.social.data.local.dao.PostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMuSocialDatabase(@ApplicationContext context: Context): MuSocialDatabase {
        return Room.databaseBuilder(
            context,
            MuSocialDatabase::class.java,
            "mu_social_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePostDao(db: MuSocialDatabase): PostDao {
        return db.postDao
    }
}
