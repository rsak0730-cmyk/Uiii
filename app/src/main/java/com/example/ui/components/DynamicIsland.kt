package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DynamicIslandConfig
import com.example.model.DynamicIslandStatus
import com.example.model.NeonTheme

@Composable
fun DynamicIsland(
    status: DynamicIslandStatus,
    statusDetail: String,
    theme: NeonTheme,
    config: DynamicIslandConfig,
    onIslandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!config.isEnabled) return

    var isExpanded by remember { mutableStateOf(false) }

    // Pulse animation for active states
    val infiniteTransition = rememberInfiniteTransition(label = "island_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(config.animationDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val iconVector: ImageVector = when (status) {
        DynamicIslandStatus.IDLE -> Icons.Default.SmartToy
        DynamicIslandStatus.LISTENING -> Icons.Default.Mic
        DynamicIslandStatus.PROCESSING -> Icons.Default.Psychology
        DynamicIslandStatus.EXECUTING -> Icons.Default.Bolt
        DynamicIslandStatus.DIALING -> Icons.Default.PhoneInTalk
        DynamicIslandStatus.VOICEMAIL -> Icons.Default.Voicemail
        DynamicIslandStatus.WATCHDOG -> Icons.Default.Visibility
    }

    Box(
        modifier = modifier
            .offset(x = config.offsetX.dp, y = config.offsetY.dp)
            .padding(horizontal = 8.dp)
            .testTag("dynamic_island_container"),
        contentAlignment = Alignment.TopCenter
    ) {
        val shape = RoundedCornerShape(config.cornerRadiusDp.dp)

        Box(
            modifier = Modifier
                .widthIn(min = if (isExpanded) 330.dp else config.widthDp.dp, max = 360.dp)
                .animateContentSize(
                    animationSpec = tween(config.animationDurationMs, easing = FastOutSlowInEasing)
                )
                .shadow(
                    elevation = if (status.pulseActive) 16.dp else 8.dp,
                    shape = shape,
                    spotColor = theme.primary,
                    ambientColor = theme.primary
                )
                .clip(shape)
                .background(Color(0xFF0A0C10))
                .border(
                    width = if (status.pulseActive) 1.5.dp else 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            theme.primary.copy(alpha = if (status.pulseActive) 0.9f else 0.4f),
                            Color.White.copy(alpha = 0.2f),
                            theme.accent.copy(alpha = if (status.pulseActive) 0.9f else 0.4f)
                        )
                    ),
                    shape = shape
                )
                .clickable {
                    isExpanded = !isExpanded
                    onIslandClick()
                }
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("dynamic_island_pill"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left indicator dot or icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(if (status.pulseActive) (12 * pulseScale).dp else 10.dp)
                                .clip(CircleShape)
                                .background(if (status.pulseActive) theme.primary else Color(0xFF555B6E))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = iconVector,
                            contentDescription = status.label,
                            tint = if (status.pulseActive) theme.primary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Main label
                    Text(
                        text = status.label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(horizontal = 8.dp)
                    )

                    // Right small waveform / badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(3) { index ->
                            val barHeight = if (status.pulseActive) {
                                (6 + (index * 4) * pulseScale).coerceIn(4f, 16f)
                            } else {
                                4f
                            }
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(barHeight.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(theme.primary.copy(alpha = 0.8f))
                            )
                        }
                    }
                }

                // Expanded info drawer
                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "AGENT INTELLIGENCE HUB",
                            color = theme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (statusDetail.isNotBlank()) statusDetail else "Omni Agent monitoring live device events. Tap anywhere to toggle voice listening or review watchdog alerts.",
                            color = Color(0xFFD1D5DB),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
