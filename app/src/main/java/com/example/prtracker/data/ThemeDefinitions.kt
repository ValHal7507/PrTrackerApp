package com.example.prtracker.data

data class AppTheme(
    val id: String,
    val name: String,
    val description: String,
    val settings: AppearanceSettings
)

val ALL_THEMES: List<AppTheme> = listOf(
    AppTheme(
        id = "default",
        name = "SYSTEM DEFAULT",
        description = "Original cyberpunk cyan & purple",
        settings = AppearanceSettings()
    ),
    AppTheme(
        id = "blood_iron",
        name = "BLOOD & IRON",
        description = "Warrior red dominance",
        settings = AppearanceSettings(
            exerciseAccent = 0xFFFF003C,
            exerciseSecondary = 0xFFFF6B00,
            pinnedAccent = 0xFFFF6B00,
            pinnedSecondary = 0xFFFF003C,
            runningAccent = 0xFFFF2D78,
            runningSecondary = 0xFFC4005A,
            systemAccent = 0xFFFF003C,
            systemSecondary = 0xFFFF6B00,
            activeThemeId = "blood_iron"
        )
    ),
    AppTheme(
        id = "toxic_green",
        name = "TOXIC OVERRIDE",
        description = "Radioactive lime assault",
        settings = AppearanceSettings(
            exerciseAccent = 0xFFAAFF00,
            exerciseSecondary = 0xFF00FF85,
            pinnedAccent = 0xFF00FF85,
            pinnedSecondary = 0xFFAAFF00,
            runningAccent = 0xFFAAFF00,
            runningSecondary = 0xFF77CC00,
            systemAccent = 0xFFAAFF00,
            systemSecondary = 0xFF00FF85,
            activeThemeId = "toxic_green"
        )
    ),
    AppTheme(
        id = "void_purple",
        name = "VOID PROTOCOL",
        description = "Deep space full purple immersion",
        settings = AppearanceSettings(
            exerciseAccent = 0xFFB026FF,
            exerciseSecondary = 0xFF7B2FFF,
            pinnedAccent = 0xFF7B2FFF,
            pinnedSecondary = 0xFFB026FF,
            runningAccent = 0xFFFF2D78,
            runningSecondary = 0xFF7B2FFF,
            systemAccent = 0xFFB026FF,
            systemSecondary = 0xFF7B2FFF,
            activeThemeId = "void_purple"
        )
    ),
    AppTheme(
        id = "solar_storm",
        name = "SOLAR STORM",
        description = "Amber heat meets plasma white",
        settings = AppearanceSettings(
            exerciseAccent = 0xFFFFB800,
            exerciseSecondary = 0xFFFF6B00,
            pinnedAccent = 0xFFFF6B00,
            pinnedSecondary = 0xFFFFB800,
            runningAccent = 0xFFFF8C00,
            runningSecondary = 0xFFFFB800,
            systemAccent = 0xFFFFB800,
            systemSecondary = 0xFFFF6B00,
            activeThemeId = "solar_storm"
        )
    ),
    AppTheme(
        id = "ice_protocol",
        name = "ICE PROTOCOL",
        description = "Frozen blue white minimal",
        settings = AppearanceSettings(
            exerciseAccent = 0xFF00CFFF,
            exerciseSecondary = 0xFFC8C8FF,
            pinnedAccent = 0xFFC8C8FF,
            pinnedSecondary = 0xFF00CFFF,
            runningAccent = 0xFF00CFFF,
            runningSecondary = 0xFF0090BB,
            systemAccent = 0xFF00CFFF,
            systemSecondary = 0xFFC8C8FF,
            activeThemeId = "ice_protocol"
        )
    )
)
