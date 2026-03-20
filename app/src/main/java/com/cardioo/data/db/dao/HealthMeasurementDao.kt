package com.cardioo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cardioo.data.db.entity.HealthMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMeasurementDao {
    @Query("SELECT * FROM health_measurement WHERE userId = :userId ORDER BY timestampEpochMillis DESC")
    fun observeAllForUser(userId: Long): Flow<List<HealthMeasurementEntity>>

    @Query("SELECT * FROM health_measurement WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getByIdForUser(id: Long, userId: Long): HealthMeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HealthMeasurementEntity): Long

    @Query("DELETE FROM health_measurement WHERE id = :id AND userId = :userId")
    suspend fun deleteByIdForUser(id: Long, userId: Long)
}

