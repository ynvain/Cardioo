package com.cardioo.domain.model

enum class WeightUnit { KG, LB }

enum class HeightUnit { CM, IN }

enum class Gender { Male, Female}

fun WeightUnit.displayName(): String = when (this) {
    WeightUnit.KG -> "kg"
    WeightUnit.LB -> "lb"
}

fun HeightUnit.displayName(): String = when (this) {
    HeightUnit.CM -> "cm"
    HeightUnit.IN -> "in"
}

fun WeightUnit.toggle(): WeightUnit = if (this == WeightUnit.KG) WeightUnit.LB else WeightUnit.KG

fun HeightUnit.toggle(): HeightUnit = if (this == HeightUnit.CM) HeightUnit.IN else HeightUnit.CM

fun poundsToKg(lb: Double): Double = lb * 0.45359237
fun kgToPounds(kg: Double): Double = kg / 0.45359237

fun inchesToMeters(inches: Double): Double = inches * 0.0254
fun cmToMeters(cm: Double): Double = cm / 100.0

