package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .callTimeout(50, TimeUnit.SECONDS)
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

/** Gemini free tier — user DataStore key only (never developer tokens). */
class GeminiFreeProvider(
    private val keyProvider: suspend () -> String = { "" },
) : BaseAiProvider("GeminiFree", 0) {
    private val model = "gemini-3.5-flash-lite"
    override suspend fun complete(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        // Per-user keys only — never developer BuildConfig / Firebase api_key
        val key = keyProvider().trim()
        if (key.isBlank()) {
            return@withContext Result.Error("No user Gemini key — connect yours in Settings or Connect Gemini")
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
                .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
                .addHeader("x-goog-api-key", key)
                .post(body)
                .build()
            val response = executeRequest(req)
            if (response.code !in 200..299) return@withContext Result.Error("HTTP ${response.code}")
            val root = JSONObject(response.body)
            val parts = root.getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts")
            val out = (0 until parts.length()).joinToString("\n") { parts.getJSONObject(it).optString("text") }
            Result.Success(out)
        }.getOrElse { if (it is CancellationException) throw it; Result.Error("Gemini request failed", it) }
    }

    suspend fun testConnection(): Result<String> = complete("Reply with exactly: ok")
}

/** Groq free tier when GROQ_API_KEY is set. */
class GroqFreeProvider(
    private val keyProvider: suspend () -> String = { "" },
) : BaseAiProvider("GroqFree", 1) {
    override suspend fun complete(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val key = keyProvider().trim()
        if (key.isBlank()) {
            return@withContext Result.Error("No user Groq key")
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
        val key = keyProvider().trim()
        if (key.isBlank()) {
            return@withContext Result.Error("No user OpenRouter key")
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

private suspend fun chatCompletions(
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
    val response = executeRequest(builder.build())
    if (response.code !in 200..299) return Result.Error("HTTP ${response.code}")
    val content = JSONObject(response.body).getJSONArray("choices")
        .getJSONObject(0).getJSONObject("message").getString("content")
    Result.Success(content)
}.getOrElse { if (it is CancellationException) throw it; Result.Error("Provider request failed", it) }

private data class HttpResponse(val code: Int, val body: String)

/** Cancel the actual network call when its coroutine is cancelled or times out. */
private suspend fun executeRequest(request: Request): HttpResponse = suspendCancellableCoroutine { continuation ->
    val call = httpClient.newCall(request)
    continuation.invokeOnCancellation { call.cancel() }
    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isActive) continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                val result = response.use { HttpResponse(it.code, it.body?.string().orEmpty()) }
                if (continuation.isActive) continuation.resume(result)
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }
        }
    })
}
