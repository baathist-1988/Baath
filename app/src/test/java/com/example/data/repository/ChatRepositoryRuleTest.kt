package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.TubbyFlair
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureProfileAndDefaultChannels_authenticatedUser_createsProfileAndChannels() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = ChatRepository(firestore, auth)

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.ensureProfileAndDefaultChannels("Alice2013")
    }
    assertTrue("Expected seeding to succeed: ${result.exceptionOrNull()}", result.isSuccess)

    val profile = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeMyProfile(uid).first { it != null }
    }
    assertNotNull(profile)
    assertEquals("Alice2013", profile?.displayName)

    val channels = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeChannels().first { it.size >= 4 }
    }
    assertTrue(channels.any { it.id == "general" })
    assertTrue(channels.any { it.id == "lore" })
  }

  @Test
  fun sendMessage_andObserveMessages_emitsRealtimeMessageAndUpdatesChannel() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = ChatRepository(firestore, auth)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.ensureProfileAndDefaultChannels("Alice2013").getOrThrow()
    }

    val sendResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.sendMessage(
        channelId = "general",
        senderName = "Alice2013",
        senderFlair = TubbyFlair.GUARDIAN.label,
        text = "Welcome to Slendytubbies onlyfans 2013!",
      )
    }
    assertTrue("Expected sendMessage to succeed: ${sendResult.exceptionOrNull()}", sendResult.isSuccess)
    val messageId = sendResult.getOrThrow()

    val messages = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeMessages("general").first { list -> list.any { it.id == messageId } }
    }
    val saved = messages.first { it.id == messageId }
    assertEquals("Welcome to Slendytubbies onlyfans 2013!", saved.text)
    assertEquals("Alice2013", saved.senderName)
  }

  @Test
  fun deleteMessage_crossUserAttempt_failsWithPermissionDenied() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val aliceRepo = ChatRepository(firestore, auth)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.ensureProfileAndDefaultChannels("Alice2013").getOrThrow()
    }
    val msgId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.sendMessage(
        channelId = "lore",
        senderName = "Alice2013",
        senderFlair = TubbyFlair.PO.label,
        text = "Secret Main Land custard theory",
      ).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    val bobRepo = ChatRepository(firestore, auth)
    val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.deleteMessage("lore", msgId)
    }
    assertTrue(deleteResult.isFailure)
    val fsException = deleteResult.exceptionOrNull()?.unwrapFirestoreException()
    assertNotNull(fsException)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException?.code)
  }

  @Test
  fun observeChannels_unauthenticatedUser_failsBeforeQueryOrPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = ChatRepository(firestore, auth)

    var caught: Throwable? = null
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeChannels().first()
      }
    } catch (t: Throwable) {
      caught = t
    }
    assertNotNull(caught)
    assertTrue(
      caught is IllegalStateException ||
        caught?.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
    )
  }

  private companion object {
    const val ALICE_EMAIL = "alice_tubby@test.com"
    const val BOB_EMAIL = "bob_tubby@test.com"
    const val DEFAULT_TIMEOUT_MS = 8000L
    const val FLOW_TIMEOUT_MS = 5000L
  }
}
