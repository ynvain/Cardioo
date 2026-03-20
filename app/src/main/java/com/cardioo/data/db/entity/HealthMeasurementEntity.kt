package com.cardioo.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cardioo.domain.model.WeightUnit

@Entity(
    tableName = "health_measurement",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["timestampEpochMillis"]), Index(value = ["userId"])],
)
data class HealthMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val timestampEpochMillis: Long,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val weight: Double,
    val weightUnit: WeightUnit,
    val notes: String?,
)

