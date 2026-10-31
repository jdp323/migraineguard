package com.migraineguard.app.core.data.db

import androidx.room.*
import com.migraineguard.app.feature.body.BodyMeasurements
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val dateIso: String,
    val measurementsCm: Map<String, Float>,
    val notes: String = "",
    val modifiedAtMillis: Long,
    val deleted: Boolean = false,
) {
    fun validate() {
        require(id.matches(Regex("[a-zA-Z0-9-]{1,64}")))
        require(dateIso.length == 10); java.time.LocalDate.parse(dateIso)
        require(notes.length <= 2000 && modifiedAtMillis >= 0)
        require(measurementsCm.size <= 28 && measurementsCm.keys.all(BodyMeasurements::knownKey))
        require(measurementsCm.keys.count { it.startsWith("custom:") } <= 8)
        require(measurementsCm.values.all { it.isFinite() && it > 0f && it <= 500f })
        require(if (deleted) measurementsCm.isEmpty() && notes.isEmpty() else measurementsCm.isNotEmpty())
    }
}

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements WHERE deleted = 0 ORDER BY dateIso DESC, modifiedAtMillis DESC")
    fun observe(): Flow<List<BodyMeasurementEntity>>
    @Query("SELECT * FROM body_measurements") suspend fun all(): List<BodyMeasurementEntity>
    @Query("SELECT * FROM body_measurements WHERE id = :id") suspend fun get(id: String): BodyMeasurementEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(entry: BodyMeasurementEntity)
}
