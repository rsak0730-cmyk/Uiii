package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.NeonTheme
import com.example.model.UiDesignStyle

@Composable
fun UiStyleCard(
    style: UiDesignStyle,
    theme: NeonTheme,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = when (style) {
        UiDesignStyle.PIXEL_UI -> RoundedCornerShape(2.dp)
        UiDesignStyle.NEOBRUTALISM -> RoundedCornerShape(4.dp)
        UiDesignStyle.INFLATED_UI, UiDesignStyle.CLAYMORPHISM -> RoundedCornerShape(24.dp)
        else -> RoundedCornerShape(cornerRadius)
    }

    val boxModifier = when (style) {
        UiDesignStyle.NEOBRUTALISM -> {
            modifier
                .shadow(elevation = 6.dp, shape = shape, spotColor = Color.Black, ambientColor = Color.Black)
                .clip(shape)
                .background(Color(0xFF1E222D))
                .border(width = 2.5.dp, color = theme.primary, shape = shape)
        }
        UiDesignStyle.CYBERPUNK -> {
            modifier
                .shadow(elevation = 10.dp, shape = shape, spotColor = theme.primary, ambientColor = theme.accent)
                .clip(shape)
                .background(Color(0xFF0F111A))
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(theme.primary, Color.Transparent, theme.accent)),
                    shape = shape
                )
        }
        UiDesignStyle.GLASSMORPHISM, UiDesignStyle.GLASS_NEUMORPHISM -> {
            modifier
                .shadow(elevation = 8.dp, shape = shape, spotColor = theme.primary.copy(alpha = 0.3f))
                .clip(shape)
                .background(Color(0x331F293D))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))),
                    shape = shape
                )
        }
        UiDesignStyle.AERO_GLASSMORPHISM, UiDesignStyle.FRUTIGER_AERO -> {
            modifier
                .shadow(elevation = 12.dp, shape = shape, spotColor = Color(0xFF00E5FF))
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x66184E77), Color(0x331E6091), Color(0x551A759F))
                    )
                )
                .border(width = 1.2.dp, color = Color(0x8872EFDD), shape = shape)
        }
        UiDesignStyle.AURORAMORPHISM -> {
            modifier
                .shadow(elevation = 10.dp, shape = shape, spotColor = theme.primary)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0x5503071E), Color(0x44370617), Color(0x4403045E), Color(0x440077B6))
                    )
                )
                .border(width = 1.dp, color = theme.primary.copy(alpha = 0.6f), shape = shape)
        }
        UiDesignStyle.CLAYMORPHISM, UiDesignStyle.INFLATED_UI -> {
            modifier
                .shadow(elevation = 14.dp, shape = shape, spotColor = theme.primary.copy(alpha = 0.4f))
                .clip(shape)
                .background(Color(0xFF222634))
                .border(width = 3.dp, color = Color.White.copy(alpha = 0.2f), shape = shape)
        }
        UiDesignStyle.METALLICMORPHISM -> {
            modifier
                .shadow(elevation = 8.dp, shape = shape)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2B303A), Color(0xFF434955), Color(0xFF1E222A))
                    )
                )
                .border(width = 1.5.dp, color = Color(0xFFB0BEC5), shape = shape)
        }
        UiDesignStyle.HOLOGRAPHIC_UI, UiDesignStyle.Y2K_UI -> {
            modifier
                .shadow(elevation = 12.dp, shape = shape, spotColor = Color(0xFFFF007F))
                .clip(shape)
                .background(Color(0x44140152))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF00F0FF), Color(0xFFFFEA00))),
                    shape = shape
                )
        }
        UiDesignStyle.PIXEL_UI -> {
            modifier
                .clip(shape)
                .background(Color(0xFF10141E))
                .border(width = 2.dp, color = theme.primary, shape = shape)
        }
        UiDesignStyle.GRADIENTMORPHISM -> {
            modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(theme.primary.copy(alpha = 0.35f), Color(0x3310131C), theme.accent.copy(alpha = 0.35f))
                    )
                )
                .border(width = 1.dp, color = theme.primary.copy(alpha = 0.5f), shape = shape)
        }
        else -> {
            // Soft UI, Neumorphism, Skeuomorphism, Material, Fluent, 3D, Paper, etc.
            modifier
                .shadow(elevation = 6.dp, shape = shape, spotColor = theme.primary.copy(alpha = 0.25f))
                .clip(shape)
                .background(Color(0xFF161922))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.12f), shape = shape)
        }
    }

    Box(modifier = boxModifier, content = content)
}
