package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.auth.signOutUser
import com.example.data.model.ChatChannel
import com.example.data.model.ChatMessage
import com.example.data.model.FanMember
import com.example.data.model.TubbyFlair
import com.example.data.model.UserProfile
import com.example.ui.GroupChatViewModel
import com.example.ui.UiState
import com.example.ui.components.GlossyAquaButton
import com.example.ui.components.GlossyChromeTopBar
import com.example.ui.components.Retro2013ChatBubble
import com.example.ui.components.SlendytubbiesNightBackdrop
import com.example.ui.components.TubbyFlairBadge
import com.example.ui.theme.AeroSkyBottom
import com.example.ui.theme.AeroSkyDeep
import com.example.ui.theme.AeroSkyMid
import com.example.ui.theme.AeroSkyTop
import com.example.ui.theme.ComposerBarBorder
import com.example.ui.theme.ComposerBarBottom
import com.example.ui.theme.ComposerBarTop
import com.example.ui.theme.CustardPink
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.FrostBubbleBorder
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SlendyInkLight
import com.example.ui.theme.SlendyInkMuted
import com.example.ui.theme.SlendyNightSurface

private val CLASSIC_2013_EMOTICONS = listOf(":D", ">:D", "o_o", "<3", ":)", "[Custard]")

@Composable
fun GroupChatScreen(viewModel: GroupChatViewModel) {
  val channelsState by viewModel.channelsState.collectAsStateWithLifecycle()
  val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
  val membersState by viewModel.membersState.collectAsStateWithLifecycle()
  val myProfile by viewModel.myProfile.collectAsStateWithLifecycle()
  val activeChannelId by viewModel.activeChannelId.collectAsStateWithLifecycle()
  val replyingTo by viewModel.replyingTo.collectAsStateWithLifecycle()
  val isSending by viewModel.isSending.collectAsStateWithLifecycle()
  val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val snackbarHostState = remember { SnackbarHostState() }

  var draftText by rememberSaveable { mutableStateOf("") }
  var showMembersDialog by remember { mutableStateOf(false) }
  var showProfileDialog by remember { mutableStateOf(false) }
  var showCreateChannelDialog by remember { mutableStateOf(false) }

  LaunchedEffect(bannerMessage) {
    val msg = bannerMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearBannerMessage()
    }
  }

  val channels = (channelsState as? UiState.Success)?.data.orEmpty()
  val activeChannel = channels.firstOrNull { it.id == activeChannelId }
  val memberCount = (membersState as? UiState.Success)?.data?.size ?: 1

  SlendytubbiesNightBackdrop {
    Column(modifier = Modifier.fillMaxSize()) {
      // 2013 Slendytubbies Dark Metallic Chrome Header
      GlossyChromeTopBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_app_logo),
            contentDescription = "Slendytubbies onlyfans logo",
            contentScale = ContentScale.Crop,
            modifier =
              Modifier.size(38.dp)
                .clip(CircleShape)
                .border(1.5.dp, CustardPink.copy(alpha = 0.85f), CircleShape),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Slendytubbies onlyfans",
              style =
                MaterialTheme.typography.titleLarge.copy(
                  fontSize = 18.sp,
                  shadow =
                    Shadow(
                      color = Color.Black.copy(alpha = 0.65f),
                      offset = Offset(0f, 1f),
                      blurRadius = 3f,
                    ),
                ),
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(OnlineGreen))
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text =
                  if (activeChannel != null) {
                    "#${activeChannel.slug} • $memberCount fans • Main Land"
                  } else {
                    "Main Land Night • Est. 2013"
                  },
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFE7DDF8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          IconButton(
            onClick = { showMembersDialog = true },
            modifier = Modifier.testTag("members_roster_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Group,
              contentDescription = "View online fan members",
              tint = Color.White,
            )
          }

          IconButton(
            onClick = { showProfileDialog = true },
            modifier = Modifier.testTag("profile_settings_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Edit fan profile",
              tint = CustardPink,
            )
          }
        }
      }

      BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
        val isExpanded = maxWidth >= 720.dp

        if (isExpanded) {
          Row(modifier = Modifier.fillMaxSize()) {
            ChannelSideRail(
              channels = channels,
              activeChannelId = activeChannelId,
              onSelectChannel = viewModel::selectChannel,
              onAddChannel = { showCreateChannelDialog = true },
              modifier = Modifier.width(230.dp).fillMaxHeight(),
            )
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
              ChatMessagePane(
                activeChannel = activeChannel,
                activeChannelId = activeChannelId,
                messagesState = messagesState,
                currentUserId = viewModel.currentUserId,
                onReply = viewModel::setReplyTo,
                onDelete = viewModel::deleteMessage,
                modifier = Modifier.weight(1f).fillMaxWidth(),
              )
              RetroComposerBar(
                draftText = draftText,
                onDraftChange = { draftText = it },
                replyingTo = replyingTo,
                onClearReply = viewModel::clearReplyTo,
                isSending = isSending,
                onSend = {
                  viewModel.sendMessage(draftText) { draftText = "" }
                },
              )
            }
          }
        } else {
          Column(modifier = Modifier.fillMaxSize()) {
            ChannelTabStrip(
              channels = channels,
              activeChannelId = activeChannelId,
              onSelectChannel = viewModel::selectChannel,
              onAddChannel = { showCreateChannelDialog = true },
            )
            ChatMessagePane(
              activeChannel = activeChannel,
              activeChannelId = activeChannelId,
              messagesState = messagesState,
              currentUserId = viewModel.currentUserId,
              onReply = viewModel::setReplyTo,
              onDelete = viewModel::deleteMessage,
              modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            RetroComposerBar(
              draftText = draftText,
              onDraftChange = { draftText = it },
              replyingTo = replyingTo,
              onClearReply = viewModel::clearReplyTo,
              isSending = isSending,
              onSend = {
                viewModel.sendMessage(draftText) { draftText = "" }
              },
            )
          }
        }
      }
    }

    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 76.dp),
    )
  }

  if (showMembersDialog) {
    MembersRosterDialog(
      membersState = membersState,
      onDismiss = { showMembersDialog = false },
    )
  }

  if (showProfileDialog) {
    ProfileSettingsDialog(
      profile = myProfile,
      onSave = { name, flair, status ->
        viewModel.updateProfile(name, flair, status) { showProfileDialog = false }
      },
      onSignOut = {
        showProfileDialog = false
        signOutUser(credentialManager, scope)
      },
      onDismiss = { showProfileDialog = false },
    )
  }

  if (showCreateChannelDialog) {
    CreateChannelDialog(
      onCreate = { name, desc ->
        viewModel.createChannel(name, desc) { showCreateChannelDialog = false }
      },
      onDismiss = { showCreateChannelDialog = false },
    )
  }
}

