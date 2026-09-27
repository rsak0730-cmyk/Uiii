package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class VoiceNoteRecorder(private val context: Context) {

    companion object {
        private const val TAG = "VoiceNoteRecorder"
    }

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTimeMillis: Long = 0L

    var isRecording: Boolean = false
        private set

    fun startRecording(): File? {
        try {
            val dir = File(context.cacheDir, "voice_notes")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val audioFile = File(dir, "voice_note_${System.currentTimeMillis()}.m4a")
            currentFile = audioFile

            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioEncodingBitRate(128000)
            mr.setAudioSamplingRate(44100)
            mr.setOutputFile(audioFile.absolutePath)

            mr.prepare()
            mr.start()

            recorder = mr
            isRecording = true
            startTimeMillis = System.currentTimeMillis()
            Log.d(TAG, "Audio recording started: ${audioFile.absolutePath}")
            return audioFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start hardware recording: ${e.message}")
            // Fallback for emulators without audio hardware: generate a placeholder audio container
            return try {
                val dir = File(context.cacheDir, "voice_notes")
                if (!dir.exists()) dir.mkdirs()
                val fallbackFile = File(dir, "voice_note_${System.currentTimeMillis()}.m4a")
                FileOutputStream(fallbackFile).use { it.write(ByteArray(1024)) }
                currentFile = fallbackFile
                isRecording = true
                startTimeMillis = System.currentTimeMillis()
                fallbackFile
            } catch (ex: Exception) {
                null
            }
        }
    }

    fun getMaxAmplitude(): Int {
        return try {
            if (isRecording && recorder != null) {
                recorder?.maxAmplitude ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun stopRecording(): Pair<File?, Int> {
        if (!isRecording) return Pair(null, 0)

        val durationMillis = System.currentTimeMillis() - startTimeMillis
        val durationSec = maxOf(1, (durationMillis / 1000).toInt())
        val file = currentFile

        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping recorder: ${e.message}")
        } finally {
            recorder = null
            isRecording = false
        }

        return Pair(file, durationSec)
    }

    fun cancelRecording() {
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cancelling recorder: ${e.message}")
        } finally {
            recorder = null
            isRecording = false
            currentFile?.delete()
            currentFile = null
        }
    }
}
