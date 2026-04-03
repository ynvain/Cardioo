package com.cardioo.presentation.util

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cardioo.domain.model.BpCategory


val Orange = Color(0xFFFFA726);
val Bordeaux = Color(0xFF911535);

@Composable
fun toggleButtonBorder(toggle: Boolean): BorderStroke {
    val toggledButtonBorder = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    val untoggledButtonBorder = ButtonDefaults.outlinedButtonBorder(true)
    return if (toggle) toggledButtonBorder
    else untoggledButtonBorder
}


fun categoryColor(category: BpCategory): Color {
    return when (category) {
        BpCategory.Hypotension -> Color(0xFF5C6BC0)
        BpCategory.Normal -> Color(0xFF009650)
        BpCategory.Elevated -> Color(0xFFBEDC39)
        BpCategory.HypertensionStage1 -> Orange
        BpCategory.HypertensionStage2 -> Color(0xFFF85C90)
        BpCategory.HypertensiveCrisis -> Bordeaux
    }
}