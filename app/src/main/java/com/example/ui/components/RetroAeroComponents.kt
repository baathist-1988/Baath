package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.TubbyFlair
import com.example.ui.theme.AeroSkyBottom
import com.example.ui.theme.AeroSkyDeep
import com.example.ui.theme.AeroSkyHighlight
import com.example.ui.theme.AeroSkyMid
import com.example.ui.theme.AeroSkyTop
import com.example.ui.theme.CustardPink
import com.example.ui.theme.FrostBubbleBorder
import com.example.ui.theme.FrostBubbleBottom
import com.example.ui.theme.FrostBubbleMid
import com.example.ui.theme.FrostBubbleTop
import com.example.ui.theme.SlendyChromeBorderDark
import com.example.ui.theme.SlendyChromeBottom
import com.example.ui.theme.SlendyChromeMid
import com.example.ui.theme.SlendyChromeTop
import com.example.ui.theme.SlendyInkLight
import com.example.ui.theme.SlendyInkMuted
import com.example.ui.theme.SlendyNightBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SlendytubbiesNightBackdrop(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(modifier = modifier.fillMaxSize().background(SlendyNightBg)) {
    Image(
      painter = painterResource(id = R.drawable.img_chat_bg),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      alpha = 0.94f,
      modifier = Modifier.fillMaxSize(),
    )
    // Subtle 2013 Unity vignette & faint scanlines while keeping the uploaded Main Land scene clear
    Box(
      modifier =
        Modifier.fillMaxSize().drawBehind {
          drawRect(
            brush =
              Brush.verticalGradient(
                colors =
                  listOf(
                    Color(0x38080E15),
                    Color(0x140A141E),
                    Color(0x48070C12),
                  )
              )
          )
          val step = 3.dp.toPx()
          var y = 0f
          while (y < size.height) {
            drawLine(
              color = Color.Black.copy(alpha = 0.08f),
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = 1f,
            )
            y += step
          }
        }
    )
    content()
  }
}

@Composable
fun GlossyAquaButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
) {
  val shape = RoundedCornerShape(22.dp)
  val gradient =
    if (enabled) {
      Brush.verticalGradient(listOf(AeroSkyHighlight, AeroSkyTop, AeroSkyMid, AeroSkyBottom))
    } else {
      Brush.verticalGradient(listOf(Color(0xFF334B61), Color(0xFF233545)))
    }

  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .clip(shape)
        .background(gradient)
        .border(
          width = 1.dp,
          color = if (enabled) AeroSkyDeep else Color(0xFF2B3F52),
          shape = shape,
        )
        .drawBehind {
          drawRoundRect(
            brush =
              Brush.verticalGradient(
                colors =
                  listOf(
                    Color.White.copy(alpha = if (enabled) 0.56f else 0.15f),
                    Color.White.copy(alpha = 0.06f),
                  ),
                startY = 0f,
                endY = size.height * 0.52f,
              ),
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.52f),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
          )
        }
        .clickable(enabled = enabled, onClick = onClick)
        .padding(contentPadding),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      style =
        MaterialTheme.typography.labelMedium.copy(
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          shadow =
            Shadow(
              color = Color.Black.copy(alpha = 0.45f),
              offset = Offset(0f, -1f),
              blurRadius = 2f,
            ),
        ),
      color = if (enabled) Color.White else SlendyInkMuted,
    )
  }
}

@Composable
fun GlossyChromeTopBar(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(SlendyChromeTop, SlendyChromeMid, SlendyChromeBottom)
          )
        )
        .drawBehind {
          drawLine(
            color = Color.White.copy(alpha = 0.28f),
            start = Offset(0f, 1.dp.toPx()),
            end = Offset(size.width, 1.dp.toPx()),
            strokeWidth = 1.2.dp.toPx(),
          )
          drawLine(
            color = CustardPink.copy(alpha = 0.45f),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.5.dp.toPx(),
          )
          drawLine(
            color = SlendyChromeBorderDark,
            start = Offset(0f, size.height - 1.5.dp.toPx()),
            end = Offset(size.width, size.height - 1.5.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
          )
        }
  ) {
    content()
  }
}

@Composable
fun TubbyFlairBadge(flairLabel: String, modifier: Modifier = Modifier) {
  val flair = TubbyFlair.fromLabel(flairLabel)
  val badgeColor = Color(flair.badgeHex)
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    color = badgeColor.copy(alpha = 0.22f),
    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.72f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(badgeColor))
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = flair.label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
      )
    }
  }
}

