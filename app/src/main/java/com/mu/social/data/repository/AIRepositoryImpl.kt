package com.mu.social.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.mu.social.domain.repository.AIRepository
import com.mu.social.utils.Resource
import javax.inject.Inject

class AIRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel
) : AIRepository {

    override suspend fun generateCaption(prompt: String): Resource<String> {
        return try {
            val response = generativeModel.generateContent("Create a short, engaging social media caption for: $prompt")
            Resource.Success(response.text ?: "")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to generate caption")
        }
    }

    override suspend fun generateHashtags(text: String): Resource<List<String>> {
        return try {
            val response = generativeModel.generateContent("Generate 5-10 relevant hashtags for this social media post: $text. Return only hashtags separated by spaces.")
            val hashtags = response.text?.split(" ")?.filter { it.startsWith("#") } ?: emptyList()
            Resource.Success(hashtags)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to generate hashtags")
        }
    }

    override suspend fun moderateContent(text: String): Resource<Boolean> {
        return try {
            val response = generativeModel.generateContent("Analyze the following text for hate speech, harassment, or inappropriate content: \"$text\". Does it violate safety policies? Respond with ONLY 'SAFE' or 'UNSAFE'.")
            val isSafe = response.text?.contains("SAFE", ignoreCase = true) == true
            Resource.Success(isSafe)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Moderation failed")
        }
    }
}
