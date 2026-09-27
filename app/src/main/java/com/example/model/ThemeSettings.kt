package com.example.model

import androidx.compose.ui.graphics.Color

enum class NeonTheme(val displayName: String, val primary: Color, val accent: Color, val bgGlow: Color) {
    NEON_BLUE("Neon Blue", Color(0xFF00E5FF), Color(0xFF0088FF), Color(0x3300E5FF)),
    NEON_RED("Neon Red", Color(0xFFFF1744), Color(0xFFFF5252), Color(0x33FF1744)),
    NEON_GREEN("Neon Green", Color(0xFF00E676), Color(0xFF69F0AE), Color(0x3300E676)),
    NEON_YELLOW("Neon Yellow", Color(0xFFFFEA00), Color(0xFFFFD600), Color(0x33FFEA00)),
    NEON_ORANGE("Neon Orange", Color(0xFFFF6D00), Color(0xFFFF9100), Color(0x33FF6D00)),
    NEON_WHITE("Neon White", Color(0xFFF5F5F5), Color(0xFFE0E0E0), Color(0x33FFFFFF)),
    NEON_BROWN("Neon Brown", Color(0xFFD7CCC8), Color(0xFF8D6E63), Color(0x338D6E63)),
    NEON_PURPLE("Neon Purple", Color(0xFFD500F9), Color(0xFFAA00FF), Color(0x33D500F9))
}

enum class UiDesignStyle(val displayName: String, val description: String) {
    CYBERPUNK("Cyberpunk UI", "Sharp neon glows, angular borders, dark tech backdrop"),
    GLASSMORPHISM("Glassmorphism", "Translucent frosty panels, subtle borders, deep blurs"),
    AERO_GLASSMORPHISM("Aero Glassmorphism", "Vibrant gloss, specular highlights, airy gradients"),
    LIQUID_GLASS("Liquid Glass / Liquidmorphism", "Fluid organic contours, refraction, dynamic curves"),
    AURORAMORPHISM("Auroramorphism", "Ethereal northern lights background gradients"),
    NEOBRUTALISM("Brutalism / Neobrutalism", "High-contrast thick black outlines, hard offset drop-shadows"),
    SOFT_UI("Soft UI", "Subtle convex/concave curves, low-contrast gentle shadows"),
    NEUMORPHISM("Neumorphism", "Extruded soft tactile plastic feel, dual light & dark shadows"),
    CLAYMORPHISM("Claymorphism", "Playful 3D rounded clay-like depth, bouncy inner shadows"),
    SKEUOMORPHISM("Skeuomorphism", "Tactile realistic textures, metallic bevels, rich gradients"),
    FRUTIGER_AERO("Frutiger Aero", "Glossy skeuomorphic reflections, aqua water ripples, lens flares"),
    Y2K_UI("Y2K UI", "Early 2000s chrome badges, sci-fi techno typography"),
    HOLOGRAPHIC_UI("Holographic UI", "Iridescent rainbow shifts, laser grid scanlines"),
    MATERIAL_DESIGN("Material Design", "Clean elevation layers, ripple response, structured grid"),
    FLUENT_DESIGN("Fluent Design", "Acrylic materials, connected animations, subtle depth"),
    METALLICMORPHISM("Metallicmorphism", "Brushed aluminum, chrome reflections, beveled edge glints"),
    GLASS_NEUMORPHISM("Glassmorphic Neumorphism", "Frosted glass combined with tactile extruded drop depth"),
    GRADIENTMORPHISM("Gradientmorphism", "Bold multi-stop dynamic color morph gradients"),
    THREE_D_MORPHISM("3D Morphism", "Isometric 3D perspectives, floating layers with depth shadows"),
    PIXEL_UI("Pixel UI", "Retro 8-bit chunky block borders, arcade scanline accents"),
    RETRO_FUTURISTIC("Retro-futuristic UI", "Outrun synthwave grids, wireframe lines, CRT glow"),
    PAPER_MORPHISM("Paper / Material Morphism", "Layered tactile paper sheets, clean drop-cast shadows"),
    INFLATED_UI("Inflated UI", "Puffy balloon-like inflated volumes, glossy bubble roundedness")
}

enum class TextEffect(val displayName: String) {
    SOLID("Solid (Pure White)"),
    GRADIENT("Gradient (Multi-color)"),
    AURORA("Aurora (Animated Rainbow)"),
    GLOW("Glow (Neon Ambient)"),
    GLASS("Glass (Neon Blue Shimmer)"),
    METALLIC("Metallic (Chrome/Gold)"),
    HOLOGRAPHIC("Holographic (Iridescent)"),
    LIQUID("Liquid (Fluid Morph)"),
    THREE_D("3D (Extrusion & Shadows)"),
    OUTLINE("Outline (Transparent & Stroke)")
}

data class DynamicIslandConfig(
    val isEnabled: Boolean = true,
    val offsetX: Float = 0f, // -100 to +100 dp
    val offsetY: Float = 12f, // 0 to 80 dp
    val widthDp: Float = 220f, // 140 to 360 dp
    val cornerRadiusDp: Float = 28f, // 8 to 40 dp
    val animationDurationMs: Int = 300 // 150 to 800 ms
)

enum class DynamicIslandStatus(val label: String, val iconResName: String, val pulseActive: Boolean) {
    IDLE("Agent Omni Ready", "check_circle", false),
    LISTENING("Listening...", "mic", true),
    PROCESSING("Processing Gemini Brain...", "psychology", true),
    EXECUTING("Executing Action...", "bolt", true),
    DIALING("Connecting Call...", "phone_in_talk", true),
    VOICEMAIL("Recording Voicemail...", "voicemail", true),
    WATCHDOG("Watchdog Inspecting...", "visibility", true)
}