/**
 * 2013 glossy sky-blue speech bubble (outgoing) & smoked midnight-glass bubble (incoming)
 * floating over the foggy Slendytubbies Main Land night backdrop.
 */
@Composable
fun Retro2013ChatBubble(
  message: ChatMessage,
  isMine: Boolean,
  onReply: () -> Unit,
  onDelete: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  val formattedTime = formatBubbleTime(message.createdAt?.toDate())
  val alignment = if (isMine) Alignment.End else Alignment.Start

  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
    horizontalAlignment = alignment,
  ) {
    if (!isMine) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xA60B131C),
        modifier = Modifier.padding(start = 10.dp, bottom = 4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
          Text(
            text = message.senderName,
            style =
              MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
              ),
            color = SlendyInkLight,
          )
          Spacer(modifier = Modifier.width(6.dp))
          TubbyFlairBadge(flairLabel = message.senderFlair)
        }
      }
    }

    Box(
      modifier =
        Modifier.widthIn(min = 96.dp, max = 300.dp)
          .drawBehind {
            val tailSize = 11.dp.toPx()
            val tailDrop = 9.dp.toPx()
            val bubbleBottom = size.height - tailDrop
            val radius = minOf(22.dp.toPx(), (size.width - tailSize) / 2.2f, bubbleBottom / 2f)

            val bubblePath =
              build2013BubblePath(
                width = size.width,
                bubbleBottom = bubbleBottom,
                totalHeight = size.height,
                cornerRadius = radius,
                tailInset = tailSize,
                tailOnRight = isMine,
              )

            // Soft night drop shadow
            val shadowPath =
              build2013BubblePath(
                width = size.width,
                bubbleBottom = bubbleBottom,
                totalHeight = size.height,
                cornerRadius = radius,
                tailInset = tailSize,
                tailOnRight = isMine,
                offsetY = 3.dp.toPx(),
              )
            drawPath(
              path = shadowPath,
              color = Color.Black.copy(alpha = 0.45f),
            )

            clipPath(bubblePath) {
              drawRect(
                brush =
                  if (isMine) {
                    Brush.verticalGradient(
                      colors = listOf(AeroSkyTop, AeroSkyMid, AeroSkyBottom),
                      startY = 0f,
                      endY = size.height,
                    )
                  } else {
                    Brush.verticalGradient(
                      colors = listOf(FrostBubbleTop, FrostBubbleMid, FrostBubbleBottom),
                      startY = 0f,
                      endY = size.height,
                    )
                  }
              )

              drawCircle(
                brush =
                  Brush.radialGradient(
                    colors =
                      listOf(
                        if (isMine) AeroSkyHighlight.copy(alpha = 0.55f)
                        else Color(0xFF6AAEE6).copy(alpha = 0.24f),
                        Color.Transparent,
                      ),
                    center = Offset(size.width * 0.45f, bubbleBottom * 0.28f),
                    radius = maxOf(size.width, bubbleBottom) * 0.65f,
                  )
              )

              // Curved mid-bubble gloss horizon matching reference bubble
              val glossPath =
                Path().apply {
                  moveTo(0f, 0f)
                  lineTo(size.width, 0f)
                  lineTo(size.width, bubbleBottom * 0.46f)
                  quadraticTo(
                    size.width * 0.5f,
                    bubbleBottom * 0.62f,
                    0f,
                    bubbleBottom * 0.46f,
                  )
                  close()
                }
              drawPath(
                path = glossPath,
                brush =
                  Brush.verticalGradient(
                    colors =
                      if (isMine) {
                        listOf(
                          Color.White.copy(alpha = 0.58f),
                          Color.White.copy(alpha = 0.16f),
                        )
                      } else {
                        listOf(
                          Color.White.copy(alpha = 0.28f),
                          Color.White.copy(alpha = 0.05f),
                        )
                      },
                    startY = 0f,
                    endY = bubbleBottom * 0.58f,
                  ),
              )
            }

            drawPath(
              path = bubblePath,
              color = if (isMine) Color(0xFF4CCFFF) else FrostBubbleBorder,
              style = Stroke(width = 1.dp.toPx()),
            )
          }
          .clickable(onClick = onReply)
          .padding(
            start = if (isMine) 15.dp else 20.dp,
            end = if (isMine) 20.dp else 15.dp,
            top = 10.dp,
            bottom = 17.dp,
          )
          .testTag("chat_bubble_${message.id}"),
    ) {
      Column {
        if (!message.replyToText.isNullOrBlank()) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color =
              if (isMine) Color.White.copy(alpha = 0.22f)
              else Color.Black.copy(alpha = 0.28f),
            border =
              BorderStroke(
                width = 1.dp,
                color =
                  if (isMine) Color.White.copy(alpha = 0.40f)
                  else CustardPink.copy(alpha = 0.45f),
              ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
          ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
              Text(
                text = "Replying to ${message.replyToSender ?: "Fan"}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isMine) Color.White else CustardPink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = message.replyToText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isMine) Color.White.copy(alpha = 0.92f) else SlendyInkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        Text(
          text = message.text,
          style =
            MaterialTheme.typography.bodyLarge.copy(
              shadow =
                Shadow(
                  color = Color.Black.copy(alpha = if (isMine) 0.42f else 0.55f),
                  offset = Offset(0f, 1f),
                  blurRadius = 2f,
                )
            ),
          color = if (isMine) Color.White else SlendyInkLight,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = if (isMine) Color.White.copy(alpha = 0.9f) else SlendyInkMuted,
          )
          Spacer(modifier = Modifier.width(4.dp))
          IconButton(
            onClick = onReply,
            modifier = Modifier.size(28.dp),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Reply,
              contentDescription = "Reply to message",
              tint = if (isMine) Color.White.copy(alpha = 0.9f) else SlendyInkMuted,
              modifier = Modifier.size(15.dp),
            )
          }
          if (onDelete != null) {
            IconButton(
              onClick = onDelete,
              modifier = Modifier.size(28.dp).testTag("delete_msg_${message.id}"),
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete your message",
                tint = if (isMine) Color.White.copy(alpha = 0.9f) else SlendyInkMuted,
                modifier = Modifier.size(15.dp),
              )
            }
          }
        }
      }
    }
  }
}

