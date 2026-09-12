package com.migraineguard.app.feature.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.migraineguard.app.core.domain.WeatherSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDate
import java.util.concurrent.TimeUnit

interface WeatherRepository {
    suspend fun currentWeather(forceRefresh: Boolean = false): WeatherSnapshot
}

class OpenMeteoWeatherRepository(private val context: Context) : WeatherRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    @Volatile private var cache: Pair<LocalDate, WeatherSnapshot>? = null

    override suspend fun currentWeather(forceRefresh: Boolean): WeatherSnapshot {
        val today = LocalDate.now()
        if (!forceRefresh) {
            cache?.let { if (it.first == today) return it.second }
        }
        val coords = lastKnownCoords()
        val snapshot = if (coords != null) {
            fetchOpenMeteo(coords.first, coords.second)
                ?: fakeWeather(offline = true)
        } else {
            fakeWeather(offline = false).copy(
                locationLabel = "Default · enable location for local weather",
                isCachedOrFake = true,
            )
        }
        cache = today to snapshot
        return snapshot
    }

    @SuppressLint("MissingPermission")
    private fun lastKnownCoords(): Pair<Double, Double>? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        for (p in providers) {
            runCatching {
                val loc = lm.getLastKnownLocation(p) ?: return@runCatching
                return loc.latitude to loc.longitude
            }
        }
        return null
    }

    private suspend fun fetchOpenMeteo(lat: Double, lon: Double): WeatherSnapshot? =
        withContext(Dispatchers.IO) {
            runCatching {
                val url =
                    "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                        "&current=temperature_2m,relative_humidity_2m,surface_pressure,weather_code" +
                        "&timezone=auto"
                val req = Request.Builder().url(url).get().build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use null
                    val root = JSONObject(resp.body?.string().orEmpty())
                    val current = root.getJSONObject("current")
                    val code = current.optInt("weather_code", 0)
                    WeatherSnapshot(
                        temperatureC = current.getDouble("temperature_2m").toFloat(),
                        pressureHpa = current.getDouble("surface_pressure").toFloat(),
                        humidityPercent = current.optInt("relative_humidity_2m", 50),
                        condition = weatherCodeLabel(code),
                        locationLabel = "Lat ${"%.2f".format(lat)}, Lon ${"%.2f".format(lon)} · Open-Meteo",
                        isCachedOrFake = false,
                    )
                }
            }.getOrNull()
        }

    private fun fakeWeather(offline: Boolean) = WeatherSnapshot(
        temperatureC = 21.5f,
        pressureHpa = 1013.2f,
        humidityPercent = 55,
        condition = if (offline) "Partly cloudy (offline cache)" else "Partly cloudy (demo)",
        locationLabel = if (offline) "Cached / offline" else "Demo City",
        isCachedOrFake = true,
    )

    private fun weatherCodeLabel(code: Int): String = when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Foggy"
        51, 53, 55, 56, 57 -> "Drizzle"
        61, 63, 65, 66, 67 -> "Rain"
        71, 73, 75, 77 -> "Snow"
        80, 81, 82 -> "Rain showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Mixed conditions"
    }
}
