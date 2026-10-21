package com.migraineguard.app.core.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiConnection(val apiKey: String = "", val model: String = "")

enum class ProviderProtocol { GEMINI, RESPONSES, CLAUDE, CHAT }
data class AiService(val id: String, val label: String, val endpoint: String, val defaultModel: String, val protocol: ProviderProtocol, val images: Boolean = false)

object AiCatalog {
    val services = listOf(
        AiService("gemini", "Google Gemini", "https://generativelanguage.googleapis.com/v1beta/models/", "gemini-3.5-flash-lite", ProviderProtocol.GEMINI, true),
        AiService("openai", "OpenAI / ChatGPT API", "https://api.openai.com/v1/responses", "gpt-4.1-mini", ProviderProtocol.RESPONSES, true),
        AiService("claude", "Anthropic Claude", "https://api.anthropic.com/v1/messages", "claude-haiku-4-5-20251001", ProviderProtocol.CLAUDE, true),
        AiService("groq", "Groq", "https://api.groq.com/openai/v1/chat/completions", "llama-3.1-8b-instant", ProviderProtocol.CHAT),
        AiService("openrouter", "OpenRouter", "https://openrouter.ai/api/v1/chat/completions", "openrouter/auto", ProviderProtocol.CHAT),
        AiService("deepseek", "DeepSeek", "https://api.deepseek.com/chat/completions", "deepseek-flash", ProviderProtocol.CHAT),
        AiService("xai", "xAI / Grok", "https://api.x.ai/v1/chat/completions", "grok-4.7", ProviderProtocol.CHAT),
        AiService("mistral", "Mistral AI", "https://api.mistral.ai/v1/chat/completions", "mistral-small-latest", ProviderProtocol.CHAT),
        AiService("perplexity", "Perplexity", "https://api.perplexity.ai/v1/responses", "openai/gpt-5.6-sol", ProviderProtocol.RESPONSES),
        AiService("together", "Together AI", "https://api.together.xyz/v1/chat/completions", "", ProviderProtocol.CHAT),
        AiService("fireworks", "Fireworks AI", "https://api.fireworks.ai/inference/v1/chat/completions", "", ProviderProtocol.CHAT),
    )
    fun service(id: String) = services.firstOrNull { it.id == id }
    /** Only a suggestion; ambiguous sk- tokens are never probed against multiple services. */
    fun suggest(key: String): String? = when {
        key.trim().startsWith("sk-ant-") -> "claude"
        key.trim().startsWith("sk-or-v1-") -> "openrouter"
        key.trim().startsWith("gsk_") -> "groq"
        key.trim().startsWith("sk-proj-") -> "openai"
        key.trim().startsWith("pplx-") -> "perplexity"
        key.trim().startsWith("xai-") -> "xai"
        key.trim().startsWith("AIza") -> "gemini"
        else -> null
    }
}
