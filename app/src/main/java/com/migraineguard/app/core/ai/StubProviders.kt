package com.migraineguard.app.core.ai

import com.migraineguard.app.BuildConfig
import com.migraineguard.app.core.data.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()
}

private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

abstract class BaseAiProvider(
    override val name: String,
    override val priority: Int,
) : AiProvider

fun resolveApiKey(runtime: String?, buildConfig: String): String {
    val r = runtime?.trim().orEmpty()
    if (r.isNotBlank()) return r
    return buildConfig.trim()
}

/** Gemini free tier — runtime DataStore key overrides BuildConfig. */
class GeminiFreeProvider(
    private val keyProvider: suspend () -> String = { "" },
) : BaseAiProvider("GeminiFree", 0) {
    override suspend fun complete(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val key = resolveApiKey(keyProvider(), BuildConfig.GEMINI_API_KEY)
        if (key.isBlank()) {
            return@withContext Result.Error("No GEMINI_API_KEY")
        }
        runCatching {
            val body = JSONObject()
                .put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", prompt)),
                        ),
                    ),
                )
                .toString()
                .toRequestBody(JSON_MEDIA)
            val req = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$key")
                .post(body)
                .build()
            httpClient.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) return@withContext Result.Error("HTTP ${resp.code}")
                val root = JSONObject(text)
                val out = root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                Result.Success(out)
            }
        }.getOrElse { Result.Error(it.message ?: "Gemini failed", it) }
    }

    suspend fun testConnection(): Result<String> = complete("Reply with exactly: ok")
}

/** Groq free tier when GROQ_API_KEY is set. */
class GroqFreeProvider(
    private val keyProvider: suspend () -> String = { "" },
) : BaseAiProvider("GroqFree", 1) {
    override suspend fun complete(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val key = resolveApiKey(keyProvider(), BuildConfig.GROQ_API_KEY)
        if (key.isBlank()) {
            return@withContext Result.Error("No GROQ_API_KEY")
        }
        chatCompletions(
            url = "https://api.groq.com/openai/v1/chat/completions",
            apiKey = key,
            model = "llama-3.1-8b-instant",
            prompt = prompt,
        )
    }
}

/** OpenRouter free models when OPENROUTER_API_KEY is set. */
class OpenRouterFreeProvider(
    private val keyProvider: suspend () -> String = { "" },
) : BaseAiProvider("OpenRouterFree", 2) {
    override suspend fun complete(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val key = resolveApiKey(keyProvider(), BuildConfig.OPENROUTER_API_KEY)
        if (key.isBlank()) {
            return@withContext Result.Error("No OPENROUTER_API_KEY")
        }
        chatCompletions(
            url = "https://openrouter.ai/api/v1/chat/completions",
            apiKey = key,
            model = "openrouter/auto",
            prompt = prompt,
            extraHeaders = mapOf("HTTP-Referer" to "https://migraineguard.app", "X-Title" to "MigraineGuard"),
        )
    }
}

private fun chatCompletions(
    url: String,
    apiKey: String,
    model: String,
    prompt: String,
    extraHeaders: Map<String, String> = emptyMap(),
): Result<String> = runCatching {
    val payload = JSONObject()
        .put("model", model)
        .put(
            "messages",
            JSONArray().put(
                JSONObject().put("role", "user").put("content", prompt),
            ),
        )
        .toString()
        .toRequestBody(JSON_MEDIA)
    val builder = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(payload)
    extraHeaders.forEach { (k, v) -> builder.addHeader(k, v) }
    httpClient.newCall(builder.build()).execute().use { resp ->
        val text = resp.body?.string().orEmpty()
        if (!resp.isSuccessful) return Result.Error("HTTP ${resp.code}: ${text.take(120)}")
        val content = JSONObject(text)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
        Result.Success(content)
    }
}.getOrElse { Result.Error(it.message ?: "HTTP failed", it) }