@Composable
private fun ChannelTabStrip(
  channels: List<ChatChannel>,
  activeChannelId: String,
  onSelectChannel: (String) -> Unit,
  onAddChannel: () -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .background(
          Brush.verticalGradient(listOf(Color(0xDD172330), Color(0xDD0E1620)))
        )
        .border(0.5.dp, FrostBubbleBorder.copy(alpha = 0.6f))
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    channels.forEach { channel ->
      val selected = channel.id == activeChannelId
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border =
          BorderStroke(
            width = 1.dp,
            color = if (selected) AeroSkyTop else FrostBubbleBorder,
          ),
        modifier =
          Modifier.clip(RoundedCornerShape(16.dp))
            .background(
              if (selected) {
                Brush.verticalGradient(listOf(AeroSkyTop, AeroSkyMid, AeroSkyBottom))
              } else {
                Brush.verticalGradient(listOf(Color(0xFF223344), Color(0xFF15202B)))
              }
            )
            .clickable { onSelectChannel(channel.id) }
            .testTag("channel_tab_${channel.id}"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Default.Tag,
            contentDescription = null,
            tint = if (selected) Color.White else CustardPink,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = channel.name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (selected) Color.White else SlendyInkLight,
          )
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF211735),
      border = BorderStroke(1.dp, CustardPink.copy(alpha = 0.6f)),
      modifier =
        Modifier.clip(RoundedCornerShape(16.dp))
          .clickable(onClick = onAddChannel)
          .testTag("add_channel_button"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "New fan channel",
          tint = CustardPink,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
          text = "Room",
          style = MaterialTheme.typography.labelMedium,
          color = CustardPink,
        )
      }
    }
  }
}

