package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result

interface AiProvider {
    val name: String
    /** Lower = tried first in [FallbackAiClient]. */
    val priority: Int

    suspend fun complete(prompt: String): Result<String>
}
