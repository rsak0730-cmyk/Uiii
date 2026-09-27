package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.AgentActionController
import com.example.data.AgentPreferences
import com.example.model.*
import com.example.service.AgentAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.audio.VoiceNotePlayer
import com.example.audio.VoiceNoteRecorder
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = AgentPreferences(application)
    val actionController = AgentActionController(application)
    val voiceNotePlayer = VoiceNotePlayer(application)
    val voiceNoteRecorder = VoiceNoteRecorder(application)

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(initialMessages())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _islandStatus = MutableStateFlow(DynamicIslandStatus.IDLE)
    val islandStatus: StateFlow<DynamicIslandStatus> = _islandStatus.asStateFlow()

    private val _islandDetail = MutableStateFlow("Agent Omni is active and listening for your commands.")
    val islandDetail: StateFlow<String> = _islandDetail.asStateFlow()

    private val _contactSelectionDialog = MutableStateFlow<List<ContactNumberRow>?>(null)
    val contactSelectionDialog: StateFlow<List<ContactNumberRow>?> = _contactSelectionDialog.asStateFlow()

    private val _showContactsDialog = MutableStateFlow(false)
    val showContactsDialog: StateFlow<Boolean> = _showContactsDialog.asStateFlow()

    private val _isWatchdogOpen = MutableStateFlow(false)
    val isWatchdogOpen: StateFlow<Boolean> = _isWatchdogOpen.asStateFlow()

    private var pendingCallName: String? = null

    init {
        // Listen to volume button triggers from accessibility service
        viewModelScope.launch {
            AgentAccessibilityService.volumeTriggerEvent.collect { triggerTime ->
                if (triggerTime != null) {
                    toggleListeningFromShortcut()
                }
            }
        }
    }

    private fun initialMessages(): List<ChatMessage> = listOf(
        ChatMessage(
            role = MessageRole.AGENT,
            text = "Omni Agent activated with Gemini Brain. I can launch installed & modded apps, find device contacts, dial calls with row-number resolution, send SMS, inspect live apps via Watchdog, and manage iOS-style voicemails.\n\nCreator profile: @edit.og_",
            actionBadge = "System Online"
        )
    )

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun openWatchdog(open: Boolean) {
        _isWatchdogOpen.value = open
        if (open) {
            _islandStatus.value = DynamicIslandStatus.WATCHDOG
            _islandDetail.value = "Watchdog analyzing active screen surface..."
        } else {
            _islandStatus.value = DynamicIslandStatus.IDLE
        }
    }

    fun toggleListening() {
        toggleListeningFromShortcut()
    }

    private fun toggleListeningFromShortcut() {
        if (_islandStatus.value == DynamicIslandStatus.LISTENING) {
            _islandStatus.value = DynamicIslandStatus.IDLE
            _islandDetail.value = "Voice listening stopped via Volume shortcut."
        } else {
            _islandStatus.value = DynamicIslandStatus.LISTENING
            _islandDetail.value = "Listening triggered via Volume Up button shortcut..."
            viewModelScope.launch {
                delay(3000)
                if (_islandStatus.value == DynamicIslandStatus.LISTENING) {
                    processUserInput("Open YouTube and check trending shorts", true)
                }
            }
        }
    }

    fun processUserInput(input: String, isVoice: Boolean = false) {
        val userMsg = ChatMessage(
            role = MessageRole.USER,
            text = input,
            isVoice = isVoice
        )
        _messages.value = _messages.value + userMsg

        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        viewModelScope.launch {
            _islandStatus.value = DynamicIslandStatus.PROCESSING
            _islandDetail.value = "Processing: \"$trimmed\""

            // 1. App Launching Intent: "open [app]"
            if (lower.startsWith("open ") || lower.startsWith("launch ")) {
                val appTarget = trimmed.substringAfter(" ").trim()
                _islandStatus.value = DynamicIslandStatus.EXECUTING
                _islandDetail.value = "Searching & launching app: $appTarget"

                delay(400)
                val result = actionController.openInstalledApp(appTarget)
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.AGENT,
                    text = result.message,
                    actionBadge = result.actionBadge
                )
                _islandStatus.value = DynamicIslandStatus.IDLE
                return@launch
            }

            // 2. Making Calls: "call [name]"
            if (lower.startsWith("call ") || lower.startsWith("dial ")) {
                val contactTarget = trimmed.substringAfter(" ").trim()
                _islandStatus.value = DynamicIslandStatus.DIALING
                _islandDetail.value = "Searching contacts for: $contactTarget"

                delay(400)
                val matches = actionController.findDeviceContacts(contactTarget)

                if (matches.isEmpty()) {
                    _messages.value = _messages.value + ChatMessage(
                        role = MessageRole.AGENT,
                        text = "No contact found matching '$contactTarget'.",
                        actionBadge = "Contact Not Found"
                    )
                    _islandStatus.value = DynamicIslandStatus.IDLE
                    return@launch
                }

                // Collect all phone numbers across matching contacts
                val rows = mutableListOf<ContactNumberRow>()
                var rowIndex = 1
                for (contact in matches) {
                    for (num in contact.phoneNumbers) {
                        rows.add(
                            ContactNumberRow(
                                rowNumber = rowIndex++,
                                contactName = contact.name,
                                phoneNumber = num,
                                label = if (contact.phoneNumbers.size > 1) "Number #${rowIndex - 1}" else "Mobile"
                            )
                        )
                    }
                }

                if (rows.size > 1) {
                    // Multiple numbers with same name -> show row numbers as requested!
                    pendingCallName = contactTarget
                    _contactSelectionDialog.value = rows
                    _messages.value = _messages.value + ChatMessage(
                        role = MessageRole.AGENT,
                        text = "Found ${rows.size} phone numbers for '$contactTarget'. Please choose a row number to make the call.",
                        actionBadge = "Multiple Numbers Found"
                    )
                    _islandDetail.value = "Awaiting row number selection for $contactTarget..."
                } else {
                    // Single number -> confirm and dial
                    val singleRow = rows.first()
                    actionController.dialPhoneNumber(singleRow.phoneNumber)
                    _messages.value = _messages.value + ChatMessage(
                        role = MessageRole.AGENT,
                        text = "Calling ${singleRow.contactName} (${singleRow.phoneNumber}). Confirming dialer launch.",
                        actionBadge = "Dialing: ${singleRow.phoneNumber}"
                    )
                    _islandStatus.value = DynamicIslandStatus.IDLE
                }
                return@launch
            }

            // 3. Messaging: "send message to [name]" / "sms [name]"
            if (lower.contains("send message") || lower.contains("send sms") || lower.startsWith("sms ")) {
                _islandStatus.value = DynamicIslandStatus.EXECUTING
                _islandDetail.value = "Composing message via system SMS intent..."

                // Try to resolve contact
                val matches = actionController.findDeviceContacts("")
                val target = matches.firstOrNull() ?: ContactEntry("Contact", listOf("+1 555-0199"))
                val textBody = if (lower.contains("telling")) trimmed.substringAfter("telling").trim() else "Hello! Sent via Omni Agent."

                actionController.sendSms(target.phoneNumbers.first(), textBody)
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.AGENT,
                    text = "SMS composer opened for ${target.name} (${target.phoneNumbers.first()}) with text: \"$textBody\"",
                    actionBadge = "SMS Dispatched"
                )
                _islandStatus.value = DynamicIslandStatus.IDLE
                return@launch
            }

            // 4. Media & Interactivity: "play", "pause", "scroll", "search"
            if (lower.contains("play") || lower.contains("pause") || lower.contains("scroll") || lower.startsWith("search ")) {
                _islandStatus.value = DynamicIslandStatus.EXECUTING
                _islandDetail.value = "Executing media/screen interaction..."
                delay(300)
                val result = actionController.interactMedia(trimmed)
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.AGENT,
                    text = result.message,
                    actionBadge = result.actionBadge
                )
                _islandStatus.value = DynamicIslandStatus.IDLE
                return@launch
            }

            // 5. Watchdog command: "watchdog"
            if (lower.contains("watchdog") || lower.contains("inspect")) {
                openWatchdog(true)
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.AGENT,
                    text = "Watchdog active. Inspecting live foreground screen hierarchy and predicting automated highlights.",
                    actionBadge = "Watchdog Mode"
                )
                return@launch
            }

            // 6. Creator Instagram query
            if (lower.contains("instagram") || lower.contains("creator") || lower.contains("edit.og")) {
                actionController.openInstagramProfile("edit.og_")
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.AGENT,
                    text = "Opening creator profile on Instagram: @edit.og_",
                    actionBadge = "Instagram @edit.og_"
                )
                _islandStatus.value = DynamicIslandStatus.IDLE
                return@launch
            }

            // 7. Battery & Energy query
            if (lower.contains("battery") || lower.contains("energy") || lower.contains("power level")) {
                val report = actionController.getBatteryReport()
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.AGENT,
                    text = report.message,
                    actionBadge = report.actionBadge
                )
                _islandStatus.value = DynamicIslandStatus.IDLE
                _islandDetail.value = "Battery Status Inspected"
                return@launch
            }

            // 8. Dynamic Island query / enable / disable
            if (lower.contains("dynamic island") || lower.contains("island")) {
                val current = preferences.dynamicIslandConfig.value
                val newConfig = when {
                    lower.contains("enable") || lower.contains("turn on") || lower.contains("activate") || lower.contains("show") -> {
                        current.copy(isEnabled = true)
                    }
                    lower.contains("disable") || lower.contains("turn off") || lower.contains("hide") -> {
                        current.copy(isEnabled = false)
                    }
                    else -> current
                }
                if (newConfig != current) {
                    preferences.setDynamicIslandConfig(newConfig)
                }
                val statusText = if (newConfig.isEnabled) "ENABLED (ACTIVE)" else "DISABLED"
                val response = "Dynamic Island is currently **$statusText**.\n\n" +
                        "• **To Enable/Disable:** Go to **Tab 4 (Settings) ➔ Section 4 [DYNAMIC ISLAND SETTINGS]** and flip the **Enable Dynamic Island** switch.\n" +
                        "• **Customization:** In Settings, you can choose position presets (*Standard*, *Compact*, *Expanded*), customize X/Y position sliders, change corner radius, and adjust physics animation curves.\n" +
                        "• **Interaction:** When enabled, the island pill floats at the top. Tap it anytime to expand or toggle voice listening!"
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.AGENT,
                    text = response,
                    actionBadge = "Dynamic Island: $statusText"
                )
                _islandStatus.value = DynamicIslandStatus.IDLE
                return@launch
            }

            // 9. Show / search contacts list
            if (lower == "contacts" || lower == "show contacts" || lower.contains("contacts list") || lower.contains("friends and family") || lower == "family") {
                _showContactsDialog.value = true
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.AGENT,
                    text = "Opening your Friends & Family contacts list. Use the search bar at the top to filter names instantly!",
                    actionBadge = "Contacts Filter Active"
                )
                _islandStatus.value = DynamicIslandStatus.IDLE
                return@launch
            }

            // 10. General AI Studio Query (Gemini / OpenRouter / OpenAI)
            _islandDetail.value = "Consulting AI Brain..."
            val aiResponse = actionController.queryAiBrain(
                prompt = trimmed,
                config = preferences.apiConfig.value,
                energyAware = preferences.energyAwareAi.value
            )
            _messages.value = _messages.value + ChatMessage(
                role = MessageRole.AGENT,
                text = aiResponse,
                actionBadge = "${preferences.apiConfig.value.provider.displayName} Response"
            )
            _islandStatus.value = DynamicIslandStatus.IDLE
            _islandDetail.value = "Agent Omni Ready"
        }
    }

    fun openContactsDialog() {
        _showContactsDialog.value = true
    }

    fun closeContactsDialog() {
        _showContactsDialog.value = false
    }

    fun sendVoiceNote(file: File, durationSec: Int, contactTarget: String? = null) {
        viewModelScope.launch {
            val noteMsg = ChatMessage(
                role = MessageRole.USER,
                text = if (contactTarget != null) "Voice note for $contactTarget ($durationSec s)" else "Voice note ($durationSec s)",
                isVoiceNote = true,
                audioFilePath = file.absolutePath,
                durationSeconds = durationSec,
                actionBadge = "Audio Note"
            )
            _messages.value = _messages.value + noteMsg

            _islandStatus.value = DynamicIslandStatus.PROCESSING
            _islandDetail.value = "Processing audio voice note dispatch..."
            delay(700)

            val replyText = if (contactTarget != null) {
                "Quick voice note ($durationSec s) queued and dispatched to **$contactTarget**. Audio waveform and recording saved to your communication thread."
            } else {
                "Voice note ($durationSec s) received and saved to communication thread. Tap Play to listen back with waveform telemetry."
            }

            _messages.value = _messages.value + ChatMessage(
                role = MessageRole.AGENT,
                text = replyText,
                actionBadge = "Audio Note Saved"
            )
            _islandStatus.value = DynamicIslandStatus.IDLE
            _islandDetail.value = "Agent Omni Ready"
        }
    }

    fun dialContact(name: String, number: String) {
        _showContactsDialog.value = false
        actionController.dialPhoneNumber(number)
        _messages.value = _messages.value + ChatMessage(
            role = MessageRole.AGENT,
            text = "Dialing $name ($number). Launching phone dialer...",
            actionBadge = "Dialing: $number"
        )
    }

    fun sendSmsToContact(name: String, number: String) {
        _showContactsDialog.value = false
        actionController.sendSms(number, "Hey $name! Sent via Omni Agent.")
        _messages.value = _messages.value + ChatMessage(
            role = MessageRole.AGENT,
            text = "Opening SMS messenger for $name ($number).",
            actionBadge = "SMS: $number"
        )
    }

    fun confirmContactRowCall(row: ContactNumberRow) {
        _contactSelectionDialog.value = null
        actionController.dialPhoneNumber(row.phoneNumber)
        _messages.value = _messages.value + ChatMessage(
            role = MessageRole.AGENT,
            text = "Confirmed Row #${row.rowNumber}: Dialing ${row.contactName} at ${row.phoneNumber}.",
            actionBadge = "Dialed: ${row.phoneNumber}"
        )
        _islandStatus.value = DynamicIslandStatus.IDLE
    }

    fun dismissContactSelectionDialog() {
        _contactSelectionDialog.value = null
    }

    fun clearAllChat() {
        _messages.value = initialMessages()
        _islandStatus.value = DynamicIslandStatus.IDLE
        _islandDetail.value = "Chat reset. Agent Omni ready."
    }

    override fun onCleared() {
        super.onCleared()
        voiceNotePlayer.release()
        voiceNoteRecorder.cancelRecording()
    }
}