@Composable
private fun ChannelSideRail(
  channels: List<ChatChannel>,
  activeChannelId: String,
  onSelectChannel: (String) -> Unit,
  onAddChannel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .background(Color(0xE0111A24))
        .border(0.5.dp, FrostBubbleBorder.copy(alpha = 0.6f))
        .padding(10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "MAIN LAND ROOMS",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = CustardPink,
      )
      IconButton(onClick = onAddChannel, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Default.Add, contentDescription = "Create channel", tint = AeroSkyTop)
      }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      items(channels, key = { it.id }) { channel ->
        val selected = channel.id == activeChannelId
        Surface(
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, if (selected) AeroSkyTop else FrostBubbleBorder),
          color = if (selected) AeroSkyMid else SlendyNightSurface,
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable { onSelectChannel(channel.id) }
              .testTag("channel_rail_${channel.id}"),
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "#${channel.name}",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = Color.White,
            )
            if (!channel.lastMessageText.isNullOrBlank()) {
              Text(
                text = "${channel.lastMessageSender ?: "Fan"}: ${channel.lastMessageText}",
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White.copy(alpha = 0.88f) else SlendyInkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChatMessagePane(
  activeChannel: ChatChannel?,
  activeChannelId: String,
  messagesState: UiState<List<ChatMessage>>,
  currentUserId: String,
  onReply: (ChatMessage) -> Unit,
  onDelete: (ChatMessage) -> Unit,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()

  when (messagesState) {
    is UiState.Loading -> {
      Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(color = CustardPink)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Loading #${activeChannel?.slug ?: activeChannelId}...",
            style = MaterialTheme.typography.labelMedium,
            color = SlendyInkMuted,
          )
        }
      }
    }
    is UiState.Error -> {
      Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = SlendyNightSurface,
          border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.55f)),
        ) {
          Text(
            text = messagesState.message,
            style = MaterialTheme.typography.bodyMedium,
            color = ErrorCrimson,
            modifier = Modifier.padding(16.dp),
          )
        }
      }
    }
    is UiState.Success -> {
      val messages = messagesState.data
      LaunchedEffect(activeChannelId, messages.size) {
        if (messages.isNotEmpty()) {
          listState.animateScrollToItem(messages.lastIndex)
        }
      }

      LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp),
      ) {
        item(key = "channel_topic_header") {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xCC111B27),
            border = BorderStroke(1.dp, CustardPink.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                text = "Welcome to #${activeChannel?.name ?: activeChannelId}",
                style = MaterialTheme.typography.titleMedium,
                color = AeroSkyTop,
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = activeChannel?.description ?: "Slendytubbies onlyfans • Est. 2013",
                style = MaterialTheme.typography.labelSmall,
                color = SlendyInkMuted,
              )
            }
          }
        }

        if (messages.isEmpty()) {
          item(key = "empty_channel_prompt") {
            Box(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
              contentAlignment = Alignment.Center,
            ) {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xDB0E1620),
                border = BorderStroke(1.dp, FrostBubbleBorder),
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Image(
                    painter = painterResource(id = R.drawable.img_app_logo),
                    contentDescription = "Slendytubbies onlyfans logo",
                    contentScale = ContentScale.Crop,
                    modifier =
                      Modifier.size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, CustardPink.copy(alpha = 0.7f), CircleShape),
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = "Only fog and silence here...",
                    style = MaterialTheme.typography.titleMedium,
                    color = SlendyInkLight,
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Be the first Slendytubbies fan to send a glossy bubble here!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlendyInkMuted,
                  )
                }
              }
            }
          }
        } else {
          items(messages, key = { it.id }) { msg ->
            val isMine = msg.senderId == currentUserId
            Retro2013ChatBubble(
              message = msg,
              isMine = isMine,
              onReply = { onReply(msg) },
              onDelete = if (isMine) { { onDelete(msg) } } else null,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun RetroComposerBar(
  draftText: String,
  onDraftChange: (String) -> Unit,
  replyingTo: ChatMessage?,
  onClearReply: () -> Unit,
  isSending: Boolean,
  onSend: () -> Unit,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(Brush.verticalGradient(listOf(ComposerBarTop, ComposerBarBottom)))
        .border(0.5.dp, ComposerBarBorder)
        .navigationBarsPadding()
        .imePadding(),
  ) {
    AnimatedVisibility(visible = replyingTo != null) {
      if (replyingTo != null) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .background(Color(0xFF1F1634))
              .padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Replying to ${replyingTo.senderName}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = CustardPink,
            )
            Text(
              text = replyingTo.text,
              style = MaterialTheme.typography.labelSmall,
              color = SlendyInkMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          IconButton(onClick = onClearReply, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = SlendyInkMuted)
          }
        }
      }
    }

    // Classic 2013 quick emoticon strip
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 10.dp, vertical = 5.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      CLASSIC_2013_EMOTICONS.forEach { emote ->
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF1A2838),
          border = BorderStroke(1.dp, FrostBubbleBorder),
          modifier =
            Modifier.clip(RoundedCornerShape(10.dp))
              .clickable {
                val sep = if (draftText.isEmpty() || draftText.endsWith(" ")) "" else " "
                onDraftChange((draftText + sep + emote).take(500))
              },
        ) {
          Text(
            text = emote,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (emote == "[Custard]") CustardPink else AeroSkyTop,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.weight(1f)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0B1118))
            .border(1.dp, FrostBubbleBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
      ) {
        if (draftText.isEmpty()) {
          Text(
            text = "Message #Slendytubbies...",
            style = MaterialTheme.typography.bodyMedium,
            color = SlendyInkMuted.copy(alpha = 0.7f),
          )
        }
        BasicTextField(
          value = draftText,
          onValueChange = { if (it.length <= 500) onDraftChange(it) },
          textStyle = TextStyle(color = SlendyInkLight, fontSize = 15.sp),
          cursorBrush = SolidColor(AeroSkyTop),
          maxLines = 4,
          modifier = Modifier.fillMaxWidth().testTag("message_input"),
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      GlossyAquaButton(
        text = if (isSending) "..." else "Send",
        onClick = onSend,
        enabled = draftText.isNotBlank() && !isSending,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = Modifier.testTag("send_message_button"),
      )
    }
  }
}

