package com.migraineguard.app.core.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class BodyProgressEntity(
    val id: String,
    val dateIso: String,
    val pose: String,
    val notes: String = "",
    val weightKg: Float? = null,
    val waistCm: Float? = null,
    val bodyFatPercent: Float? = null,
    val jpegBase64: String = "",
    val modifiedAtMillis: Long,
    val deleted: Boolean = false,
) {
    fun validate() {
        require(id.matches(Regex("[a-zA-Z0-9-]{1,64}")))
        java.time.LocalDate.parse(dateIso)
        require(pose in listOf("Front", "Side", "Back", "Other"))
        require(notes.length <= 2000 && modifiedAtMillis >= 0)
        require(weightKg == null || weightKg.isFinite() && weightKg > 0 && weightKg <= 1000) { "Weight must be greater than 0 and at most 1000 kg." }
        require(waistCm == null || waistCm.isFinite() && waistCm in 20f..300f) { "Waist must be between 20 and 300 cm." }
        require(bodyFatPercent == null || bodyFatPercent.isFinite() && bodyFatPercent in 1f..75f) { "Scale body fat must be between 1 and 75 percent." }
        require(jpegBase64.length <= 480_000)
        if (jpegBase64.isNotBlank()) {
            val bytes = java.util.Base64.getDecoder().decode(jpegBase64)
            require(bytes.size in 4..360_000 && bytes[0] == 0xff.toByte() && bytes[1] == 0xd8.toByte())
        }
        require(!deleted || jpegBase64.isEmpty())
    }
}

@Entity(tableName = "body_progress")
data class BodyProgressRow(
    @PrimaryKey val id: String,
    val dateIso: String,
    val pose: String,
    val notes: String,
    val weightKg: Float?,
    val waistCm: Float?,
    val bodyFatPercent: Float?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val jpegBytes: ByteArray,
    val modifiedAtMillis: Long,
    val deleted: Boolean,
) {
    fun portable() = BodyProgressEntity(id, dateIso, pose, notes, weightKg, waistCm, bodyFatPercent,
        java.util.Base64.getEncoder().encodeToString(jpegBytes), modifiedAtMillis, deleted)
    companion object {
        fun from(entry: BodyProgressEntity) = BodyProgressRow(entry.id, entry.dateIso, entry.pose, entry.notes,
            entry.weightKg, entry.waistCm, entry.bodyFatPercent, java.util.Base64.getDecoder().decode(entry.jpegBase64),
            entry.modifiedAtMillis, entry.deleted)
    }
}

@Dao
abstract class BodyProgressDao {
    @Query("SELECT * FROM body_progress WHERE deleted = 0 ORDER BY dateIso DESC, modifiedAtMillis DESC")
    abstract fun observeRows(): Flow<List<BodyProgressRow>>
    @Query("SELECT * FROM body_progress") abstract suspend fun allRows(): List<BodyProgressRow>
    @Query("SELECT * FROM body_progress WHERE id = :id") abstract suspend fun getRow(id: String): BodyProgressRow?
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun putRow(entry: BodyProgressRow)
    fun observe(): Flow<List<BodyProgressEntity>> = observeRows().map { rows -> rows.map { it.portable() } }
    suspend fun all(): List<BodyProgressEntity> = allRows().map { it.portable() }
    suspend fun get(id: String): BodyProgressEntity? = getRow(id)?.portable()
    suspend fun put(entry: BodyProgressEntity) { entry.validate(); putRow(BodyProgressRow.from(entry)) }
}
