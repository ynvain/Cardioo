package com.cardioo.presentation.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cardioo.R
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

@Composable
fun AccountAvatar(
    name: String,
    background: Color = avatarColor(name),
    size: Dp,
) {
    val initialsFallback = stringResource(R.string.avatar_initials_fallback)
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
    val contentColor = if (background.luminance() > 0.6f) Color.Black else Color.White
    Box(
        modifier = Modifier
            .size(size)
            .background(background, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifBlank { initialsFallback },
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,

            )
    }
}

private fun avatarColor(seed: String): Color {
    val palette = listOf(
        Color(0xFFFF6B8B),
        Color(0xFF7E57C2),
        Color(0xFF26A69A),
        Color(0xFF42A5F5),
        Color(0xFFFFA726),
        Color(0xFFEC407A),
    )
    val idx = kotlin.math.abs(seed.hashCode()) % palette.size
    return palette[idx]
}