@Composable
private fun MembersRosterDialog(
  membersState: UiState<List<FanMember>>,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SlendyNightSurface,
    title = {
      Text("Slendytubbies Fans Roster", style = MaterialTheme.typography.titleLarge, color = SlendyInkLight)
    },
    text = {
      when (membersState) {
        is UiState.Loading -> {
          Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CustardPink)
          }
        }
        is UiState.Error -> {
          Text(membersState.message, color = ErrorCrimson)
        }
        is UiState.Success -> {
          val members = membersState.data
          if (members.isEmpty()) {
            Text("No members found yet.", color = SlendyInkMuted)
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth().height(260.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(members, key = { it.userId }) { member ->
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(0xFF192636),
                  border = BorderStroke(1.dp, FrostBubbleBorder),
                ) {
                  Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = member.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = SlendyInkLight,
                        modifier = Modifier.weight(1f),
                      )
                      TubbyFlairBadge(flairLabel = member.tubbyFlair)
                    }
                    if (member.statusMessage.isNotBlank()) {
                      Spacer(modifier = Modifier.height(3.dp))
                      Text(
                        text = member.statusMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = SlendyInkMuted,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close", color = AeroSkyTop) }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileSettingsDialog(
  profile: UserProfile?,
  onSave: (String, TubbyFlair, String) -> Unit,
  onSignOut: () -> Unit,
  onDismiss: () -> Unit,
) {
  var nickname by remember(profile) { mutableStateOf(profile?.displayName ?: "") }
  var selectedFlair by remember(profile) {
    mutableStateOf(TubbyFlair.fromLabel(profile?.tubbyFlair ?: TubbyFlair.GUARDIAN.label))
  }
  var status by remember(profile) {
    mutableStateOf(profile?.statusMessage ?: "Hunting custards since 2013")
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SlendyNightSurface,
    title = { Text("2013 Fan Profile", style = MaterialTheme.typography.titleLarge, color = SlendyInkLight) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = nickname,
          onValueChange = { if (it.length <= 30) nickname = it },
          label = { Text("Fan Nickname") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("profile_nickname_input"),
        )
        OutlinedTextField(
          value = status,
          onValueChange = { if (it.length <= 80) status = it },
          label = { Text("2013 Status Line") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Text(
          text = "Choose Slendytubbies Flair:",
          style = MaterialTheme.typography.labelMedium,
          color = SlendyInkLight,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          TubbyFlair.entries.forEach { flair ->
            val isSelected = flair == selectedFlair
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) Color(flair.badgeHex) else Color(0xFF192636),
              border = BorderStroke(1.dp, Color(flair.badgeHex)),
              modifier = Modifier.clickable { selectedFlair = flair },
            ) {
              Text(
                text = flair.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else Color(flair.badgeHex),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }
          }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = FrostBubbleBorder)
        TextButton(
          onClick = onSignOut,
          modifier = Modifier.testTag("sign_out_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = null,
            tint = ErrorCrimson,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Sign out of Google", color = ErrorCrimson)
        }
      }
    },
    confirmButton = {
      GlossyAquaButton(
        text = "Save",
        enabled = nickname.isNotBlank(),
        onClick = { onSave(nickname, selectedFlair, status) },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      )
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = SlendyInkMuted) }
    },
  )
}

@Composable
private fun CreateChannelDialog(
  onCreate: (String, String) -> Unit,
  onDismiss: () -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SlendyNightSurface,
    title = { Text("New Fan Channel", style = MaterialTheme.typography.titleLarge, color = SlendyInkLight) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { if (it.length <= 40) name = it },
          label = { Text("Channel Name (e.g. V1 Screenshots)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("new_channel_name_input"),
        )
        OutlinedTextField(
          value = description,
          onValueChange = { if (it.length <= 140) description = it },
          label = { Text("Topic Description") },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      GlossyAquaButton(
        text = "Create",
        enabled = name.isNotBlank(),
        onClick = { onCreate(name, description) },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      )
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = SlendyInkMuted) }
    },
  )
}
