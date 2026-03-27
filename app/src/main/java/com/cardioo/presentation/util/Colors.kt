package com.cardioo.presentation.util

import androidx.compose.ui.graphics.Color
import com.cardioo.domain.model.BpCategory

fun categoryColor(category: BpCategory): Color {
    return when (category) {
        BpCategory.Normal -> Color(0xFF009688)
        BpCategory.Elevated -> Color(0xFFFFA726)
        BpCategory.HypertensionStage1 -> Color(0xFFF85C90)
        BpCategory.HypertensionStage2 -> Color(0xFF911535)
    }
}