private fun build2013BubblePath(
  width: Float,
  bubbleBottom: Float,
  totalHeight: Float,
  cornerRadius: Float,
  tailInset: Float,
  tailOnRight: Boolean,
  offsetY: Float = 0f,
): Path {
  return Path().apply {
    if (tailOnRight) {
      val rightEdge = width - tailInset * 0.45f
      val rect = Rect(0f, offsetY, rightEdge, bubbleBottom + offsetY)
      addRoundRect(
        RoundRect(
          rect = rect,
          topLeft = CornerRadius(cornerRadius, cornerRadius),
          topRight = CornerRadius(cornerRadius, cornerRadius),
          bottomLeft = CornerRadius(cornerRadius, cornerRadius),
          bottomRight = CornerRadius(cornerRadius * 0.72f, cornerRadius * 0.72f),
        )
      )
      val tailPath =
        Path().apply {
          moveTo(rightEdge - cornerRadius * 0.95f, bubbleBottom + offsetY - 1f)
          quadraticTo(
            rightEdge - cornerRadius * 0.25f,
            totalHeight + offsetY - 1f,
            width,
            totalHeight + offsetY,
          )
          quadraticTo(
            rightEdge - 1f,
            bubbleBottom + offsetY + 2f,
            rightEdge - 2f,
            bubbleBottom + offsetY - cornerRadius * 0.45f,
          )
          close()
        }
      addPath(tailPath)
    } else {
      val leftEdge = tailInset * 0.45f
      val rect = Rect(leftEdge, offsetY, width, bubbleBottom + offsetY)
      addRoundRect(
        RoundRect(
          rect = rect,
          topLeft = CornerRadius(cornerRadius, cornerRadius),
          topRight = CornerRadius(cornerRadius, cornerRadius),
          bottomLeft = CornerRadius(cornerRadius * 0.72f, cornerRadius * 0.72f),
          bottomRight = CornerRadius(cornerRadius, cornerRadius),
        )
      )
      val tailPath =
        Path().apply {
          moveTo(leftEdge + cornerRadius * 0.95f, bubbleBottom + offsetY - 1f)
          quadraticTo(
            leftEdge + cornerRadius * 0.25f,
            totalHeight + offsetY - 1f,
            0f,
            totalHeight + offsetY,
          )
          quadraticTo(
            leftEdge + 1f,
            bubbleBottom + offsetY + 2f,
            leftEdge + 2f,
            bubbleBottom + offsetY - cornerRadius * 0.45f,
          )
          close()
        }
      addPath(tailPath)
    }
  }
}

private fun formatBubbleTime(date: Date?): String {
  if (date == null) return "sending..."
  return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}
