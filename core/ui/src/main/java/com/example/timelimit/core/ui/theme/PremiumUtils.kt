package com.example.timelimit.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom modifier to add a soft glow effect behind a component.
 */
fun Modifier.glow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
) = this.drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = shadowColor
        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        canvas.drawRoundRect(
            0f, 0f, size.width, size.height,
            borderRadius.toPx(), borderRadius.toPx(),
            paint
        )
    }
}

/**
 * A Premium Glassmorphism card designed for the TimeLimit aesthetic.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) GlassBlack else GlassWhite
    val borderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(borderColor, Color.Transparent)
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}

/**
 * Standardized premium screen background with a subtle radial gradient.
 */
@Composable
fun PremiumBackground(
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val bgBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF1C1C1E),
                Color(0xFF000000)
            ),
            center = androidx.compose.ui.geometry.Offset(0f, 0f),
            radius = 2000f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF2F2F7)
            ),
            center = androidx.compose.ui.geometry.Offset(0f, 0f),
            radius = 2000f
        )
    }

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .background(bgBrush)
            .padding(0.dp)
    ) {
        content()
    }
}
