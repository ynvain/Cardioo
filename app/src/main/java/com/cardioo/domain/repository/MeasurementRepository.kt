package com.cardioo.domain.repository

import com.cardioo.domain.model.HealthMeasurement
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {
    fun observeAll(): Flow<List<HealthMeasurement>>
    fun observeCount(): Flow<Int>
    /**
     * Cursor-based paging for the readings list.
     *
     * - Pass null cursor to load the newest page.
     * - Pass (beforeTimestampEpochMillis, beforeId) from the last loaded item to load the next page.
     */
    suspend fun getPage(
        limit: Int,
        beforeTimestampEpochMillis: Long? = null,
        beforeId: Long? = null,
    ): List<HealthMeasurement>
    suspend fun getById(id: Long): HealthMeasurement?
    suspend fun upsert(measurement: HealthMeasurement): Long
    suspend fun delete(id: Long)
}

