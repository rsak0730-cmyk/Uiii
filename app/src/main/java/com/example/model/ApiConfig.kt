package com.example.model

enum class ApiProvider(val displayName: String, val defaultBaseUrl: String, val models: List<String>) {
    GEMINI(
        displayName = "Gemini (AI Studio)",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/",
        models = listOf(
            "gemini-3.5-flash",
            "gemini-3.1-pro-preview",
            "gemini-2.5-flash-image",
            "gemini-3.1-flash-lite-preview"
        )
    ),
    OPENROUTER(
        displayName = "OpenRouter",
        defaultBaseUrl = "https://openrouter.ai/api/v1/",
        models = listOf(
            "google/gemini-2.0-flash-exp:free",
            "deepseek/deepseek-r1:free",
            "meta-llama/llama-3.3-70b-instruct:free",
            "mistralai/mistral-7b-instruct:free",
            "qwen/qwen-2.5-coder-32b-instruct:free"
        )
    ),
    OPENAI(
        displayName = "OpenAI",
        defaultBaseUrl = "https://api.openai.com/v1/",
        models = listOf(
            "gpt-4o-mini",
            "gpt-4o",
            "gpt-3.5-turbo",
            "o3-mini"
        )
    )
}

data class ApiConfig(
    val profileName: String = "Default Agent Brain",
    val provider: ApiProvider = ApiProvider.GEMINI,
    val apiKey: String = "",
    val selectedModel: String = "gemini-3.5-flash",
    val baseUrl: String = "https://generativelanguage.googleapis.com/"
)
