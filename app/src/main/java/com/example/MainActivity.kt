package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.authStateFlow
import com.example.data.repository.ChatRepository
import com.example.ui.GroupChatViewModel
import com.example.ui.screens.GroupChatScreen
import com.example.ui.screens.RetroSignInScreen
import com.example.ui.theme.SlendytubbiesAeroTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SlendytubbiesAeroTheme {
        AppNavigation()
      }
    }
  }
}

@Composable
fun AppNavigation() {
  val auth = Firebase.auth
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    RetroSignInScreen(
      onAuthSuccess = {
        // AuthStateListener updates currentUser reactively
      }
    )
  } else {
    val uid = user.uid
    val fallbackName = user.displayName?.takeIf { it.isNotBlank() } ?: "TubbyFan2013"
    key(uid) {
      val viewModel: GroupChatViewModel =
        viewModel(
          key = uid,
          factory =
            viewModelFactory {
              initializer {
                val app =
                  checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                  }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                GroupChatViewModel(
                  repository = ChatRepository(db, auth),
                  currentUserId = uid,
                  fallbackDisplayName = fallbackName,
                )
              }
            },
        )
      GroupChatScreen(viewModel = viewModel)
    }
  }
}
