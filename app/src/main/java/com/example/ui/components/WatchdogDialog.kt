package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NeonTheme
import com.example.model.UiDesignStyle

@Composable
fun WatchdogDialog(
    screenContextText: String,
    onExecuteAction: (String) -> Unit,
    onDismiss: () -> Unit,
    theme: NeonTheme,
    uiStyle: UiDesignStyle
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AGENT WATCHDOG • LIVE SCREEN",
                    color = theme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Live Screen context badge
                Surface(
                    color = Color(0xFF10131E),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262A3B))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("DETECTED LIVE SURFACE:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = screenContextText,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }

                // AI Prediction & Highlight
                Text("AGENT PREDICTIONS & HIGHLIGHTS:", color = theme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                WatchdogActionRow(
                    title = "Highlight Search Input",
                    description = "Focus keyboard on active search container",
                    icon = Icons.Default.Highlight,
                    theme = theme,
                    onClick = {
                        onExecuteAction("Highlighted Search Box on active screen")
                        onDismiss()
                    }
                )

                WatchdogActionRow(
                    title = "Auto-Scroll Feed / Reel",
                    description = "Dispatches smooth scroll trigger to next item",
                    icon = Icons.Default.SwipeVertical,
                    theme = theme,
                    onClick = {
                        onExecuteAction("Scrolled to next feed item")
                        onDismiss()
                    }
                )

                WatchdogActionRow(
                    title = "Extract Phone / Contact",
                    description = "Scans screen text for phone numbers to dial",
                    icon = Icons.AutoMirrored.Filled.PhoneCallback,
                    theme = theme,
                    onClick = {
                        onExecuteAction("Dialed detected phone number")
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
            ) {
                Text("Close Watchdog", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF141724)
    )
}

@Composable
private fun WatchdogActionRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: NeonTheme,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF191D2C),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = theme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(description, color = Color.LightGray, fontSize = 10.sp)
            }
        }
    }
}
