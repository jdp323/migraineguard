package com.migraineguard.app.core.data.db

import androidx.room.*

/** Local-only receipts make a notification's three alternatives one atomic choice. */
@Entity(tableName = "water_action_receipts")
data class WaterActionReceipt(@PrimaryKey val token: String, val appliedAtMillis: Long)

@Dao
abstract class WaterActionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun claim(receipt: WaterActionReceipt): Long
    @Query("INSERT OR IGNORE INTO water_history(dateIso,cups) VALUES(:date,0)")
    abstract suspend fun ensureDay(date: String)
    @Query("UPDATE water_history SET cups = MIN(1000,cups + :amount) WHERE dateIso = :date")
    abstract suspend fun increment(date: String, amount: Int)
    @Query("DELETE FROM water_action_receipts WHERE appliedAtMillis < :cutoff")
    abstract suspend fun prune(cutoff: Long)
    @Transaction
    open suspend fun addOnce(token: String, date: String, amount: Int, now: Long): Boolean {
        require(amount in 1..3 && token.length in 1..100)
        if (claim(WaterActionReceipt(token,now)) == -1L) return false
        ensureDay(date); increment(date,amount); prune(now - 172_800_000L)
        return true
    }
}
