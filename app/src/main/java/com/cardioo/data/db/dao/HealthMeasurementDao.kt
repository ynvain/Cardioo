package com.cardioo.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cardioo.data.db.entity.HealthMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMeasurementDao {
    @Query("SELECT * FROM health_measurement WHERE userId = :userId ORDER BY timestampEpochMillis DESC")
    fun observeAllForUser(userId: Long): Flow<List<HealthMeasurementEntity>>

    /**
     * First page of measurements, ordered newest → oldest.
     *
     * We use keyset/cursor pagination (instead of LIMIT/OFFSET) because OFFSET becomes unstable
     * when new rows are inserted (or deleted) while the user is scrolling: items can shift between
     * pages and cause duplicates.
     */
    @Query(
        "SELECT * FROM health_measurement WHERE userId = :userId " +
            "ORDER BY timestampEpochMillis DESC, id DESC " +
            "LIMIT :limit",
    )
    suspend fun getFirstPageForUser(userId: Long, limit: Int): List<HealthMeasurementEntity>

    /**
     * Next page of measurements strictly older than the provided cursor.
     *
     * Cursor is (timestampEpochMillis, id) of the last item currently shown.
     * The secondary sort by id guarantees deterministic ordering even when multiple rows have the
     * same timestamp.
     */
    @Query(
        "SELECT * FROM health_measurement WHERE userId = :userId AND " +
            "(timestampEpochMillis < :beforeTimestamp OR (timestampEpochMillis = :beforeTimestamp AND id < :beforeId)) " +
            "ORDER BY timestampEpochMillis DESC, id DESC " +
            "LIMIT :limit",
    )
    suspend fun getNextPageForUser(
        userId: Long,
        beforeTimestamp: Long,
        beforeId: Long,
        limit: Int,
    ): List<HealthMeasurementEntity>

    @Query("SELECT COUNT(*) FROM health_measurement WHERE userId = :userId")
    fun observeCountForUser(userId: Long): Flow<Int>

    @Query("SELECT * FROM health_measurement WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getByIdForUser(id: Long, userId: Long): HealthMeasurementEntity?

    @Upsert
    suspend fun upsert(entity: HealthMeasurementEntity): Long

    @Query("DELETE FROM health_measurement WHERE id = :id AND userId = :userId")
    suspend fun deleteByIdForUser(id: Long, userId: Long)
}

