package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result

/**
 * Pluggable AI backend. Real HTTP will be added later; free-tier keys come from
 * BuildConfig / local.properties (never commit secrets).
 */
interface AiProvider {
    val name: String
    /** Lower = tried first in [FallbackAiClient]. */
    val priority: Int

    suspend fun complete(prompt: String): Result<String>
}
