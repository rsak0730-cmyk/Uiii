package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.AgentAccessibilityService
import com.example.service.BatteryMonitorService
import com.example.ui.components.DynamicIsland
import com.example.ui.components.WatchdogDialog
import com.example.ui.screens.ApiSetupScreen
import com.example.ui.screens.MainChatScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoicemailScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start background battery monitoring service
        try {
            val serviceIntent = Intent(this, BatteryMonitorService::class.java)
            startService(serviceIntent)
        } catch (_: Exception) {
            BatteryMonitorService.updateFromContext(this)
        }

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val activeTab by mainViewModel.activeTab.collectAsStateWithLifecycle()
            val messages by mainViewModel.messages.collectAsStateWithLifecycle()
            val islandStatus by mainViewModel.islandStatus.collectAsStateWithLifecycle()
            val islandDetail by mainViewModel.islandDetail.collectAsStateWithLifecycle()
            val contactSelectionDialog by mainViewModel.contactSelectionDialog.collectAsStateWithLifecycle()
            val isWatchdogOpen by mainViewModel.isWatchdogOpen.collectAsStateWithLifecycle()

            val theme by mainViewModel.preferences.theme.collectAsStateWithLifecycle()
            val uiStyle by mainViewModel.preferences.uiStyle.collectAsStateWithLifecycle()
            val textEffect by mainViewModel.preferences.textEffect.collectAsStateWithLifecycle()
            val islandConfig by mainViewModel.preferences.dynamicIslandConfig.collectAsStateWithLifecycle()
            val apiConfig by mainViewModel.preferences.apiConfig.collectAsStateWithLifecycle()
            val voicemails by mainViewModel.preferences.voicemails.collectAsStateWithLifecycle()
            val isEnergyAwareAi by mainViewModel.preferences.energyAwareAi.collectAsStateWithLifecycle()
            val showContactsDialog by mainViewModel.showContactsDialog.collectAsStateWithLifecycle()
            val screenContextText by AgentAccessibilityService.screenContextText.collectAsStateWithLifecycle()

            // Back handler: return to main chat if in secondary tab
            BackHandler(enabled = activeTab != 0) {
                mainViewModel.setActiveTab(0)
            }

            MyApplicationTheme(neonTheme = theme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF090A10),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0D0F18),
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("bottom_nav_bar")
                        ) {
                            // Tab 1: Main Chat
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { mainViewModel.setActiveTab(0) },
                                icon = {
                                    Icon(
                                        imageVector = if (activeTab == 0) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Main Chat"
                                    )
                                },
                                label = { Text("Chat", fontSize = 11.sp, fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = theme.primary,
                                    indicatorColor = theme.primary,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                ),
                                modifier = Modifier.testTag("nav_tab_chat")
                            )

                            // Tab 2: API Setup
                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { mainViewModel.setActiveTab(1) },
                                icon = {
                                    Icon(
                                        imageVector = if (activeTab == 1) Icons.Default.VpnKey else Icons.Outlined.VpnKey,
                                        contentDescription = "API Setup"
                                    )
                                },
                                label = { Text("API Brain", fontSize = 11.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = theme.primary,
                                    indicatorColor = theme.primary,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                ),
                                modifier = Modifier.testTag("nav_tab_api")
                            )

                            // Tab 3: Voicemails
                            val unreadCount = voicemails.count { it.isUnread }
                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { mainViewModel.setActiveTab(2) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (unreadCount > 0) {
                                                Badge(containerColor = theme.primary, contentColor = Color.Black) {
                                                    Text("$unreadCount")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (activeTab == 2) Icons.Default.Voicemail else Icons.Outlined.Voicemail,
                                            contentDescription = "Voicemails"
                                        )
                                    }
                                },
                                label = { Text("Voicemail", fontSize = 11.sp, fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = theme.primary,
                                    indicatorColor = theme.primary,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                ),
                                modifier = Modifier.testTag("nav_tab_voicemail")
                            )

                            // Tab 4: Settings
                            NavigationBarItem(
                                selected = activeTab == 3,
                                onClick = { mainViewModel.setActiveTab(3) },
                                icon = {
                                    Icon(
                                        imageVector = if (activeTab == 3) Icons.Default.Tune else Icons.Outlined.Tune,
                                        contentDescription = "Settings"
                                    )
                                },
                                label = { Text("Settings", fontSize = 11.sp, fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = theme.primary,
                                    indicatorColor = theme.primary,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                ),
                                modifier = Modifier.testTag("nav_tab_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            0 -> MainChatScreen(
                                messages = messages,
                                onSendMessage = { text, isVoice -> mainViewModel.processUserInput(text, isVoice) },
                                onSendVoiceNote = { file, duration, contact -> mainViewModel.sendVoiceNote(file, duration, contact) },
                                onClearChat = { mainViewModel.clearAllChat() },
                                theme = theme,
                                uiStyle = uiStyle,
                                textEffect = textEffect,
                                islandConfig = islandConfig,
                                islandStatus = islandStatus,
                                islandDetail = islandDetail,
                                actionController = mainViewModel.actionController,
                                contactSelectionDialog = contactSelectionDialog,
                                onConfirmRowNumber = { mainViewModel.confirmContactRowCall(it) },
                                onDismissContactDialog = { mainViewModel.dismissContactSelectionDialog() },
                                showContactsDialog = showContactsDialog,
                                onOpenContactsDialog = { mainViewModel.openContactsDialog() },
                                onCloseContactsDialog = { mainViewModel.closeContactsDialog() },
                                onCallContact = { name, num -> mainViewModel.dialContact(name, num) },
                                onSmsContact = { name, num -> mainViewModel.sendSmsToContact(name, num) },
                                voiceNotePlayer = mainViewModel.voiceNotePlayer,
                                voiceNoteRecorder = mainViewModel.voiceNoteRecorder
                            )
                            1 -> ApiSetupScreen(
                                currentConfig = apiConfig,
                                onSaveConfig = { mainViewModel.preferences.setApiConfig(it) },
                                theme = theme,
                                uiStyle = uiStyle,
                                actionController = mainViewModel.actionController
                            )
                            2 -> VoicemailScreen(
                                voicemails = voicemails,
                                onDeleteVoicemail = { mainViewModel.preferences.deleteVoicemail(it) },
                                onAddVoicemail = { mainViewModel.preferences.addVoicemail(it) },
                                theme = theme,
                                uiStyle = uiStyle
                            )
                            3 -> SettingsScreen(
                                currentTheme = theme,
                                onSelectTheme = { mainViewModel.preferences.setTheme(it) },
                                currentUiStyle = uiStyle,
                                onSelectUiStyle = { mainViewModel.preferences.setUiStyle(it) },
                                currentTextEffect = textEffect,
                                onSelectTextEffect = { mainViewModel.preferences.setTextEffect(it) },
                                islandConfig = islandConfig,
                                onUpdateIslandConfig = { mainViewModel.preferences.setDynamicIslandConfig(it) },
                                isEnergyAwareAi = isEnergyAwareAi,
                                onToggleEnergyAwareAi = { mainViewModel.preferences.setEnergyAwareAi(it) }
                            )
                        }

                        // Floating Dynamic Island on secondary tabs (API Brain & Voicemails)
                        if ((activeTab == 1 || activeTab == 2) && islandConfig.isEnabled) {
                            DynamicIsland(
                                status = islandStatus,
                                statusDetail = islandDetail,
                                theme = theme,
                                config = islandConfig,
                                onIslandClick = {
                                    mainViewModel.toggleListening()
                                },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 4.dp)
                            )
                        }

                        // Watchdog Dialog
                        if (isWatchdogOpen) {
                            WatchdogDialog(
                                screenContextText = screenContextText,
                                onExecuteAction = { actionText ->
                                    mainViewModel.processUserInput(actionText, false)
                                },
                                onDismiss = { mainViewModel.openWatchdog(false) },
                                theme = theme,
                                uiStyle = uiStyle
                            )
                        }
                    }
                }
            }
        }
    }
}
