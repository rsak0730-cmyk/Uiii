package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.model.NeonTheme
import com.example.model.UiDesignStyle
import com.example.model.Voicemail
import com.example.ui.components.UiStyleCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoicemailScreen(
    voicemails: List<Voicemail>,
    onDeleteVoicemail: (String) -> Unit,
    onAddVoicemail: (Voicemail) -> Unit,
    theme: NeonTheme,
    uiStyle: UiDesignStyle,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var activePlayingId by remember { mutableStateOf<String?>(null) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }

    // Simulation states
    var isSimulatingCall by remember { mutableStateOf(false) }
    var ringCountdown by remember { mutableIntStateOf(20) }
    var simulationStep by remember { mutableStateOf("") }

    // Playback ticker
    LaunchedEffect(activePlayingId) {
        if (activePlayingId != null) {
            playbackProgress = 0f
            while (playbackProgress < 1f && activePlayingId != null) {
                delay(100)
                playbackProgress += 0.02f
            }
            activePlayingId = null
            playbackProgress = 0f
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF090A10),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Voicemails",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!isSimulatingCall) {
                                isSimulatingCall = true
                                ringCountdown = 20
                                simulationStep = "Incoming Call from Sophia (+1 555-400-8811)... Ringing (20s)"
                                scope.launch {
                                    while (ringCountdown > 0) {
                                        delay(1000)
                                        ringCountdown--
                                        simulationStep = "Call unanswered ($ringCountdown s remaining)..."
                                    }
                                    simulationStep = "Omni Agent answering: 'User unavailable, recording voicemail...'"
                                    delay(2500)
                                    simulationStep = "Sophia leaving voice message..."
                                    delay(2500)
                                    val newVoicemail = Voicemail(
                                        callerName = "Sophia Hayes",
                                        phoneNumber = "+1 (555) 400-8811",
                                        timestamp = System.currentTimeMillis(),
                                        durationSeconds = 22,
                                        transcript = "Hey! Called your phone, after 20 seconds your Omni Agent answered and prompted me to record this. Let's sync about the project launch!",
                                        isUnread = true
                                    )
                                    onAddVoicemail(newVoicemail)
                                    isSimulatingCall = false
                                }
                            }
                        },
                        modifier = Modifier
                            .testTag("simulate_call_button")
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x2200E676))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddIcCall,
                            contentDescription = "Simulate 20s Incoming Call",
                            tint = Color(0xFF00E676)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090A10))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Simulation banner if active
            AnimatedVisibility(visible = isSimulatingCall) {
                Surface(
                    color = Color(0xFF141926),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            progress = { (20 - ringCountdown) / 20f },
                            color = Color(0xFF00E676),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("iOS Voicemail Engine Active", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(simulationStep, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Info Card
            UiStyleCard(
                style = uiStyle,
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(theme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Voicemail,
                            contentDescription = null,
                            tint = theme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("iOS Intelligent Call Routing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "When an incoming call is unpicked after 20s ring, Omni Agent captures and transcribes the message.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            if (voicemails.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Voicemail,
                            contentDescription = "No voicemails",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No Voicemails Saved", color = Color.Gray, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Tap + above to simulate a 20s missed call recording", color = Color.DarkGray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(voicemails, key = { it.id }) { item ->
                        val isPlaying = activePlayingId == item.id
                        VoicemailItemCard(
                            voicemail = item,
                            isPlaying = isPlaying,
                            playbackProgress = if (isPlaying) playbackProgress else 0f,
                            onToggleListen = {
                                activePlayingId = if (isPlaying) null else item.id
                            },
                            onDelete = { onDeleteVoicemail(item.id) },
                            theme = theme,
                            uiStyle = uiStyle
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoicemailItemCard(
    voicemail: Voicemail,
    isPlaying: Boolean,
    playbackProgress: Float,
    onToggleListen: () -> Unit,
    onDelete: () -> Unit,
    theme: NeonTheme,
    uiStyle: UiDesignStyle
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    UiStyleCard(
        style = uiStyle,
        theme = theme,
        cornerRadius = 14.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (voicemail.isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(theme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = voicemail.callerName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = voicemail.phoneNumber,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateFormat.format(Date(voicemail.timestamp)),
                        color = Color.DarkGray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_voicemail_${voicemail.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Voicemail",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Audio Player Controls & Waveform
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF11141D))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Stop Button with matching audio icon
                IconButton(
                    onClick = onToggleListen,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Color(0xFFFF1744) else theme.primary)
                        .testTag("toggle_listen_${voicemail.id}")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Stop Listening" else "Listen Voicemail",
                        tint = if (isPlaying) Color.White else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Waveform visualization
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                ) {
                    voicemail.waveformAmplitudes.forEachIndexed { index, amp ->
                        val barFraction = index.toFloat() / voicemail.waveformAmplitudes.size
                        val isPlayed = isPlaying && barFraction <= playbackProgress
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height((amp * 26).coerceIn(4f, 26f).dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(
                                    if (isPlayed) theme.primary else Color(0xFF374151)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${voicemail.durationSeconds}s",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transcript text
            Text(
                text = "\"${voicemail.transcript}\"",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
