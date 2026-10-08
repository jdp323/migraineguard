package com.migraineguard.app.feature.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.migraineguard.app.core.ai.executeRequest
import com.migraineguard.app.core.domain.WeatherSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Request
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.math.round

interface WeatherRepository {
    suspend fun currentWeather(forceRefresh: Boolean = false): WeatherSnapshot?
}

class OpenMeteoWeatherRepository(private val context: Context) : WeatherRepository {
    @Volatile private var cache: WeatherSnapshot? = null

    override suspend fun currentWeather(forceRefresh: Boolean): WeatherSnapshot? {
        val now = System.currentTimeMillis()
        val previous = cache
        if (!forceRefresh && previous != null && now - previous.observedAtMillis < 30 * 60_000L) return previous
        val coords = coordinates()
        val fresh = coords?.let { fetch(it.first, it.second) }
        if (fresh != null) { cache = fresh; return fresh }
        return previous?.copy(isCachedOrFake = true, locationLabel = "Last available weather · Open-Meteo")
    }

    @SuppressLint("MissingPermission")
    private suspend fun coordinates(): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) return null
        return try {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
                .firstOrNull { manager.isProviderEnabled(it) } ?: return null
            val last = manager.getLastKnownLocation(provider)?.takeIf {
                System.currentTimeMillis() - it.time <= 6 * 60 * 60_000L
            }
            val location = last ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                withTimeoutOrNull(10_000) {
                    suspendCancellableCoroutine<Location?> { continuation ->
                        val signal = CancellationSignal()
                        continuation.invokeOnCancellation { signal.cancel() }
                        manager.getCurrentLocation(provider, signal, ContextCompat.getMainExecutor(context)) {
                            if (continuation.isActive) continuation.resume(it)
                        }
                    }
                }
            } else null
            location?.let { round(it.latitude * 100) / 100 to round(it.longitude * 100) / 100 }
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { null }
    }

    private suspend fun fetch(lat: Double, lon: Double): WeatherSnapshot? = try {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,relative_humidity_2m,surface_pressure,weather_code&timezone=auto"
        val response = executeRequest(Request.Builder().url(url).get().build())
        if (response.code !in 200..299) null else {
            val current = JSONObject(response.body).getJSONObject("current")
            WeatherSnapshot(
                temperatureC = current.getDouble("temperature_2m").toFloat(),
                pressureHpa = current.getDouble("surface_pressure").toFloat(),
                humidityPercent = current.getInt("relative_humidity_2m"),
                condition = weatherCodeLabel(current.getInt("weather_code")),
                locationLabel = "Local weather · Open-Meteo",
                observedAtMillis = System.currentTimeMillis(),
            )
        }
    } catch (e: CancellationException) { throw e }
    catch (_: Exception) { null }

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
