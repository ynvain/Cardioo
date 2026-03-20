package com.cardioo.domain.repository

import com.cardioo.domain.model.HealthMeasurement
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {
    fun observeAll(): Flow<List<HealthMeasurement>>
    suspend fun getById(id: Long): HealthMeasurement?
    suspend fun upsert(measurement: HealthMeasurement): Long
    suspend fun delete(id: Long)
}

