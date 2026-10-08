package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

internal class TestProvider(
    override val name: String,
    override val priority: Int,
    private val answer: suspend () -> Result<String>,
) : AiProvider {
    var calls = 0
    override suspend fun complete(prompt: String): Result<String> {
        calls++
        return answer()
    }
}

class FallbackAiClientTest {
    @Test fun successUsesPriorityAndStopsFurtherTransmission() = runTest {
        val later = TestProvider("later", 5) { Result.Success("later") }
        val first = TestProvider("first", 0) { Result.Success("first") }
        assertEquals(Result.Success("first"), FallbackAiClient(listOf(later, first)).complete("hello"))
        assertEquals(0, later.calls)
    }

    @Test fun exceptionAndBlankResponseContinueToUsableProvider() = runTest {
        val thrown = TestProvider("broken", 0) { error("network unavailable") }
        val blank = TestProvider("blank", 1) { Result.Success("  ") }
        val working = TestProvider("working", 2) { Result.Success("usable") }
        assertEquals(Result.Success("usable"), FallbackAiClient(listOf(thrown, blank, working)).complete("hello"))
    }

    @Test fun providerTimeoutContinuesToFallback() = runTest {
        val slow = TestProvider("slow", 0) { delay(10_000); Result.Success("late") }
        val fallback = TestProvider("fallback", 1) { Result.Success("ready") }
        val client = FallbackAiClient(listOf(slow, fallback), providerTimeoutMillis = 100)
        assertEquals(Result.Success("ready"), client.complete("hello"))
        assertEquals(1, fallback.calls)
    }

    @Test fun cancellationNeverSendsToAnotherProvider() = runTest {
        val cancelled = TestProvider("cancelled", 0) { throw CancellationException("closed") }
        val fallback = TestProvider("fallback", 1) { Result.Success("unexpected") }
        try {
            FallbackAiClient(listOf(cancelled, fallback)).complete("hello")
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
        assertEquals(0, fallback.calls)
    }

    @Test fun parentTimeoutNeverStartsAnotherProvider() = runTest {
        val slow = TestProvider("slow", 0) { delay(10_000); Result.Success("late") }
        val fallback = TestProvider("fallback", 1) { Result.Success("unexpected") }
        try {
            withTimeout(50) {
                FallbackAiClient(listOf(slow, fallback), providerTimeoutMillis = 1000).complete("hello")
            }
            fail("Parent timeout must propagate")
        } catch (_: TimeoutCancellationException) { }
        assertEquals(0, fallback.calls)
    }

    @Test fun noProvidersReportsUnavailable() = runTest {
        assertTrue(FallbackAiClient(emptyList()).complete("hello") is Result.Error)
    }
}
