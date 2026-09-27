package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AgentAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AgentAccessibility"
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _volumeTriggerEvent = MutableStateFlow<Long?>(null)
        val volumeTriggerEvent: StateFlow<Long?> = _volumeTriggerEvent.asStateFlow()

        private val _screenContextText = MutableStateFlow("Home / Active Desktop")
        val screenContextText: StateFlow<String> = _screenContextText.asStateFlow()

        fun triggerVolumeShortcut() {
            _volumeTriggerEvent.value = System.currentTimeMillis()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceRunning.value = true
        Log.d(TAG, "AgentAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: ""
        val className = event.className?.toString() ?: ""
        val textList = event.text.mapNotNull { it?.toString() }

        if (textList.isNotEmpty()) {
            _screenContextText.value = "$packageName: ${textList.joinToString(" ")}"
        } else if (packageName.isNotEmpty()) {
            _screenContextText.value = "Active: $packageName ($className)"
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "AgentAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event != null && event.action == KeyEvent.ACTION_DOWN) {
            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                Log.d(TAG, "Volume Up pressed in AccessibilityService - toggling Agent listening")
                _volumeTriggerEvent.value = System.currentTimeMillis()
                return false // allow normal volume handling as well
            }
        }
        return super.onKeyEvent(event)
    }
}
