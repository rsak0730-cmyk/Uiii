package com.example.model

import java.util.UUID

data class Voicemail(
    val id: String = UUID.randomUUID().toString(),
    val callerName: String,
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 18,
    val transcript: String,
    val isUnread: Boolean = true,
    val waveformAmplitudes: List<Float> = listOf(
        0.3f, 0.6f, 0.9f, 0.4f, 0.8f, 0.2f, 0.7f, 1.0f, 0.5f, 0.9f,
        0.6f, 0.3f, 0.7f, 0.8f, 0.4f, 0.2f, 0.6f, 0.9f, 0.5f, 0.3f
    )
)
