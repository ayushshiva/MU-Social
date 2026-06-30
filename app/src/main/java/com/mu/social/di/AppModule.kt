package com.mu.social.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.mu.social.data.local.dao.PostDao
import com.mu.social.data.repository.*
import com.mu.social.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.mu.social.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions = FirebaseFunctions.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(auth, firestore)

    @Provides
    @Singleton
    fun providePostRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        postDao: PostDao
    ): PostRepository = PostRepositoryImpl(firestore, storage, postDao)

    @Provides
    @Singleton
    fun provideSocialRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): SocialRepository = SocialRepositoryImpl(firestore, storage)

    @Provides
    @Singleton
    fun provideStoryRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): StoryRepository = StoryRepositoryImpl(firestore, storage)

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ChatRepository = ChatRepositoryImpl(firestore, storage)

    @Provides
    @Singleton
    fun provideReelRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ReelRepository = ReelRepositoryImpl(firestore, storage)

    @Provides
    @Singleton
    fun provideAIRepository(
        generativeModel: GenerativeModel
    ): AIRepository = AIRepositoryImpl(generativeModel)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore
    ): NotificationRepository = NotificationRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        firestore: FirebaseFirestore
    ): AnalyticsRepository = AnalyticsRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideModerationRepository(
        firestore: FirebaseFirestore
    ): ModerationRepository = ModerationRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideLiveStreamRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        functions: FirebaseFunctions
    ): LiveStreamRepository = LiveStreamRepositoryImpl(firestore, auth, functions)

    @Provides
    @Singleton
    fun provideWalletRepository(
        firestore: FirebaseFirestore,
        functions: FirebaseFunctions
    ): WalletRepository = WalletRepositoryImpl(firestore, functions)
}
