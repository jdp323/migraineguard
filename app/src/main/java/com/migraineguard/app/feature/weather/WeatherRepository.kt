package com.migraineguard.app.feature.weather

import com.migraineguard.app.core.domain.WeatherSnapshot

/**
 * GPS-backed weather / pressure / temperature.
 * Fake impl for now; later Open-Meteo or similar + FusedLocationProvider.
 */
interface WeatherRepository {
    suspend fun currentWeather(): WeatherSnapshot
}

class FakeWeatherRepository : WeatherRepository {
    override suspend fun currentWeather(): WeatherSnapshot = WeatherSnapshot(
        temperatureC = 21.5f,
        pressureHpa = 1013.2f,
        condition = "Partly cloudy (fake)",
        locationLabel = "Demo City · GPS stub",
    )
}
