package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.ChatChannel
import com.example.data.model.ChatMessage
import com.example.data.model.FanMember
import com.example.data.model.SLENDYTUBBIES_GROUP_ID
import com.example.data.model.TubbyFlair
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    db =
      FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
      ),
    auth = Firebase.auth,
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

  fun observeMyProfile(userId: String): Flow<UserProfile?> = flow {
    val uid = requireUserId()
    val path = "users/$userId"
    emitAll(
      db.collection("users")
        .document(userId)
        .snapshots()
        .map { snapshot ->
          snapshot.toObject(
            UserProfile::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        }
        .catch { error ->
          logStreamErrorIfActive(error, OperationType.GET, path, uid)
          throw error
        }
    )
  }

  fun observeMembers(): Flow<List<FanMember>> = flow {
    val uid = requireUserId()
    val path = "fan_members"
    emitAll(
      db.collection(path)
        .whereEqualTo("groupId", SLENDYTUBBIES_GROUP_ID)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              FanMember::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedBy { it.displayName.lowercase() }
        }
        .catch { error ->
          logStreamErrorIfActive(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeChannels(): Flow<List<ChatChannel>> = flow {
    val uid = requireUserId()
    val path = "channels"
    emitAll(
      db.collection(path)
        .whereEqualTo("groupId", SLENDYTUBBIES_GROUP_ID)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ChatChannel::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedWith(compareBy<ChatChannel> { it.sortOrder }.thenBy { it.name.lowercase() })
        }
        .catch { error ->
          logStreamErrorIfActive(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeMessages(channelId: String): Flow<List<ChatMessage>> = flow {
    val uid = requireUserId()
    val path = "channels/$channelId/messages"
    emitAll(
      db.collection("channels")
        .document(channelId)
        .collection("messages")
        .whereEqualTo("channelId", channelId)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ChatMessage::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedWith(
              compareBy<ChatMessage> {
                it.createdAt?.toDate()?.time ?: Long.MAX_VALUE
              }.thenBy { it.id }
            )
        }
        .catch { error ->
          logStreamErrorIfActive(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  suspend fun ensureProfileAndDefaultChannels(fallbackName: String): Result<Unit> = runCatching {
    val uid = requireUserId()
    val cleanName =
      fallbackName.trim().ifBlank { "TubbyFan2013" }.take(30)
    val defaultStatus = "Hunting custards since 2013"

    val userRef = db.collection("users").document(uid)
    val memberRef = db.collection("fan_members").document(uid)

    try {
      val userSnap = userRef.get().await()
      if (!userSnap.exists()) {
        val profilePayload =
          mapOf(
            "userId" to uid,
            "displayName" to cleanName,
            "tubbyFlair" to TubbyFlair.GUARDIAN.label,
            "statusMessage" to defaultStatus,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        userRef.set(profilePayload).await()
      }

      val memberSnap = memberRef.get().await()
      if (!memberSnap.exists()) {
        val existingProfile =
          userRef.get().await().toObject(UserProfile::class.java)
        val memberPayload =
          mapOf(
            "userId" to uid,
            "groupId" to SLENDYTUBBIES_GROUP_ID,
            "displayName" to (existingProfile?.displayName ?: cleanName),
            "tubbyFlair" to (existingProfile?.tubbyFlair ?: TubbyFlair.GUARDIAN.label),
            "statusMessage" to (existingProfile?.statusMessage ?: defaultStatus),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        memberRef.set(memberPayload).await()
      }

      seedDefaultChannelsIfMissing(uid)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, userRef.path, auth)
      throw e
    }
  }

  private suspend fun seedDefaultChannelsIfMissing(uid: String) {
    val existingChannels =
      db.collection("channels")
        .whereEqualTo("groupId", SLENDYTUBBIES_GROUP_ID)
        .get()
        .await()
    val existingIds = existingChannels.documents.map { it.id }.toSet()

    for (seed in DEFAULT_CHANNELS) {
      if (seed.id !in existingIds) {
        val channelRef = db.collection("channels").document(seed.id)
        val payload =
          mapOf(
            "groupId" to SLENDYTUBBIES_GROUP_ID,
            "slug" to seed.slug,
            "name" to seed.name,
            "description" to seed.description,
            "sortOrder" to seed.sortOrder,
            "createdBy" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        channelRef.set(payload).await()
      }
    }
  }

  suspend fun updateProfile(
    displayName: String,
    tubbyFlair: TubbyFlair,
    statusMessage: String,
  ): Result<Unit> = runCatching {
    val uid = requireUserId()
    val cleanName = displayName.trim().take(30)
    require(cleanName.isNotEmpty()) { "Nickname cannot be empty." }
    val cleanStatus = statusMessage.trim().take(80)

    val userRef = db.collection("users").document(uid)
    val memberRef = db.collection("fan_members").document(uid)

    try {
      val userUpdate =
        mapOf(
          "displayName" to cleanName,
          "tubbyFlair" to tubbyFlair.label,
          "statusMessage" to cleanStatus,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      userRef.update(userUpdate).await()

      val memberSnap = memberRef.get().await()
      if (memberSnap.exists()) {
        memberRef.update(userUpdate).await()
      } else {
        memberRef
          .set(
            mapOf(
              "userId" to uid,
              "groupId" to SLENDYTUBBIES_GROUP_ID,
              "displayName" to cleanName,
              "tubbyFlair" to tubbyFlair.label,
              "statusMessage" to cleanStatus,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, userRef.path, auth)
      throw e
    }
  }

  suspend fun createChannel(
    name: String,
    description: String,
    sortOrder: Long = 10L,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val cleanName = name.trim().take(40)
    require(cleanName.isNotEmpty()) { "Channel name cannot be empty." }
    val cleanDesc = description.trim().ifBlank { "Slendytubbies fan discussion" }.take(140)
    val slug =
      cleanName
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "room-${UUID.randomUUID().toString().take(6)}" }
        .take(32)
    val channelId = "${slug}-${UUID.randomUUID().toString().take(6)}"
    val channelRef = db.collection("channels").document(channelId)

    val payload =
      mapOf(
        "groupId" to SLENDYTUBBIES_GROUP_ID,
        "slug" to slug,
        "name" to cleanName,
        "description" to cleanDesc,
        "sortOrder" to sortOrder,
        "createdBy" to uid,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )

    try {
      channelRef.set(payload).await()
      channelId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, channelRef.path, auth)
      throw e
    }
  }

  suspend fun sendMessage(
    channelId: String,
    senderName: String,
    senderFlair: String,
    text: String,
    replyTo: ChatMessage? = null,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val cleanText = text.trim().take(500)
    require(cleanText.isNotEmpty()) { "Message cannot be empty." }
    val cleanSenderName = senderName.trim().ifBlank { "TubbyFan" }.take(30)
    val validFlair = TubbyFlair.fromLabel(senderFlair).label

    val channelRef = db.collection("channels").document(channelId)
    val msgRef = channelRef.collection("messages").document()

    val rawPayload =
      mapOf(
        "channelId" to channelId,
        "groupId" to SLENDYTUBBIES_GROUP_ID,
        "senderId" to uid,
        "senderName" to cleanSenderName,
        "senderFlair" to validFlair,
        "text" to cleanText,
        "replyToId" to replyTo?.id?.takeIf { it.isNotBlank() }?.take(128),
        "replyToSender" to replyTo?.senderName?.takeIf { it.isNotBlank() }?.take(30),
        "replyToText" to replyTo?.text?.takeIf { it.isNotBlank() }?.take(120),
        "createdAt" to FieldValue.serverTimestamp(),
      )
    @Suppress("UNCHECKED_CAST")
    val cleanPayload = rawPayload.filterValues { it != null } as Map<String, Any>

    try {
      msgRef.set(cleanPayload).await()
      // Update parent channel preview snippet
      channelRef
        .update(
          mapOf(
            "lastMessageText" to cleanText.take(200),
            "lastMessageSender" to cleanSenderName,
            "lastMessageAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      msgRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, msgRef.path, auth)
      throw e
    }
  }

  suspend fun deleteMessage(channelId: String, messageId: String): Result<Unit> = runCatching {
    requireUserId()
    require(channelId.isNotBlank() && messageId.isNotBlank())
    val msgRef = db.collection("channels").document(channelId).collection("messages").document(messageId)
    try {
      msgRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, msgRef.path, auth)
      throw e
    }
  }

  private fun logStreamErrorIfActive(
    error: Throwable,
    operationType: OperationType,
    path: String,
    uid: String,
  ) {
    val firestoreError =
      generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
    if (firestoreError == null && error is CancellationException) return
    val exceptionToLog = firestoreError ?: (error as? Exception)
    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
      handleFirestoreError(exceptionToLog, operationType, path, auth)
    }
  }

  private data class ChannelSeed(
    val id: String,
    val slug: String,
    val name: String,
    val description: String,
    val sortOrder: Long,
  )

  private companion object {
    val DEFAULT_CHANNELS =
      listOf(
        ChannelSeed(
          id = "general",
          slug = "general",
          name = "General",
          description = "2013 main hangout for Slendytubbies fans",
          sortOrder = 0L,
        ),
        ChannelSeed(
          id = "lore",
          slug = "lore",
          name = "Lore & Theories",
          description = "Custard infection theories, Main Land & Tubby Land secrets",
          sortOrder = 1L,
        ),
        ChannelSeed(
          id = "custard-hunt",
          slug = "custard-hunt",
          name = "Custard Hunt",
          description = "Co-op custard runs, speedrun tips & server codes",
          sortOrder = 2L,
        ),
        ChannelSeed(
          id = "sightings-2013",
          slug = "sightings-2013",
          name = "2013 Sightings",
          description = "Nostalgic v1/v2 memories, classic jumpscares & fan art",
          sortOrder = 3L,
        ),
      )
  }
}
