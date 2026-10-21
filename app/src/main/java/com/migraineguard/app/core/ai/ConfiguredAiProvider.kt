package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.data.prefs.UserPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

fun UserPreferences.connection(id: String): AiConnection {
    val stored = aiConnections[id]
    val legacy = when (id) { "gemini" -> geminiApiKey; "groq" -> groqApiKey; "openrouter" -> openRouterApiKey; else -> stored?.apiKey.orEmpty() }
    return AiConnection(legacy, stored?.model?.takeIf { it.isNotBlank() } ?: AiCatalog.service(id)?.defaultModel.orEmpty())
}

/** Selected service only; legacy Auto retains the user's original three-provider fallback. */
class ConfiguredAiProvider(private val preferences: suspend () -> UserPreferences) : BaseAiProvider("Selected cloud AI", 0) {
    override suspend fun complete(prompt: String): Result<String> {
        val prefs = preferences()
        if (prefs.preferredAiProvider == "offline") return Result.Error("On-device mode selected")
        val ids = if (prefs.preferredAiProvider == "auto") listOf("gemini", "groq", "openrouter") else listOf(prefs.preferredAiProvider)
        var last: Result<String> = Result.Error("No configured cloud key. Open Settings → AI providers.")
        for (id in ids) {
            val c = prefs.connection(id)
            if (c.apiKey.isBlank()) continue
            last = completeService(id, c, prompt)
            if (last is Result.Success) return last
        }
        return last
    }
    suspend fun completeWithImage(prompt: String, base64: String, mimeType: String): Result<String> {
        val prefs = preferences()
        val id = if (prefs.preferredAiProvider == "auto") "gemini" else prefs.preferredAiProvider
        if (AiCatalog.service(id)?.images != true) return Result.Error("Choose Gemini, OpenAI or Claude in Settings for photos. The photo was not sent.")
        return completeService(id, prefs.connection(id), prompt, base64, mimeType)
    }
}

suspend fun completeService(id: String, connection: AiConnection, prompt: String, image: String? = null, mime: String = "image/jpeg"): Result<String> = withContext(Dispatchers.IO) {
    val service = AiCatalog.service(id) ?: return@withContext Result.Error("Unknown AI provider")
    if (connection.apiKey.isBlank()) return@withContext Result.Error("Add your "+service.label+" API key in Settings.")
    if (connection.model.isBlank()) return@withContext Result.Error("Enter the model ID offered by "+service.label+".")
    if (image != null && !service.images) return@withContext Result.Error("Photo analysis is not enabled for this provider.")
    try {
        val response = executeRequest(buildServiceRequest(service, connection, prompt, image, mime))
        if (response.code !in 200..299) return@withContext Result.Error(service.label+" HTTP "+response.code+when (response.code) {
            401 -> ": key rejected"; 403 -> ": check API access or account restrictions"; 404 -> ": check the model ID"; 429 -> ": quota/rate limit reached"; else -> ": request failed"
        })
        val output = parseServiceOutput(service.protocol, response.body)
        if (output.isBlank()) Result.Error(service.label+" returned no usable text") else Result.Success(output)
    } catch (e: CancellationException) { throw e }
    catch (_: Exception) { Result.Error(service.label+" request failed. Check internet, model access and key settings.") }
}

internal fun buildServiceRequest(service: AiService, c: AiConnection, prompt: String, image: String? = null, mime: String = "image/jpeg"): Request {
    require(c.apiKey.length <= 4096 && c.model.length in 1..160)
    require(image == null || (service.images && image.length <= 6_000_000 && mime in setOf("image/jpeg", "image/png", "image/webp")))
    val payload = JSONObject().put("model", c.model)
    var url = service.endpoint
    val builder = Request.Builder().header("Content-Type", "application/json")
    when (service.protocol) {
        ProviderProtocol.GEMINI -> {
            require(c.model.matches(Regex("[A-Za-z0-9._-]+")))
            url += c.model+":generateContent"
            payload.remove("model")
            val parts = JSONArray().put(JSONObject().put("text", prompt))
            if (image != null) parts.put(JSONObject().put("inline_data", JSONObject().put("mime_type", mime).put("data", image)))
            payload.put("contents", JSONArray().put(JSONObject().put("parts", parts)))
                .put("generationConfig", JSONObject().put("maxOutputTokens", 1800))
            builder.header("x-goog-api-key", c.apiKey)
        }
        ProviderProtocol.RESPONSES -> {
            val content = JSONArray().put(JSONObject().put("type", "input_text").put("text", prompt))
            if (image != null) content.put(JSONObject().put("type", "input_image").put("image_url", "data:"+mime+";base64,"+image))
            payload.put("input", JSONArray().put(JSONObject().put("role", "user").put("content", content)))
                .put("store", false).put("max_output_tokens", 1800)
            builder.header("Authorization", "Bearer "+c.apiKey)
        }
        ProviderProtocol.CLAUDE -> {
            val content = JSONArray().put(JSONObject().put("type", "text").put("text", prompt))
            if (image != null) content.put(JSONObject().put("type", "image").put("source", JSONObject().put("type", "base64").put("media_type", mime).put("data", image)))
            payload.put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", content))).put("max_tokens", 1800)
            builder.header("x-api-key", c.apiKey).header("anthropic-version", "2023-06-01")
        }
        ProviderProtocol.CHAT -> {
            payload.put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt))).put("max_tokens", 1800)
            builder.header("Authorization", "Bearer "+c.apiKey)
        }
    }
    return builder.url(url).post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())).build()
}

internal fun parseServiceOutput(protocol: ProviderProtocol, body: String): String {
    val obj = JSONObject(body)
    return when (protocol) {
        ProviderProtocol.GEMINI -> obj.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").textParts("text")
        ProviderProtocol.CLAUDE -> obj.getJSONArray("content").textParts("text")
        ProviderProtocol.CHAT -> obj.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content")
        ProviderProtocol.RESPONSES -> {
            require(obj.optString("status", "completed") == "completed") { "Incomplete response" }
            val outputs = obj.getJSONArray("output")
            (0 until outputs.length()).mapNotNull { outputs.getJSONObject(it).optJSONArray("content") }
                .joinToString("\n") { it.textParts("text") }
        }
    }.trim()
}
private fun JSONArray.textParts(key: String): String = (0 until length()).mapNotNull { i ->
    optJSONObject(i)?.optString(key)?.takeIf { it.isNotBlank() }
}.joinToString("\n")

fun UserPreferences.hasConfiguredCloud(): Boolean = when (preferredAiProvider) {
    "offline" -> false
    "auto" -> listOf("gemini", "groq", "openrouter").any { connection(it).apiKey.isNotBlank() }
    else -> connection(preferredAiProvider).let { it.apiKey.isNotBlank() && it.model.isNotBlank() }
}
