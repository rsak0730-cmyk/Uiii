package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackState(
    val messageId: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0
)

class VoiceNotePlayer(private val context: Context) {

    companion object {
        private const val TAG = "VoiceNotePlayer"
    }

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun togglePlay(messageId: String, filePath: String?, durationSec: Int = 5) {
        val currentState = _playbackState.value

        if (currentState.messageId == messageId && currentState.isPlaying) {
            pause()
            return
        }

        play(messageId, filePath, durationSec)
    }

    fun play(messageId: String, filePath: String?, fallbackDurationSec: Int = 5) {
        stop()

        val file = filePath?.let { File(it) }
        val canPlayHardware = file != null && file.exists() && file.length() > 500

        if (canPlayHardware) {
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(file!!.absolutePath)
                    prepare()
                    start()
                }
                mediaPlayer = mp
                val duration = mp.duration.coerceAtLeast(1000)

                _playbackState.value = PlaybackState(
                    messageId = messageId,
                    isPlaying = true,
                    progress = 0f,
                    currentPositionMs = 0,
                    durationMs = duration
                )

                mp.setOnCompletionListener {
                    stop()
                }

                startProgressTracker(messageId, duration)
                return
            } catch (e: Exception) {
                Log.w(TAG, "Hardware playback failed, using simulated audio animation: ${e.message}")
            }
        }

        // Simulated playback for demo files or silent test files
        val totalMs = (if (fallbackDurationSec > 0) fallbackDurationSec else 5) * 1000
        _playbackState.value = PlaybackState(
            messageId = messageId,
            isPlaying = true,
            progress = 0f,
            currentPositionMs = 0,
            durationMs = totalMs
        )
        startProgressTracker(messageId, totalMs)
    }

    private fun startProgressTracker(messageId: String, durationMs: Int) {
        progressJob?.cancel()
        progressJob = scope.launch {
            val step = 100L
            var currentMs = 0
            while (currentMs < durationMs && _playbackState.value.messageId == messageId && _playbackState.value.isPlaying) {
                delay(step)
                currentMs += step.toInt()
                val progress = (currentMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                _playbackState.value = _playbackState.value.copy(
                    progress = progress,
                    currentPositionMs = currentMs
                )
            }
            if (_playbackState.value.messageId == messageId) {
                stop()
            }
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            Log.w(TAG, "Error pausing player: ${e.message}")
        }
        progressJob?.cancel()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    fun stop() {
        progressJob?.cancel()
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing player: ${e.message}")
        } finally {
            mediaPlayer = null
        }
        _playbackState.value = PlaybackState()
    }

    fun release() {
        stop()
    }
}
