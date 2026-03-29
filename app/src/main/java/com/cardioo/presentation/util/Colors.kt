package com.cardioo.presentation.util

import androidx.compose.ui.graphics.Color
import com.cardioo.domain.model.BpCategory

fun categoryColor(category: BpCategory): Color {
    return when (category) {
        BpCategory.Hypotension -> Color(0xFF5C6BC0)
        BpCategory.Normal -> Color(0xFF009650)
        BpCategory.Elevated -> Color(0xFFBEDC39)
        BpCategory.HypertensionStage1 -> Color(0xFFFFA726)
        BpCategory.HypertensionStage2 -> Color(0xFFF85C90)
        BpCategory.HypertensiveCrisis -> Color(0xFF911535)
    }
}