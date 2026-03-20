package com.cardioo.domain.model

enum class BpCategory(val label: String) {
    Normal("Normal"),
    Elevated("Elevated"),
    HypertensionStage1("Hypertension Stage 1"),
    HypertensionStage2("Hypertension Stage 2"),
}

fun bpCategory(systolic: Int, diastolic: Int): BpCategory {
    // Simplified ACC/AHA 2017-style ranges.
    // Intentionally uses "worst of" systolic/diastolic to match common clinical categorization.
    return when {
        systolic >= 140 || diastolic >= 90 -> BpCategory.HypertensionStage2
        systolic in 130..139 || diastolic in 80..89 -> BpCategory.HypertensionStage1
        systolic in 120..129 && diastolic < 80 -> BpCategory.Elevated
        else -> BpCategory.Normal
    }
}

