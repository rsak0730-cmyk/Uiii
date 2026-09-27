package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.service.AgentAccessibilityService
import com.example.service.BatteryMonitorService
import com.example.service.EnergyMode
import com.example.ui.components.DynamicIsland
import com.example.ui.components.StylizedText
import com.example.ui.components.UiStyleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: NeonTheme,
    onSelectTheme: (NeonTheme) -> Unit,
    currentUiStyle: UiDesignStyle,
    onSelectUiStyle: (UiDesignStyle) -> Unit,
    currentTextEffect: TextEffect,
    onSelectTextEffect: (TextEffect) -> Unit,
    islandConfig: DynamicIslandConfig,
    onUpdateIslandConfig: (DynamicIslandConfig) -> Unit,
    isEnergyAwareAi: Boolean,
    onToggleEnergyAwareAi: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isAccessibilityActive by AgentAccessibilityService.isServiceRunning.collectAsState()
    val batteryInfo by BatteryMonitorService.batteryInfo.collectAsState()

    var isEnabled by remember(islandConfig) { mutableStateOf(islandConfig.isEnabled) }
    var offsetX by remember(islandConfig) { mutableFloatStateOf(islandConfig.offsetX) }
    var offsetY by remember(islandConfig) { mutableFloatStateOf(islandConfig.offsetY) }
    var widthDp by remember(islandConfig) { mutableFloatStateOf(islandConfig.widthDp) }
    var cornerRadiusDp by remember(islandConfig) { mutableFloatStateOf(islandConfig.cornerRadiusDp) }
    var animDurationMs by remember(islandConfig) { mutableIntStateOf(islandConfig.animationDurationMs) }

    fun commitConfig() {
        onUpdateIslandConfig(
            DynamicIslandConfig(
                isEnabled = isEnabled,
                offsetX = offsetX,
                offsetY = offsetY,
                widthDp = widthDp,
                cornerRadiusDp = cornerRadiusDp,
                animationDurationMs = animDurationMs
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF090A10),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Agent Studio Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090A10))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Preview of Current Text Effect & Style
            UiStyleCard(
                style = currentUiStyle,
                theme = currentTheme,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ACTIVE STYLE & TEXT PREVIEW", color = currentTheme.primary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    StylizedText(
                        text = "Omni Agent System 4.0",
                        effect = currentTextEffect,
                        theme = currentTheme,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currentTheme.displayName} • ${currentUiStyle.displayName}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            // ENERGY-AWARE AI & BATTERY LIFE DASHBOARD
            SectionHeader("ENERGY-AWARE AI & BATTERY LIFE", currentTheme.primary)
            val batteryColor = when {
                batteryInfo.isCharging -> Color(0xFF00E676)
                batteryInfo.levelPercent > 50 -> currentTheme.primary
                batteryInfo.levelPercent in 20..50 -> Color(0xFFFFEA00)
                else -> Color(0xFFFF1744)
            }

            UiStyleCard(
                style = currentUiStyle,
                theme = currentTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("battery_dashboard_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Row: Battery Level & Charging Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(batteryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        batteryInfo.isCharging -> Icons.Default.BatteryChargingFull
                                        batteryInfo.levelPercent <= 15 -> Icons.Default.BatteryAlert
                                        else -> Icons.Default.BatteryStd
                                    },
                                    contentDescription = "Battery Status",
                                    tint = batteryColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${batteryInfo.levelPercent}%",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (batteryInfo.isCharging) Color(0x3300E676) else Color(0x22FFFFFF),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (batteryInfo.isCharging) "CHARGING" else batteryInfo.pluggedSource.uppercase(),
                                            color = if (batteryInfo.isCharging) Color(0xFF00E676) else Color.LightGray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Temp: ${batteryInfo.temperatureCelsius}°C • Saver: ${if (batteryInfo.isPowerSaveMode) "Active" else "Off"}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Mode Badge
                        Surface(
                            color = batteryColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, batteryColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = batteryInfo.energyMode.displayName,
                                color = batteryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Battery Progress Level Bar
                    LinearProgressIndicator(
                        progress = { batteryInfo.levelPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = batteryColor,
                        trackColor = Color(0xFF202434)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Energy AI Policy Info
                    Surface(
                        color = Color(0xFF0F121C),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = currentTheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Active Model Allocation: ${batteryInfo.energyMode.recommendedModel}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = batteryInfo.energyMode.description,
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Energy Aware Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Energy-Aware AI Optimization",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Auto-switches to fast Flash-Lite model when battery is under 20% to conserve device battery and avoid thermal throttling.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isEnergyAwareAi,
                            onCheckedChange = onToggleEnergyAwareAi,
                            colors = SwitchDefaults.colors(checkedThumbColor = currentTheme.primary),
                            modifier = Modifier.testTag("energy_aware_ai_switch")
                        )
                    }
                }
            }

            // 1. THEME COLOR CHANGE (8 Neon Options)
            SectionHeader("1. THEME COLOR CHANGE", currentTheme.primary)
            Text(
                "Select one of the 8 neon aesthetic colorways:",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (themeOption in NeonTheme.entries) {
                    val isSelected = themeOption == currentTheme
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(themeOption.primary)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSelectTheme(themeOption) }
                            .testTag("theme_color_${themeOption.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // 2. UI DESIGN STYLES (23 Distinct Styles)
            SectionHeader("2. UI DESIGN STYLES (${UiDesignStyle.entries.size} STYLES)", currentTheme.primary)
            Text(
                "Choose your architectural design language for cards, sheets, and dialogs:",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (rowStyles in UiDesignStyle.entries.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (style in rowStyles) {
                            val isSelected = style == currentUiStyle
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectUiStyle(style) }
                                    .testTag("style_${style.name.lowercase()}"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) currentTheme.primary.copy(alpha = 0.2f) else Color(0xFF141722),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) currentTheme.primary else Color(0xFF222634)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = style.displayName,
                                        color = if (isSelected) currentTheme.primary else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = style.description,
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                        if (rowStyles.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 3. TEXT COLOR & ANIMATION (10 Effects)
            SectionHeader("3. TEXT COLOR / ANIMATION EFFECTS", currentTheme.primary)
            Text(
                "Select rendering shader, gradient, or extrusion effect for typography:",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (effect in TextEffect.entries) {
                    val isSelected = effect == currentTextEffect
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTextEffect(effect) }
                            .testTag("text_effect_${effect.name.lowercase()}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) currentTheme.primary.copy(alpha = 0.15f) else Color(0xFF131620),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) currentTheme.primary else Color(0xFF202433)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = effect.displayName,
                                    color = if (isSelected) currentTheme.primary else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                StylizedText(
                                    text = "Preview: Gemini Powered",
                                    effect = effect,
                                    theme = currentTheme,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = currentTheme.primary)
                            }
                        }
                    }
                }
            }

            // 4. DYNAMIC ISLAND SETTINGS (Placing, X/Y, Size, Corner Rounding, Smoothness)
            SectionHeader("4. DYNAMIC ISLAND SETTINGS", currentTheme.primary)
            Text(
                "Enable, position, and customize your iOS-style agent status pill:",
                color = Color.Gray,
                fontSize = 12.sp
            )

            // Master Enable Toggle Card
            UiStyleCard(
                style = currentUiStyle,
                theme = currentTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dynamic_island_master_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Enable Dynamic Island",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (isEnabled) Color(0x3300E676) else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isEnabled) "ACTIVE" else "DISABLED",
                                        color = if (isEnabled) Color(0xFF00E676) else Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Floats at the top of your screen to show live agent activity (processing, listening, dialing calls, and voicemails). Tap it anytime to expand!",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = {
                                isEnabled = it
                                commitConfig()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = currentTheme.primary),
                            modifier = Modifier.testTag("dynamic_island_enable_switch")
                        )
                    }

                    if (isEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "QUICK POSITION PRESETS",
                            color = currentTheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    offsetX = 0f
                                    offsetY = 12f
                                    widthDp = 220f
                                    cornerRadiusDp = 28f
                                    commitConfig()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2232)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Standard", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                onClick = {
                                    offsetX = 0f
                                    offsetY = 6f
                                    widthDp = 170f
                                    cornerRadiusDp = 24f
                                    commitConfig()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2232)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Compact", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                onClick = {
                                    offsetX = 0f
                                    offsetY = 14f
                                    widthDp = 290f
                                    cornerRadiusDp = 30f
                                    commitConfig()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2232)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Expanded", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Dynamic Island Live Simulation Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F1118))
                    .border(1.dp, Color(0xFF222634), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                DynamicIsland(
                    status = DynamicIslandStatus.EXECUTING,
                    statusDetail = "Simulated Live Island Preview",
                    theme = currentTheme,
                    config = DynamicIslandConfig(
                        isEnabled = isEnabled,
                        offsetX = offsetX,
                        offsetY = offsetY.coerceIn(4f, 40f),
                        widthDp = widthDp.coerceIn(140f, 320f),
                        cornerRadiusDp = cornerRadiusDp,
                        animationDurationMs = animDurationMs
                    ),
                    onIslandClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Sliders
            SliderItem("X-Axis Position", "${offsetX.toInt()} dp", offsetX, -60f..60f) {
                offsetX = it
                commitConfig()
            }

            SliderItem("Y-Axis Position", "${offsetY.toInt()} dp", offsetY, 0f..60f) {
                offsetY = it
                commitConfig()
            }

            SliderItem("Pill Size (Width)", "${widthDp.toInt()} dp", widthDp, 140f..340f) {
                widthDp = it
                commitConfig()
            }

            SliderItem("Corner Rounding Count", "${cornerRadiusDp.toInt()} dp", cornerRadiusDp, 8f..38f) {
                cornerRadiusDp = it
                commitConfig()
            }

            SliderItem("Animation Smoothness (Duration)", "$animDurationMs ms", animDurationMs.toFloat(), 150f..800f) {
                animDurationMs = it.toInt()
                commitConfig()
            }

            // Timeline / Smoothness Graph visualization
            TimelineSmoothnessGraph(animDurationMs = animDurationMs, primaryColor = currentTheme.primary)

            // 5. ACCESSIBILITY & VOLUME UP SHORTCUT
            SectionHeader("5. SYSTEM SHORTCUT & ACCESSIBILITY", currentTheme.primary)
            UiStyleCard(
                style = currentUiStyle,
                theme = currentTheme,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Accessibility Service", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                if (isAccessibilityActive) "Status: Enabled & Active" else "Status: Not Enabled in System Settings",
                                color = if (isAccessibilityActive) Color(0xFF00E676) else Color(0xFFFF5252),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isAccessibilityActive,
                            onCheckedChange = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = currentTheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Volume Up Button Shortcut: When accessibility is enabled, tapping the Volume Up key immediately starts voice listening with the Dynamic Island animation. Pressing again turns off listening.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            AgentAccessibilityService.triggerVolumeShortcut()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Volume Up Button Press", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        letterSpacing = 1.sp
    )
}

@Composable
fun SliderItem(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.LightGray, fontSize = 12.sp)
            Text(valueText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF00E5FF)
            )
        )
    }
}

@Composable
fun TimelineSmoothnessGraph(animDurationMs: Int, primaryColor: Color) {
    Column {
        Text("Animation Easing Timeline Graph (FastOutSlowIn)", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF10131C))
                .border(1.dp, Color(0xFF202434), RoundedCornerShape(8.dp))
        ) {
            val width = size.width
            val height = size.height
            val path = Path()

            path.moveTo(0f, height - 10f)
            // Cubic bezier reflecting FastOutSlowIn curve
            path.cubicTo(
                width * 0.4f, height - 10f,
                width * 0.2f, 10f,
                width, 10f
            )

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // Current timeline cursor based on duration
            val cursorX = ((animDurationMs - 150f) / 650f) * width
            drawLine(
                color = Color.White,
                start = Offset(cursorX, 0f),
                end = Offset(cursorX, height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
