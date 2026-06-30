package com.mu.social.domain.repository

import com.mu.social.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AIRepository {
    suspend fun generateCaption(prompt: String): Resource<String>
    suspend fun generateHashtags(text: String): Resource<List<String>>
    suspend fun moderateContent(text: String): Resource<Boolean> // returns true if safe
}
