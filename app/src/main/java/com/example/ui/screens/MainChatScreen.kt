package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.agent.AgentActionController
import com.example.model.*
import com.example.audio.VoiceNotePlayer
import com.example.audio.VoiceNoteRecorder
import com.example.ui.components.ContactsListDialog
import com.example.ui.components.DynamicIsland
import com.example.ui.components.StylizedText
import com.example.ui.components.UiStyleCard
import com.example.ui.components.VoiceNoteBubble
import com.example.ui.components.VoiceRecordingBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatScreen(
    messages: List<ChatMessage>,
    onSendMessage: (String, Boolean) -> Unit,
    onSendVoiceNote: (File, Int, String?) -> Unit,
    onClearChat: () -> Unit,
    theme: NeonTheme,
    uiStyle: UiDesignStyle,
    textEffect: TextEffect,
    islandConfig: DynamicIslandConfig,
    islandStatus: DynamicIslandStatus,
    islandDetail: String,
    actionController: AgentActionController,
    contactSelectionDialog: List<ContactNumberRow>?,
    onConfirmRowNumber: (ContactNumberRow) -> Unit,
    onDismissContactDialog: () -> Unit,
    showContactsDialog: Boolean,
    onOpenContactsDialog: () -> Unit,
    onCloseContactsDialog: () -> Unit,
    onCallContact: (String, String) -> Unit,
    onSmsContact: (String, String) -> Unit,
    voiceNotePlayer: VoiceNotePlayer,
    voiceNoteRecorder: VoiceNoteRecorder,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Voice Note Recording State
    var isRecordingVoiceNote by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }
    var targetContactForVoiceNote by remember { mutableStateOf<String?>(null) }

    // Playback state from voice note player
    val playbackState by voiceNotePlayer.playbackState.collectAsState()

    // Recording duration timer
    LaunchedEffect(isRecordingVoiceNote) {
        if (isRecordingVoiceNote) {
            recordingDurationSec = 0
            while (isRecordingVoiceNote) {
                delay(1000)
                recordingDurationSec++
            }
        }
    }

    // Audio recording permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = voiceNoteRecorder.startRecording()
            if (file != null) {
                isRecordingVoiceNote = true
                recordingDurationSec = 0
            }
        }
    }

    // Scroll to bottom when message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher for contacts
    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onSendMessage("Contacts permission granted. Finding contacts...", false)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF090A10),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090A10))
            ) {
                // Top bar row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top-left: Creator Instagram ID hidden behind agent title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                actionController.openInstagramProfile("edit.og_")
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("creator_instagram_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = "Instagram @edit.og_",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StylizedText(
                                    text = "Agent Omni",
                                    effect = textEffect,
                                    theme = theme,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open Instagram Profile",
                                    tint = theme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = "creator: @edit.og_",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Top-right actions: Friends & Family Contacts + Delete Full Chat
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onOpenContactsDialog,
                            modifier = Modifier
                                .testTag("top_bar_contacts_button")
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.primary.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Friends & Family Contacts",
                                tint = theme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .testTag("delete_full_chat_button")
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x22FF1744))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Delete Full Chat",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Dynamic Island pill
                DynamicIsland(
                    status = islandStatus,
                    statusDetail = islandDetail,
                    theme = theme,
                    config = islandConfig,
                    onIslandClick = {
                        isListening = !isListening
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Quick suggestions carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickChip("Friends & Family", Icons.Default.People, theme) {
                    onOpenContactsDialog()
                }
                QuickChip("Voice Note", Icons.Default.GraphicEq, theme) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        val file = voiceNoteRecorder.startRecording()
                        if (file != null) {
                            isRecordingVoiceNote = true
                            recordingDurationSec = 0
                        }
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
                QuickChip("Open YouTube", Icons.Default.SmartDisplay, theme) {
                    onSendMessage("Open YouTube", false)
                }
                QuickChip("Call Contact", Icons.Default.Call, theme) {
                    onOpenContactsDialog()
                }
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        theme = theme,
                        uiStyle = uiStyle,
                        isVoicePlaying = (playbackState.messageId == message.id && playbackState.isPlaying),
                        voiceProgress = if (playbackState.messageId == message.id) playbackState.progress else 0f,
                        onTogglePlayVoice = {
                            voiceNotePlayer.togglePlay(message.id, message.audioFilePath, message.durationSeconds)
                        },
                        onOpenInstagram = { actionController.openInstagramProfile("edit.og_") }
                    )
                }
            }

            // Voice Listening Banner if active
            AnimatedVisibility(visible = isListening) {
                VoiceWaveBanner(
                    theme = theme,
                    onStop = {
                        isListening = false
                        onSendMessage("Send SMS to Emma telling her I'm on the way", true)
                    }
                )
            }

            // Bottom Input Bar & Voice Recording Bar
            Surface(
                color = Color(0xFF0F1118),
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRecordingVoiceNote) {
                    VoiceRecordingBar(
                        elapsedSeconds = recordingDurationSec,
                        theme = theme,
                        onCancel = {
                            voiceNoteRecorder.cancelRecording()
                            isRecordingVoiceNote = false
                            targetContactForVoiceNote = null
                        },
                        onSend = {
                            val (file, dur) = voiceNoteRecorder.stopRecording()
                            if (file != null) {
                                onSendVoiceNote(file, dur, targetContactForVoiceNote)
                            }
                            isRecordingVoiceNote = false
                            targetContactForVoiceNote = null
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // AI Voice Listening button
                        IconButton(
                            onClick = { isListening = !isListening },
                            modifier = Modifier
                                .testTag("voice_listen_button")
                                .clip(CircleShape)
                                .background(if (isListening) theme.primary else Color(0xFF1E222D))
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = "Toggle Voice Input",
                                tint = if (isListening) Color.Black else theme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Quick Voice Note Recorder button
                        IconButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    val file = voiceNoteRecorder.startRecording()
                                    if (file != null) {
                                        isRecordingVoiceNote = true
                                        recordingDurationSec = 0
                                    }
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .testTag("record_voice_note_button")
                                .clip(CircleShape)
                                .background(Color(0xFF14241C))
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Record Voice Note",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Text input field
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    "Ask Omni or record voice note...",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF161922),
                                unfocusedContainerColor = Color(0xFF161922),
                                focusedBorderColor = theme.primary,
                                unfocusedBorderColor = Color(0xFF2A2E3D)
                            ),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text_field"),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Button
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onSendMessage(textInput, false)
                                    textInput = ""
                                }
                            },
                            enabled = textInput.isNotBlank(),
                            modifier = Modifier
                                .testTag("chat_send_button")
                                .clip(CircleShape)
                                .background(
                                    if (textInput.isNotBlank()) theme.primary else Color(0xFF222634)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = if (textInput.isNotBlank()) Color.Black else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Contacts List Dialog with top search bar
    if (showContactsDialog) {
        ContactsListDialog(
            contacts = actionController.findDeviceContacts(""),
            theme = theme,
            uiStyle = uiStyle,
            onCallNumber = onCallContact,
            onSendSms = onSmsContact,
            onRecordVoiceNoteForContact = { contact ->
                targetContactForVoiceNote = contact.name
                onCloseContactsDialog()
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    val file = voiceNoteRecorder.startRecording()
                    if (file != null) {
                        isRecordingVoiceNote = true
                        recordingDurationSec = 0
                    }
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onDismiss = onCloseContactsDialog
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Full Chat?", color = Color.White) },
            text = { Text("All conversation history with Omni Agent will be permanently erased.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChat()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF161924)
        )
    }

    // Multi-number row selection dialog
    if (contactSelectionDialog != null && contactSelectionDialog.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onDismissContactDialog,
            title = {
                Text(
                    text = "Multiple Numbers Found for ${contactSelectionDialog.first().contactName}",
                    color = theme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Please choose a row number to make call:",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    contactSelectionDialog.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E222D))
                                .clickable { onConfirmRowNumber(row) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(theme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${row.rowNumber}",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = row.phoneNumber, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(text = row.label, color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                            Icon(imageVector = Icons.Default.Phone, contentDescription = "Dial", tint = theme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissContactDialog) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF12141F)
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    theme: NeonTheme,
    uiStyle: UiDesignStyle,
    isVoicePlaying: Boolean = false,
    voiceProgress: Float = 0f,
    onTogglePlayVoice: () -> Unit = {},
    onOpenInstagram: () -> Unit
) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(theme.primary)
                    .clickable { onOpenInstagram() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Agent Omni (@edit.og_)",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.widthIn(max = 290.dp)) {
            if (message.isVoiceNote) {
                VoiceNoteBubble(
                    message = message,
                    theme = theme,
                    isPlaying = isVoicePlaying,
                    progress = voiceProgress,
                    onTogglePlay = onTogglePlayVoice
                )
            } else {
                UiStyleCard(
                    style = uiStyle,
                    theme = theme,
                    cornerRadius = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (message.actionBadge != null) {
                            Surface(
                                color = theme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = theme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = message.actionBadge,
                                        color = theme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = message.text,
                            color = if (isUser) Color.White else Color(0xFFECEFF1),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )

                        if (message.isVoice) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = theme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Voice command captured",
                                    color = theme.primary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2E3D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun QuickChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: NeonTheme,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF161922),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.35f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = theme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun VoiceWaveBanner(
    theme: NeonTheme,
    onStop: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val waveScale by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_scale"
    )

    Surface(
        color = Color(0xFF141724),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.primary, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .width(4.dp)
                            .height((8 + (i * 6) * waveScale).coerceIn(4f, 26f).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(theme.primary)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Agent Listening via Mic...", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Say an app name or contact to execute", color = Color.Gray, fontSize = 11.sp)
                }
            }

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Process", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
