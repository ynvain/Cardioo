package com.cardioo.domain.model

data class HealthMeasurement(
    val id: Long = 0L,
    val userId: Long,
    val timestampEpochMillis: Long,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int?,
    val weight: Double?,
    val weightUnit: WeightUnit,
    val notes: String?,
)
