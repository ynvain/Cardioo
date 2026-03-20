package com.cardioo.data.mapper

import com.cardioo.data.db.entity.HealthMeasurementEntity
import com.cardioo.data.db.entity.UserEntity
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.UserProfile
import kotlinx.datetime.LocalDate

fun UserEntity.toDomain(): UserProfile =
    UserProfile(
        id = id,
        name = name,
        height = height,
        heightUnit = heightUnit,
        weightUnit = weightUnit,
        dateOfBirth = dateOfBirthIso?.let { LocalDate.parse(it) },
        gender = gender,
    )

fun UserProfile.toEntity(): UserEntity =
    UserEntity(
        id = id,
        name = name,
        height = height,
        heightUnit = heightUnit,
        weightUnit = weightUnit,
        dateOfBirthIso = dateOfBirth?.toString(),
        gender = gender,
    )

fun HealthMeasurementEntity.toDomain(): HealthMeasurement =
    HealthMeasurement(
        id = id,
        userId = userId,
        timestampEpochMillis = timestampEpochMillis,
        systolic = systolic,
        diastolic = diastolic,
        pulse = pulse,
        weight = weight,
        weightUnit = weightUnit,
        notes = notes,
    )

fun HealthMeasurement.toEntity(): HealthMeasurementEntity =
    HealthMeasurementEntity(
        id = id,
        userId = userId,
        timestampEpochMillis = timestampEpochMillis,
        systolic = systolic,
        diastolic = diastolic,
        pulse = pulse,
        weight = weight,
        weightUnit = weightUnit,
        notes = notes,
    )

