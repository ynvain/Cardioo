package com.cardioo.domain.model

/**
 * Adult office BP categories aligned with common ACC/AHA 2017-style thresholds, plus
 * hypotension (low) and hypertensive crisis (emergency) bands used in many clinical references.
 *
 * Classification uses the more severe category when systolic and diastolic fall in different bands.
 */
enum class BpCategory() {
    Hypotension,
    Normal,

    /** SBP 120–129 mmHg and diastolic below 80 mmHg (ACC/AHA “elevated”). */
    Elevated,
    HypertensionStage1,
    HypertensionStage2,
    HypertensiveCrisis,
}

fun bpCategory(systolic: Int, diastolic: Int): BpCategory {
    return when {
        systolic >= 180 || diastolic >= 120 -> BpCategory.HypertensiveCrisis
        systolic >= 140 || diastolic >= 90 -> BpCategory.HypertensionStage2
        systolic in 130..139 || diastolic in 80..89 -> BpCategory.HypertensionStage1
        systolic in 120..129 -> BpCategory.Elevated
        systolic < 90 || diastolic < 60 -> BpCategory.Hypotension
        else -> BpCategory.Normal
    }
}

