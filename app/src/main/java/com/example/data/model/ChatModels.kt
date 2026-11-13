package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

const val SLENDYTUBBIES_GROUP_ID = "slendytubbies-onlyfans"

enum class TubbyFlair(
  val label: String,
  val shortTag: String,
  val badgeHex: Long,
) {
  GUARDIAN("Guardian", "GRD", 0xFF29A8E8),
  TINKY_WINKY("Tinky Winky", "TNK", 0xFF7B4DFF),
  DIPSY("Dipsy", "DPS", 0xFF39B54A),
  LAA_LAA("Laa-Laa", "LAA", 0xFFE6A700),
  PO("Po", "PO", 0xFFE53935),
  NEWBORN("Newborn", "NWB", 0xFF546E7A);

  companion object {
    fun fromLabel(label: String): TubbyFlair =
      entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: GUARDIAN
  }
}

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val tubbyFlair: String = TubbyFlair.GUARDIAN.label,
  val statusMessage: String = "Collecting custards since 2013",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class FanMember(
  @DocumentId val id: String = "",
  val userId: String = "",
  val groupId: String = SLENDYTUBBIES_GROUP_ID,
  val displayName: String = "",
  val tubbyFlair: String = TubbyFlair.GUARDIAN.label,
  val statusMessage: String = "Online in 2013 Lounge",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ChatChannel(
  @DocumentId val id: String = "",
  val groupId: String = SLENDYTUBBIES_GROUP_ID,
  val slug: String = "",
  val name: String = "",
  val description: String = "",
  val sortOrder: Long = 0L,
  val createdBy: String = "",
  val lastMessageText: String? = null,
  val lastMessageSender: String? = null,
  val lastMessageAt: Timestamp? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ChatMessage(
  @DocumentId val id: String = "",
  val channelId: String = "",
  val groupId: String = SLENDYTUBBIES_GROUP_ID,
  val senderId: String = "",
  val senderName: String = "",
  val senderFlair: String = TubbyFlair.GUARDIAN.label,
  val text: String = "",
  val replyToId: String? = null,
  val replyToSender: String? = null,
  val replyToText: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
)
