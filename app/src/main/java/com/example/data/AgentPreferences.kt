package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AgentPreferences(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("omni_agent_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<NeonTheme> = _theme.asStateFlow()

    private val _uiStyle = MutableStateFlow(loadUiStyle())
    val uiStyle: StateFlow<UiDesignStyle> = _uiStyle.asStateFlow()

    private val _textEffect = MutableStateFlow(loadTextEffect())
    val textEffect: StateFlow<TextEffect> = _textEffect.asStateFlow()

    private val _dynamicIslandConfig = MutableStateFlow(loadDynamicIslandConfig())
    val dynamicIslandConfig: StateFlow<DynamicIslandConfig> = _dynamicIslandConfig.asStateFlow()

    private val _apiConfig = MutableStateFlow(loadApiConfig())
    val apiConfig: StateFlow<ApiConfig> = _apiConfig.asStateFlow()

    private val _voicemails = MutableStateFlow(loadVoicemails())
    val voicemails: StateFlow<List<Voicemail>> = _voicemails.asStateFlow()

    private val _energyAwareAi = MutableStateFlow(prefs.getBoolean("energy_aware_ai", true))
    val energyAwareAi: StateFlow<Boolean> = _energyAwareAi.asStateFlow()

    fun setEnergyAwareAi(enabled: Boolean) {
        prefs.edit().putBoolean("energy_aware_ai", enabled).apply()
        _energyAwareAi.value = enabled
    }

    private fun loadTheme(): NeonTheme {
        val name = prefs.getString("neon_theme", NeonTheme.NEON_BLUE.name)
        return try {
            NeonTheme.valueOf(name ?: NeonTheme.NEON_BLUE.name)
        } catch (_: Exception) {
            NeonTheme.NEON_BLUE
        }
    }

    fun setTheme(theme: NeonTheme) {
        prefs.edit().putString("neon_theme", theme.name).apply()
        _theme.value = theme
    }

    private fun loadUiStyle(): UiDesignStyle {
        val name = prefs.getString("ui_style", UiDesignStyle.CYBERPUNK.name)
        return try {
            UiDesignStyle.valueOf(name ?: UiDesignStyle.CYBERPUNK.name)
        } catch (_: Exception) {
            UiDesignStyle.CYBERPUNK
        }
    }

    fun setUiStyle(style: UiDesignStyle) {
        prefs.edit().putString("ui_style", style.name).apply()
        _uiStyle.value = style
    }

    private fun loadTextEffect(): TextEffect {
        val name = prefs.getString("text_effect", TextEffect.GLOW.name)
        return try {
            TextEffect.valueOf(name ?: TextEffect.GLOW.name)
        } catch (_: Exception) {
            TextEffect.GLOW
        }
    }

    fun setTextEffect(effect: TextEffect) {
        prefs.edit().putString("text_effect", effect.name).apply()
        _textEffect.value = effect
    }

    private fun loadDynamicIslandConfig(): DynamicIslandConfig {
        return DynamicIslandConfig(
            isEnabled = prefs.getBoolean("island_enabled", true),
            offsetX = prefs.getFloat("island_offset_x", 0f),
            offsetY = prefs.getFloat("island_offset_y", 12f),
            widthDp = prefs.getFloat("island_width", 220f),
            cornerRadiusDp = prefs.getFloat("island_corner_radius", 28f),
            animationDurationMs = prefs.getInt("island_anim_duration", 300)
        )
    }

    fun setDynamicIslandConfig(config: DynamicIslandConfig) {
        prefs.edit()
            .putBoolean("island_enabled", config.isEnabled)
            .putFloat("island_offset_x", config.offsetX)
            .putFloat("island_offset_y", config.offsetY)
            .putFloat("island_width", config.widthDp)
            .putFloat("island_corner_radius", config.cornerRadiusDp)
            .putInt("island_anim_duration", config.animationDurationMs)
            .apply()
        _dynamicIslandConfig.value = config
    }

    private fun loadApiConfig(): ApiConfig {
        val providerStr = prefs.getString("api_provider", ApiProvider.GEMINI.name)
        val provider = try {
            ApiProvider.valueOf(providerStr ?: ApiProvider.GEMINI.name)
        } catch (_: Exception) {
            ApiProvider.GEMINI
        }
        return ApiConfig(
            profileName = prefs.getString("api_profile_name", "Default Agent Brain") ?: "Default Agent Brain",
            provider = provider,
            apiKey = prefs.getString("api_key", "") ?: "",
            selectedModel = prefs.getString("api_model", provider.models.first()) ?: provider.models.first(),
            baseUrl = prefs.getString("api_base_url", provider.defaultBaseUrl) ?: provider.defaultBaseUrl
        )
    }

    fun setApiConfig(config: ApiConfig) {
        prefs.edit()
            .putString("api_profile_name", config.profileName)
            .putString("api_provider", config.provider.name)
            .putString("api_key", config.apiKey)
            .putString("api_model", config.selectedModel)
            .putString("api_base_url", config.baseUrl)
            .apply()
        _apiConfig.value = config
    }

    private fun loadVoicemails(): List<Voicemail> {
        val jsonStr = prefs.getString("voicemails_json", null)
        if (jsonStr.isNullOrEmpty()) {
            // Seed initial realistic voicemails
            return listOf(
                Voicemail(
                    callerName = "Alex Rivera",
                    phoneNumber = "+1 (555) 234-8901",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                    durationSeconds = 24,
                    transcript = "Hey! It's Alex. Omni answered when you didn't pick up after 20 seconds. Calling regarding the project demo tomorrow morning. Let me know when you're free!",
                    isUnread = true
                ),
                Voicemail(
                    callerName = "Emma Watson",
                    phoneNumber = "+1 (555) 892-1144",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 180,
                    durationSeconds = 15,
                    transcript = "Hi! Leaving this voicemail via your AI Agent. Just confirming dinner reservation for Friday at 7 PM. Talk soon!",
                    isUnread = false
                ),
                Voicemail(
                    callerName = "Tech Support",
                    phoneNumber = "+1 (800) 441-2020",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 720,
                    durationSeconds = 32,
                    transcript = "Hello, this is tech dispatch. Your package delivery is scheduled between 2 PM and 4 PM today. Signature will be required.",
                    isUnread = false
                )
            )
        }
        return try {
            val list = mutableListOf<Voicemail>()
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Voicemail(
                        id = obj.getString("id"),
                        callerName = obj.getString("callerName"),
                        phoneNumber = obj.getString("phoneNumber"),
                        timestamp = obj.getLong("timestamp"),
                        durationSeconds = obj.getInt("durationSeconds"),
                        transcript = obj.getString("transcript"),
                        isUnread = obj.getBoolean("isUnread")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveVoicemails(voicemails: List<Voicemail>) {
        _voicemails.value = voicemails
        try {
            val array = JSONArray()
            for (vm in voicemails) {
                val obj = JSONObject()
                obj.put("id", vm.id)
                obj.put("callerName", vm.callerName)
                obj.put("phoneNumber", vm.phoneNumber)
                obj.put("timestamp", vm.timestamp)
                obj.put("durationSeconds", vm.durationSeconds)
                obj.put("transcript", vm.transcript)
                obj.put("isUnread", vm.isUnread)
                array.put(obj)
            }
            prefs.edit().putString("voicemails_json", array.toString()).apply()
        } catch (_: Exception) { }
    }

    fun addVoicemail(voicemail: Voicemail) {
        val updated = listOf(voicemail) + _voicemails.value
        saveVoicemails(updated)
    }

    fun deleteVoicemail(id: String) {
        val updated = _voicemails.value.filter { it.id != id }
        saveVoicemails(updated)
    }
}
