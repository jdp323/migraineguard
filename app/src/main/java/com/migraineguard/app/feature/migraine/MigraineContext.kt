package com.migraineguard.app.feature.migraine

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.prefs.*
import com.migraineguard.app.core.domain.WeatherSnapshot
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.weather.WeatherRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun MigraineContext(preferences: UserPreferencesRepository, weatherRepository: WeatherRepository) {
    val prefs by preferences.preferences.collectAsStateWithLifecycle(UserPreferences())
    val scope = rememberCoroutineScope()
    var weather by remember { mutableStateOf<WeatherSnapshot?>(null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    suspend fun refresh() { busy = true; try { weather = weatherRepository.currentWeather(true); message = if (weather == null) "Weather unavailable." else "" } finally { busy = false } }
    val location = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) scope.launch { refresh() } else message = "Location was not allowed. Other journal features still work." }
    SectionCard("Energy & context") {
        Text("How is your energy today?")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { (1..5).forEach { n ->
            FilterChip(selected = prefs.energyDateIso == LocalDate.now().toString() && prefs.energyLevel == n,
                onClick = { scope.launch { preferences.setEnergyLevel(n, LocalDate.now().toString()) } }, label = { Text(n.toString()) })
        } }
        Text("Keep your own observations alongside symptoms and triggers. Patterns do not prove a cause.", style = MaterialTheme.typography.bodySmall)
    }
    SectionCard("Weather context") {
        weather?.let { w -> Text(w.condition+" · "+w.temperatureC+"°C · "+w.pressureHpa+" hPa"); if (w.isCachedOrFake) Text("Older reading; not current conditions.") }
        Text("Optional. Approximate coordinates are sent to Open-Meteo when you request weather. This is context, not a migraine forecast.", style = MaterialTheme.typography.bodySmall)
        if (message.isNotBlank()) Text(message)
        OutlinedButton(onClick = { location.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }, enabled = !busy) { Text(if (busy) "Checking…" else "Check local weather") }
    }
}
