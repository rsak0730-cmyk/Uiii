package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.model.NeonTheme
import com.example.model.TextEffect

@Composable
fun StylizedText(
    text: String,
    effect: TextEffect,
    theme: NeonTheme,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Start
) {
    val infiniteTransition = rememberInfiniteTransition(label = "text_effect")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_offset"
    )

    when (effect) {
        TextEffect.SOLID -> {
            Text(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = Color.White,
                textAlign = textAlign
            )
        }
        TextEffect.GRADIENT -> {
            val gradientBrush = Brush.linearGradient(
                colors = listOf(theme.primary, theme.accent, Color.White),
                start = Offset(0f, 0f),
                end = Offset(400f, 400f)
            )
            Text(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontWeight = fontWeight,
                style = TextStyle(brush = gradientBrush),
                textAlign = textAlign
            )
        }
        TextEffect.AURORA -> {
            val auroraBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF00FFC5),
                    Color(0xFF7E00FF),
                    Color(0xFFFF007F),
                    Color(0xFF00E5FF)
                ),
                start = Offset(animOffset % 800f, 0f),
                end = Offset((animOffset % 800f) + 600f, 400f)
            )
            Text(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontWeight = fontWeight,
                style = TextStyle(brush = auroraBrush),
                textAlign = textAlign
            )
        }
        TextEffect.GLOW -> {
            val glowShadow = Shadow(
                color = theme.primary,
                offset = Offset(0f, 0f),
                blurRadius = 14f
            )
            Text(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = Color.White,
                style = TextStyle(shadow = glowShadow),
                textAlign = textAlign
            )
        }
        TextEffect.GLASS -> {
            val glassBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFB3E5FC),
                    Color(0xFF00E5FF).copy(alpha = 0.9f),
                    Color.White.copy(alpha = 0.95f),
                    Color(0xFF80D8FF)
                ),
                start = Offset(animOffset % 600f, 0f),
                end = Offset((animOffset % 600f) + 300f, 200f)
            )
            Text(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontWeight = fontWeight,
                style = TextStyle(
                    brush = glassBrush,
                    shadow = Shadow(color = Color(0x8800E5FF), blurRadius = 8f)
                ),
                textAlign = textAlign
            )
        }
        TextEffect.METALLIC -> {
            val metallicBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFCFD8DC),
                    Color(0xFFECEFF1),
                    Color(0xFF90A4AE),
                    Color(0xFFFFFFFF),
                    Color(0xFF78909C)
                ),
                start = Offset(0f, 0f),
                end = Offset(300f, 150f)
            )
            Text(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontWeight = fontWeight,
                style = TextStyle(
                    brush = metallicBrush,
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.7f), offset = Offset(1f, 2f), blurRadius = 3f)
                ),
                textAlign = textAlign
            )
        }
        TextEffect.HOLOGRAPHIC -> {
            val holoBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF007F),
                    Color(0xFF00F0FF),
                    Color(0xFFFFEE00),
                    Color(0xFFB000FF),
                    Color(0xFF00FF88)
                ),
                start = Offset(animOffset % 500f, 0f),
                end = Offset((animOffset % 500f) + 400f, 300f)
            )
            Text(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontWeight = fontWeight,
                style = TextStyle(
                    brush = holoBrush,
                    shadow = Shadow(color = Color(0xFF00F0FF), blurRadius = 10f)
                ),
                textAlign = textAlign
            )
        }
        TextEffect.LIQUID -> {
            val liquidBrush = Brush.sweepGradient(
                colors = listOf(
                    theme.primary,
                    theme.accent,
                    Color(0xFF00FFFF),
                    theme.primary
                )
            )
            Text(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontWeight = fontWeight,
                style = TextStyle(brush = liquidBrush),
                textAlign = textAlign
            )
        }
        TextEffect.THREE_D -> {
            Box(modifier = modifier) {
                // Background shadow layer
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = Color.Black.copy(alpha = 0.9f),
                    textAlign = textAlign,
                    style = TextStyle(shadow = Shadow(color = Color(0xFF111111), offset = Offset(3f, 5f), blurRadius = 1f))
                )
                // Front extruded face
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = theme.primary,
                    textAlign = textAlign,
                    style = TextStyle(shadow = Shadow(color = Color.White.copy(alpha = 0.4f), offset = Offset(-1f, -1f), blurRadius = 2f))
                )
            }
        }
        TextEffect.OUTLINE -> {
            Box(modifier = modifier) {
                // Stroke outline
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    textAlign = textAlign,
                    style = TextStyle(
                        color = theme.primary,
                        drawStyle = Stroke(width = 3.5f, join = StrokeJoin.Round)
                    )
                )
                // Hollow center fill
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = Color(0xFF0A0C14),
                    textAlign = textAlign
                )
            }
        }
    }
}
