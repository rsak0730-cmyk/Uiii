package com.example.model

import java.util.UUID

enum class MessageRole {
    USER,
    AGENT,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionBadge: String? = null,
    val isVoice: Boolean = false,
    val isVoiceNote: Boolean = false,
    val audioFilePath: String? = null,
    val durationSeconds: Int = 0,
    val amplitudes: List<Int> = emptyList(),
    val actionType: ActionType? = null,
    val actionPayload: String? = null
)

enum class ActionType {
    OPEN_APP,
    MAKE_CALL,
    SEND_MESSAGE,
    WATCHDOG_INSPECT,
    VOICEMAIL_CHECK,
    INTERACT_MEDIA,
    INTERACT_SCROLL,
    SEARCH_QUERY
}
