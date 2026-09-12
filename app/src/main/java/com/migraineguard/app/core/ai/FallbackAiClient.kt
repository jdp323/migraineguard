package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result

/**
 * Tries providers in priority order (most capable free tier first).
 * On failure / quota-style errors, advances to the next provider.
 */
class FallbackAiClient(
    providers: List<AiProvider>,
) {
    private val ordered = providers.sortedBy { it.priority }

    suspend fun complete(prompt: String): Result<String> {
        var lastError: String? = null
        for (provider in ordered) {
            when (val result = provider.complete(prompt)) {
                is Result.Success -> return result
                is Result.Error -> lastError = "${provider.name}: ${result.message}"
            }
        }
        return Result.Error(lastError ?: "No AI providers configured")
    }

    fun providerNames(): List<String> = ordered.map { it.name }
}
