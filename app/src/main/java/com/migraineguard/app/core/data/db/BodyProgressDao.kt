package com.migraineguard.app.core.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "body_progress")
data class BodyProgressEntity(
    @PrimaryKey val id: String,
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

@Dao
interface BodyProgressDao {
    @Query("SELECT * FROM body_progress WHERE deleted = 0 ORDER BY dateIso DESC, modifiedAtMillis DESC")
    fun observe(): Flow<List<BodyProgressEntity>>
    @Query("SELECT * FROM body_progress") suspend fun all(): List<BodyProgressEntity>
    @Query("SELECT * FROM body_progress WHERE id = :id") suspend fun get(id: String): BodyProgressEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(entry: BodyProgressEntity)
}
