package com.mu.social.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.mu.social.domain.model.Chat
import com.mu.social.domain.model.Message
import com.mu.social.domain.model.MessageType
import com.mu.social.domain.model.User
import com.mu.social.domain.repository.ChatRepository
import com.mu.social.utils.LogTag
import com.mu.social.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ChatRepository {

    override suspend fun sendMessage(
        chatId: String,
        message: Message,
        mediaUri: Uri?
    ): Resource<Unit> {
        return try {
            var finalMessage = message

            if (mediaUri != null) {
                val fileName = UUID.randomUUID().toString()
                val ref = storage.reference.child("chats/$chatId/$fileName")
                ref.putFile(mediaUri).await()
                val url = ref.downloadUrl.await().toString()
                finalMessage = message.copy(mediaUrl = url)
            }

            val messageId = firestore.collection("chats").document(chatId)
                .collection("messages").document().id
            finalMessage = finalMessage.copy(messageId = messageId)

            firestore.collection("chats").document(chatId)
                .collection("messages").document(messageId)
                .set(finalMessage).await()

            firestore.collection("chats").document(chatId)
                .update(
                    "lastMessage",
                    if (finalMessage.messageType == MessageType.TEXT) finalMessage.text else finalMessage.messageType.name,
                    "lastMessageTimestamp",
                    finalMessage.timestamp,
                    "lastMessageSenderId",
                    finalMessage.senderId
                ).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.d(LogTag.CHAT_REPO, "sendMessage error=${e.localizedMessage}")
            Resource.Error(e.localizedMessage ?: "Failed to send message")
        }
    }

    override fun getMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(LogTag.CHAT_REPO, "getMessages error=${error.localizedMessage}")
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }

                val messages = snapshot?.toObjects(Message::class.java).orEmpty()
                trySend(Resource.Success(messages))
            }
        awaitClose { subscription.remove() }
    }

    override fun getChats(userId: String): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading())

        Log.d(LogTag.CHAT_REPO, "getChats query start userId=$userId")

        val subscription = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(LogTag.CHAT_REPO, "getChats snapshot error=${error.localizedMessage}")
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }

                val docCount = snapshot?.size() ?: 0
                Log.d(LogTag.CHAT_REPO, "getChats snapshot docCount=$docCount")

                val chats = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data

                    // Defensive normalization for participants
                    val rawParticipants = data?.get("participants")
                    val normalizedParticipants: List<String> = when (rawParticipants) {
                        is List<*> -> rawParticipants
                            .mapNotNull { item ->
                                when (item) {
                                    is String -> item.trim().takeIf { it.isNotBlank() }
                                    else -> null
                                }
                            }
                        else -> emptyList()
                    }

                    val lastMessageTimestamp = (data?.get("lastMessageTimestamp") as? Number)?.toLong() ?: 0L

                    val chat = Chat(
                        chatId = (data?.get("chatId") as? String) ?: doc.id,
                        participants = normalizedParticipants,
                        lastMessage = (data?.get("lastMessage") as? String) ?: "",
                        lastMessageTimestamp = lastMessageTimestamp,
                        lastMessageSenderId = (data?.get("lastMessageSenderId") as? String) ?: "",
                        isGroup = (data?.get("isGroup") as? Boolean) ?: false,
                        groupName = data?.get("groupName") as? String,
                        groupIcon = data?.get("groupIcon") as? String,
                        typingStatus = (data?.get("typingStatus") as? Map<*, *>)
                            ?.mapNotNull { (k, v) ->
                                val key = k as? String ?: return@mapNotNull null
                                val value = v as? Boolean ?: return@mapNotNull null
                                key to value
                            }
                            ?.toMap()
                            .orEmpty()
                    )

                    // Defensive sanity: only reject clearly invalid chat docs.
                    // Firestore already guarantees whereArrayContains(..., userId) matched.
                    // If participants is totally missing/empty, keep only if doc still has chatId and timestamp.
                    val hasCoreFields = chat.chatId.isNotBlank() && chat.lastMessageTimestamp > 0
                    if (!hasCoreFields) {
                        null
                    } else {
                        chat
                    }
                }.orEmpty()

                val parsedCount = chats.size
                Log.d(LogTag.CHAT_REPO, "getChats parsed chatCount=$parsedCount")

                trySend(Resource.Success(chats))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun createChat(participants: List<String>): Resource<String> {
        return try {
            // Check if chat already exists
            val existingChat = firestore.collection("chats")
                .whereArrayContains("participants", participants[0])
                .get().await()
                .documents.find {
                    val parts = it.get("participants") as? List<*>
                    parts?.containsAll(participants) == true && parts.size == participants.size
                }

            if (existingChat != null) {
                return Resource.Success(existingChat.id)
            }

            val chatId = firestore.collection("chats").document().id
            val chat = Chat(
                chatId = chatId,
                participants = participants,
                lastMessageTimestamp = System.currentTimeMillis()
            )
            firestore.collection("chats").document(chatId).set(chat).await()
            Resource.Success(chatId)
        } catch (e: Exception) {
            Log.d(LogTag.CHAT_REPO, "createChat error=${e.localizedMessage}")
            Resource.Error(e.localizedMessage ?: "Failed to create chat")
        }
    }

    override suspend fun markMessageAsSeen(chatId: String, messageId: String): Resource<Unit> {
        return try {
            firestore.collection("chats").document(chatId)
                .collection("messages").document(messageId)
                .update("seen", true).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.d(LogTag.CHAT_REPO, "markMessageAsSeen error=${e.localizedMessage}")
            Resource.Error(e.localizedMessage ?: "Failed to update seen status")
        }
    }

    override suspend fun deleteMessage(chatId: String, messageId: String, forEveryone: Boolean): Resource<Unit> {
        return try {
            if (forEveryone) {
                firestore.collection("chats").document(chatId)
                    .collection("messages").document(messageId)
                    .update("isDeletedForEveryone", true).await()
            } else {
                // Delete for me not implemented server-side.
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.d(LogTag.CHAT_REPO, "deleteMessage error=${e.localizedMessage}")
            Resource.Error(e.localizedMessage ?: "Failed to delete message")
        }
    }

    override suspend fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean): Resource<Unit> {
        return try {
            firestore.collection("chats").document(chatId)
                .update("typingStatus.$userId", isTyping).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.d(LogTag.CHAT_REPO, "setTypingStatus error=${e.localizedMessage}")
            Resource.Error(e.localizedMessage ?: "Failed to update typing status")
        }
    }

    override fun getUserPresence(userId: String): Flow<Resource<User>> = callbackFlow {
        val subscription = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error"))
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                if (user != null) trySend(Resource.Success(user))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateUserPresence(userId: String, isOnline: Boolean): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("isOnline", isOnline, "lastSeen", System.currentTimeMillis()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update presence")
        }
    }

    override fun getChatPartner(participants: List<String>, currentUserId: String): Flow<Resource<User>> = callbackFlow {
        val partnerId = participants.find { it != currentUserId } ?: return@callbackFlow
        val subscription = firestore.collection("users").document(partnerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val user = snapshot?.toObject(User::class.java)
                if (user != null) trySend(Resource.Success(user))
            }
        awaitClose { subscription.remove() }
    }
}

