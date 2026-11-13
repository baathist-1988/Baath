package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatChannel
import com.example.data.model.ChatMessage
import com.example.data.model.FanMember
import com.example.data.model.TubbyFlair
import com.example.data.model.UserProfile
import com.example.data.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class GroupChatViewModel(
  private val repository: ChatRepository,
  val currentUserId: String,
  private val fallbackDisplayName: String,
) : ViewModel() {

  private val _activeChannelId = MutableStateFlow(DEFAULT_CHANNEL_ID)
  val activeChannelId: StateFlow<String> = _activeChannelId.asStateFlow()

  private val _replyingTo = MutableStateFlow<ChatMessage?>(null)
  val replyingTo: StateFlow<ChatMessage?> = _replyingTo.asStateFlow()

  private val _isSending = MutableStateFlow(false)
  val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

  private val _bannerMessage = MutableStateFlow<String?>(null)
  val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

  private val _isSeeding = MutableStateFlow(true)
  val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

  val myProfile: StateFlow<UserProfile?> =
    repository
      .observeMyProfile(currentUserId)
      .catch { error ->
        unwrapFirestoreError(error)?.let {
          Log.w(TAG, "Error observing profile", it)
        }
        emit(null)
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = null,
      )

  val channelsState: StateFlow<UiState<List<ChatChannel>>> =
    repository
      .observeChannels()
      .map<List<ChatChannel>, UiState<List<ChatChannel>>> { UiState.Success(it) }
      .catch { error ->
        val rootError = unwrapOrRethrowCancellation(error)
        Log.w(TAG, "Error observing channels", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load fan channels"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val messagesState: StateFlow<UiState<List<ChatMessage>>> =
    _activeChannelId
      .flatMapLatest { channelId ->
        repository
          .observeMessages(channelId)
          .map<List<ChatMessage>, UiState<List<ChatMessage>>> { UiState.Success(it) }
          .catch { error ->
            val rootError = unwrapOrRethrowCancellation(error)
            Log.w(TAG, "Error observing messages in $channelId", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load messages"))
          }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val membersState: StateFlow<UiState<List<FanMember>>> =
    repository
      .observeMembers()
      .map<List<FanMember>, UiState<List<FanMember>>> { UiState.Success(it) }
      .catch { error ->
        val rootError = unwrapOrRethrowCancellation(error)
        Log.w(TAG, "Error observing fan members", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load members"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  init {
    viewModelScope.launch {
      _isSeeding.value = true
      repository
        .ensureProfileAndDefaultChannels(fallbackDisplayName)
        .onFailure { error ->
          _bannerMessage.value = error.localizedMessage ?: "Failed to initialize group lounge"
        }
      _isSeeding.value = false
    }
  }

  fun selectChannel(channelId: String) {
    if (channelId.isNotBlank() && _activeChannelId.value != channelId) {
      _activeChannelId.value = channelId
      _replyingTo.value = null
    }
  }

  fun setReplyTo(message: ChatMessage) {
    _replyingTo.value = message
  }

  fun clearReplyTo() {
    _replyingTo.value = null
  }

  fun clearBannerMessage() {
    _bannerMessage.value = null
  }

  fun sendMessage(rawText: String, onSuccess: () -> Unit) {
    val trimmed = rawText.trim()
    if (trimmed.isEmpty() || _isSending.value) return

    val profile = myProfile.value
    val senderName = profile?.displayName?.ifBlank { null } ?: fallbackDisplayName.ifBlank { "TubbyFan2013" }
    val senderFlair = profile?.tubbyFlair ?: TubbyFlair.GUARDIAN.label
    val replyTarget = _replyingTo.value
    val targetChannel = _activeChannelId.value

    viewModelScope.launch {
      _isSending.value = true
      val result =
        repository.sendMessage(
          channelId = targetChannel,
          senderName = senderName,
          senderFlair = senderFlair,
          text = trimmed,
          replyTo = replyTarget,
        )
      _isSending.value = false
      result
        .onSuccess {
          _replyingTo.value = null
          onSuccess()
        }
        .onFailure { error ->
          _bannerMessage.value = error.localizedMessage ?: "Couldn't send message"
        }
    }
  }

  fun deleteMessage(message: ChatMessage) {
    if (message.senderId != currentUserId || message.id.isBlank()) return
    viewModelScope.launch {
      repository
        .deleteMessage(message.channelId.ifBlank { _activeChannelId.value }, message.id)
        .onFailure { error ->
          _bannerMessage.value = error.localizedMessage ?: "Couldn't delete message"
        }
    }
  }

  fun updateProfile(
    displayName: String,
    tubbyFlair: TubbyFlair,
    statusMessage: String,
    onSaved: () -> Unit,
  ) {
    viewModelScope.launch {
      repository
        .updateProfile(displayName, tubbyFlair, statusMessage)
        .onSuccess {
          _bannerMessage.value = "Fan profile saved!"
          onSaved()
        }
        .onFailure { error ->
          _bannerMessage.value = error.localizedMessage ?: "Couldn't update profile"
        }
    }
  }

  fun createChannel(name: String, description: String, onCreated: () -> Unit) {
    val currentCount = (channelsState.value as? UiState.Success)?.data?.size?.toLong() ?: 4L
    viewModelScope.launch {
      repository
        .createChannel(name = name, description = description, sortOrder = currentCount)
        .onSuccess { newId ->
          _activeChannelId.value = newId
          _replyingTo.value = null
          onCreated()
        }
        .onFailure { error ->
          _bannerMessage.value = error.localizedMessage ?: "Couldn't create channel"
        }
    }
  }

  private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
    generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()

  private fun unwrapOrRethrowCancellation(error: Throwable): Throwable {
    val firestoreError = unwrapFirestoreError(error)
    if (firestoreError == null && error is CancellationException) throw error
    return firestoreError ?: error
  }

  private companion object {
    const val TAG = "GroupChatVM"
    const val DEFAULT_CHANNEL_ID = "general"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
