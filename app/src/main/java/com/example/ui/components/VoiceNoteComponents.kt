package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.NeonTheme

@Composable
fun VoiceNoteBubble(
    message: ChatMessage,
    theme: NeonTheme,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSec = if (message.durationSeconds > 0) message.durationSeconds else 5
    val currentSec = (totalSec * progress).toInt()

    Surface(
        color = Color(0xFF141724),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.testTag("voice_note_bubble_${message.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Surface(
                    color = theme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = theme.primary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VOICE NOTE",
                            color = theme.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (isPlaying) String.format("%02d:%02d / %02d:%02d", currentSec / 60, currentSec % 60, totalSec / 60, totalSec % 60)
                    else String.format("%02d:%02d", totalSec / 60, totalSec % 60),
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Controls & Audio Waveform Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(theme.primary)
                        .testTag("voice_note_play_btn_${message.id}")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Voice Note" else "Play Voice Note",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Simulated Interactive Waveform Bars
                val barCount = 24
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onTogglePlay() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (i in 0 until barCount) {
                        val barFraction = i.toFloat() / barCount.toFloat()
                        val isPlayed = barFraction <= progress
                        // Deterministic pseudo-waveform heights
                        val barHeight = 8 + ((i * 7 + (message.id.hashCode() % 13)) % 20)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (isPlayed) theme.primary else Color(0xFF32384D)
                                )
                        )
                    }
                }
            }

            if (message.text.isNotBlank() && !message.text.startsWith("Voice note (")) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message.text,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun VoiceRecordingBar(
    elapsedSeconds: Int,
    theme: NeonTheme,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recDotAlpha"
    )

    Surface(
        color = Color(0xFF141724),
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_recording_active_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Cancel Button
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF281822))
                    .testTag("voice_record_cancel_btn")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Cancel Recording",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Pulsing Record Indicator & Elapsed Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = alphaAnim))
                )
                Text(
                    text = "RECORDING",
                    color = Color.Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Animated mini waveform
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.width(60.dp)
            ) {
                for (i in 0 until 8) {
                    val h = 6 + ((elapsedSeconds * 4 + i * 5) % 18)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(h.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(theme.primary)
                    )
                }
            }

            // Send Voice Note Button
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676))
                    .testTag("voice_record_send_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Voice Note",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
