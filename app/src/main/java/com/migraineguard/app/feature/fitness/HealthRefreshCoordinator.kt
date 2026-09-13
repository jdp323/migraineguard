package com.migraineguard.app.feature.fitness

import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

/**
 * Shared holder that refreshes Health Connect fitness + sleep into UI state.
 * Debounces (~2s) so foreground resume + tab switches are not spammy.
 * Never blocks the UI; soft last-refreshed / error surfaces.
 */
class HealthRefreshCoordinator(
    private val healthDataRepository: HealthDataRepository,
    private val scope: CoroutineScope,
) {
    data class State(
        val fitness: HealthSnapshot? = null,
        val sleep: SleepSnapshot? = null,
        val lastRefreshed: Instant? = null,
        val isRefreshing: Boolean = false,
        val lastError: String? = null,
        val permitted: Boolean = false,
        val status: HealthConnectStatus = HealthConnectStatus.USING_FALLBACK,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val mutex = Mutex()
    private var lastKickAtMs: Long = 0L
    private var runningJob: Job? = null

    companion object {
        const val DEBOUNCE_MS = 2_000L
    }

    /** Kick a refresh; debounced unless [force]. */
    fun refresh(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastKickAtMs < DEBOUNCE_MS) return
        lastKickAtMs = now
        runningJob?.cancel()
        runningJob = scope.launch(Dispatchers.IO) {
            mutex.withLock {
                _state.value = _state.value.copy(isRefreshing = true, lastError = null)
                try {
                    val status = healthDataRepository.availabilityStatus()
                    val permitted = healthDataRepository.hasAllPermissions()
                    val fitness = healthDataRepository.latestFitness()
                    val sleep = healthDataRepository.latestSleep()
                    val softErr = healthDataRepository.lastError().ifBlank { null }
                    _state.value = State(
                        fitness = fitness,
                        sleep = sleep,
                        lastRefreshed = Instant.now(),
                        isRefreshing = false,
                        lastError = softErr,
                        permitted = permitted,
                        status = status,
                    )
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        lastError = e.message ?: e.javaClass.simpleName,
                        lastRefreshed = Instant.now(),
                    )
                }
            }
        }
    }
}
