package com.cardioo.domain.model

import kotlinx.datetime.LocalDate

data class UserProfile(
    val id: Long = 0L,
    val name: String,
    val height: Double,
    val heightUnit: HeightUnit,
    val weightUnit: WeightUnit,
    val dateOfBirth: LocalDate?,
    val gender: Gender?,
)

