package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result

/**
 * Stub free-tier providers. No network calls.
 * Flip [shouldFail] in tests/demos to exercise [FallbackAiClient] chaining.
 *
 * Planned real order (document in README):
 * 1. Gemini free → 2. Groq free → 3. OpenRouter free
 */
abstract class StubAiProvider(
    override val name: String,
    override val priority: Int,
    private val shouldFail: Boolean = false,
) : AiProvider {
    override suspend fun complete(prompt: String): Result<String> {
        if (shouldFail) {
            return Result.Error("Quota or stub failure for $name")
        }
        return Result.Success(
            "[$name stub] Placeholder insight for: ${prompt.take(80)}" +
                if (prompt.length > 80) "…" else ""
        )
    }
}

class GeminiFreeProvider(shouldFail: Boolean = false) :
    StubAiProvider(name = "GeminiFree", priority = 0, shouldFail = shouldFail)

class GroqFreeProvider(shouldFail: Boolean = false) :
    StubAiProvider(name = "GroqFree", priority = 1, shouldFail = shouldFail)

class OpenRouterFreeProvider(shouldFail: Boolean = false) :
    StubAiProvider(name = "OpenRouterFree", priority = 2, shouldFail = shouldFail)
