package com.example.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class EnergyMode(val displayName: String, val recommendedModel: String, val description: String) {
    PERFORMANCE("Performance Mode", "gemini-3.1-pro-preview", "Full reasoning capability with thinking tokens and maximum context."),
    BALANCED("Balanced Mode", "gemini-3.5-flash", "Standard high-speed reasoning with optimal token throughput."),
    ECO_SAVER("Eco Saver Mode", "gemini-3.1-flash-lite-preview", "Lightweight, ultra-fast model to conserve device power and thermal envelope.")
}

data class BatteryInfo(
    val levelPercent: Int = 100,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val temperatureCelsius: Float = 25.0f,
    val pluggedSource: String = "Battery",
    val energyMode: EnergyMode = EnergyMode.PERFORMANCE
)

class BatteryMonitorService : Service() {

    companion object {
        private const val TAG = "BatteryMonitorService"
        private val _batteryInfo = MutableStateFlow(BatteryInfo())
        val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

        fun updateFromContext(context: Context) {
            try {
                val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryIntent = context.registerReceiver(null, filter)
                if (batteryIntent != null) {
                    processBatteryIntent(context, batteryIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying initial battery status: ${e.message}")
            }
        }

        private fun processBatteryIntent(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percent = if (level >= 0 && scale > 0) ((level.toFloat() / scale) * 100).toInt() else 100

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val pluggedSource = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
                else -> if (isCharging) "Charging" else "On Battery"
            }

            val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
            val tempCelsius = tempTenths / 10.0f

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isPowerSave = powerManager?.isPowerSaveMode ?: false

            val energyMode = when {
                isCharging || percent > 50 -> EnergyMode.PERFORMANCE
                percent in 20..50 && !isPowerSave -> EnergyMode.BALANCED
                else -> EnergyMode.ECO_SAVER
            }

            _batteryInfo.value = BatteryInfo(
                levelPercent = percent,
                isCharging = isCharging,
                isPowerSaveMode = isPowerSave,
                temperatureCelsius = tempCelsius,
                pluggedSource = pluggedSource,
                energyMode = energyMode
            )
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null && intent != null) {
                processBatteryIntent(context, intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BatteryMonitorService created")
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        registerReceiver(batteryReceiver, filter)
        updateFromContext(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateFromContext(this)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        Log.d(TAG, "BatteryMonitorService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
