package com.cardioo.presentation.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.cardioo.domain.model.BpCategory

@Composable
fun ColoredCategoryText(category: BpCategory) {
    val color = categoryColor(category);
    val contentColor = if (color.luminance() > 0.6f) Color.Black else Color.White
    Text(
        localizeBpCategory(category),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
    )
}



