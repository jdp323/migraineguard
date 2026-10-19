package com.migraineguard.app

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.testing.*
import androidx.health.connect.client.testing.stubs.stub
import androidx.health.connect.client.testing.stubs.Stub
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.feature.fitness.HealthConnectHealthDataRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CancellationException
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class HealthReadingTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val now = Instant.parse("2026-10-19T05:07:00Z")
    private fun repo(fake: FakeHealthConnectClient) = HealthConnectHealthDataRepository(
        context, fake, { HealthConnectClient.SDK_AVAILABLE }, { now }, readPageSize = 2)
    private fun heart(time: Instant, bpm: Long) = HeartRateRecord(
        startTime = time.minusSeconds(60), startZoneOffset = null,
        endTime = time.plusSeconds(1), endZoneOffset = null,
        samples = listOf(HeartRateRecord.Sample(time, bpm)), metadata = Metadata.manualEntry())
    private fun sleep(end: Instant) = SleepSessionRecord(
        startTime = end.minusSeconds(8 * 3600), startZoneOffset = null,
        endTime = end, endZoneOffset = null, metadata = Metadata.manualEntry())

    @Test fun grantedButEmptyDoesNotClaimPermissionDenied() = runBlocking {
        val fake = FakeHealthConnectClient()
        fake.overrides.aggregate = stub { AggregationResult(metrics = emptyMap()) }
        val repo = repo(fake)
        val fitness = repo.latestFitness()
        assertNull(fitness.observedSteps)
        assertTrue(fitness.stepSync!!.message.contains("returned no steps"))
        assertTrue(fitness.permissionSummary.contains("Steps: allowed"))
        assertTrue(fitness.heartRateStatus.contains("no heart rate samples"))
        assertTrue(repo.latestSleep().sourceLabel.contains("no sleep sessions"))
        assertEquals("", repo.lastError())
    }

    @Test fun missingPermissionsAreReportedPerReading() = runBlocking {
        val fake = FakeHealthConnectClient(permissionController = FakePermissionController(grantAll = false))
        val repo = repo(fake)
        val fitness = repo.latestFitness()
        assertNull(fitness.observedSteps)
        assertTrue(fitness.stepSync!!.message.contains("not allowed"))
        assertTrue(fitness.heartRateStatus.contains("not allowed"))
        assertTrue(repo.latestSleep().sourceLabel.contains("not allowed"))
    }

    @Test fun samsungCountSurvivesCombinedAggregateFailure() = runBlocking {
        val fake = FakeHealthConnectClient()
        fake.overrides.aggregate = Stub { request ->
            if (request.dataOriginFilter.isEmpty()) throw IllegalStateException("fixture combined failure")
            AggregationResult(metrics = mapOf(StepsRecord.COUNT_TOTAL to 180L))
        }
        val result = repo(fake).latestFitness()
        assertEquals(180, result.observedSteps)
        assertEquals(180, result.stepSync!!.samsungTotal)
        assertTrue(result.sourceLabel.contains("Samsung"))
        assertTrue(result.stepSync!!.message.contains("combined Health Connect total is unavailable"))
    }

    @Test fun previousDayHeartAndNewestSleepAreFoundAcrossPages() = runBlocking {
        val fake = FakeHealthConnectClient()
        fake.overrides.aggregate = stub { AggregationResult(metrics = mapOf(StepsRecord.COUNT_TOTAL to 0L)) }
        val sampleTime = now.minusSeconds(4 * 3600)
        fake.insertRecords(listOf(heart(sampleTime.minusSeconds(900), 63), heart(sampleTime, 77),
            heart(sampleTime.minusSeconds(600), 65), heart(sampleTime.minusSeconds(300), 66),
            sleep(now.minusSeconds(24 * 3600)), sleep(now.minusSeconds(48 * 3600)),
            sleep(now.minusSeconds(3600))))
        val repo = repo(fake)
        val fitness = repo.latestFitness()
        assertEquals(0, fitness.observedSteps) // A real zero remains zero.
        assertEquals(77, fitness.heartRateBpm)
        assertEquals(sampleTime.toEpochMilli(), fitness.heartRateRecordedAtMillis)
        val sleep = repo.latestSleep()
        assertEquals(8f, sleep.recordedHours!!, 0f)
        assertEquals(now.minusSeconds(3600).toEpochMilli(), sleep.recordedAtMillis)
    }

    @Test fun olderSleepIsDatedAndExcludedFromCurrentInsights() = runBlocking {
        val fake = FakeHealthConnectClient()
        val end = now.minusSeconds(5 * 24 * 3600)
        fake.insertRecords(listOf(sleep(end)))
        val sleep = repo(fake).latestSleep()
        assertEquals(8f, sleep.hours, 0f)
        assertFalse(sleep.isRecent)
        assertNull(sleep.recordedHours)
        assertEquals(end.toEpochMilli(), sleep.recordedAtMillis)
    }

    @Test fun readErrorsAndCancellationAreNotReportedAsEmptyData() = runBlocking {
        val fake = FakeHealthConnectClient()
        fake.overrides.aggregate = stub { throw SecurityException("fixture revoked") }
        fake.overrides.readRecords = stub { throw IllegalStateException("fixture IPC error") }
        val repo = repo(fake)
        val fitness = repo.latestFitness()
        assertTrue(fitness.stepSync!!.message.contains("permission"))
        assertTrue(fitness.heartRateStatus.contains("read failed"))
        assertTrue(repo.latestSleep().sourceLabel.contains("read failed"))
        fake.overrides.aggregate = stub { throw CancellationException("fixture cancelled") }
        try { repo.latestFitness(); fail("Cancellation must propagate") }
        catch (_: CancellationException) { }
    }
}
