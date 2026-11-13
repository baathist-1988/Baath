package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.ui.components.GlossyAquaButton
import com.example.ui.components.GlossyChromeTopBar
import com.example.ui.components.SlendytubbiesNightBackdrop
import com.example.ui.theme.AeroSkyBottom
import com.example.ui.theme.AeroSkyDeep
import com.example.ui.theme.AeroSkyHighlight
import com.example.ui.theme.AeroSkyMid
import com.example.ui.theme.AeroSkyTop
import com.example.ui.theme.CustardPink
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.FrostBubbleBorder
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SlendyInkLight
import com.example.ui.theme.SlendyInkMuted
import com.example.ui.theme.TubbyPurple

@Composable
fun RetroSignInScreen(onAuthSuccess: () -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = scope,
    )
  }

  SlendytubbiesNightBackdrop {
    Column(modifier = Modifier.fillMaxSize()) {
      GlossyChromeTopBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Forum,
            contentDescription = null,
            tint = CustardPink,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Slendytubbies onlyfans",
            style =
              MaterialTheme.typography.titleLarge.copy(
                shadow =
                  Shadow(
                    color = Color.Black.copy(alpha = 0.65f),
                    offset = Offset(0f, 1f),
                    blurRadius = 3f,
                  )
              ),
            color = Color.White,
          )
        }
      }

      Box(
        modifier =
          Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Surface(
          modifier =
            Modifier.widthIn(max = 440.dp)
              .fillMaxWidth()
              .shadow(16.dp, RoundedCornerShape(24.dp)),
          shape = RoundedCornerShape(24.dp),
          color = Color(0xE8101A24),
          border = BorderStroke(1.5.dp, FrostBubbleBorder),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Box(
              modifier =
                Modifier.size(116.dp)
                  .shadow(10.dp, CircleShape)
                  .clip(CircleShape)
                  .border(2.5.dp, CustardPink.copy(alpha = 0.85f), CircleShape),
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_app_logo),
                contentDescription = "Slendytubbies onlyfans app logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFF1F1633),
              border = BorderStroke(1.dp, TubbyPurple.copy(alpha = 0.65f)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(OnlineGreen))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "MAIN LAND NIGHT • EST. 2013",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = CustardPink,
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Slendytubbies onlyfans",
              style = MaterialTheme.typography.headlineMedium,
              color = SlendyInkLight,
              textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "2013 survival-horror fan lounge. Collect memories, share custard hunt stories, and chat in classic glossy bubbles.",
              style = MaterialTheme.typography.bodyMedium,
              color = SlendyInkMuted,
              textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Classic 2013 glossy sky-blue bubble preview over dark horror card
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(22.dp))
                  .background(
                    Brush.verticalGradient(
                      listOf(AeroSkyHighlight, AeroSkyTop, AeroSkyMid, AeroSkyBottom)
                    )
                  )
                  .border(1.dp, AeroSkyDeep, RoundedCornerShape(22.dp))
                  .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
              Column {
                Text(
                  text = "\"10/10 custards found on Main Land... watch out for Tinky Winky!\"",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = Color.White,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "#general  •  #lore  •  #custard-hunt  •  #sightings-2013",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.92f),
                )
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (errorMessage != null) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = ErrorCrimson.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
              ) {
                Text(
                  text = errorMessage!!,
                  style = MaterialTheme.typography.labelMedium,
                  color = ErrorCrimson,
                  modifier = Modifier.padding(10.dp),
                  textAlign = TextAlign.Center,
                )
              }
            }

            if (isLoading) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.height(48.dp),
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(24.dp),
                  color = CustardPink,
                  strokeWidth = 2.5.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Entering Main Land Lounge...",
                  style = MaterialTheme.typography.labelMedium,
                  color = SlendyInkMuted,
                )
              }
            } else {
              GlossyAquaButton(
                text = "Sign in with Google",
                onClick = {
                  errorMessage = null
                  isLoading = true
                  onGoogleSignInClicked(
                    context = context,
                    credentialManager = credentialManager,
                    onAuthSuccess = {
                      isLoading = false
                      onAuthSuccess()
                    },
                    onAuthError = { msg ->
                      isLoading = false
                      errorMessage = msg
                    },
                    scope = scope,
                    onAuthCancelled = { isLoading = false },
                  )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("google_signin_button"),
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Fans-only group • Online Firestore sync",
              style = MaterialTheme.typography.labelSmall,
              color = CustardPink,
              fontSize = 11.sp,
            )
          }
        }
      }
    }
  }
}
