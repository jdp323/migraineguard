package com.migraineguard.app.feature.fitness

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Poll only while this health screen is visible and the app is resumed. */
@Composable
fun HealthAutoRefresh(coordinator: HealthRefreshCoordinator?) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(coordinator, lifecycle) {
        if (coordinator == null) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                coordinator.refresh()
                delay(30_000L)
            }
        }
    }
}
