package com.example.plant_care_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PlantColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = LightMint,
    onPrimaryContainer = DarkGreen,

    secondary = SoilBrown,
    onSecondary = Color.White,
    secondaryContainer = LightSoil,
    onSecondaryContainer = DarkSoil,

    tertiary = Sunlight,
    onTertiary = Color.White,
    tertiaryContainer = LightSunlight,
    onTertiaryContainer = Color(0xFFF57F17),

    background = OffWhite,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = SageVariant,
    onSurfaceVariant = SageOnVariant,
    outline = SageOutline,
)

@Composable
fun PlantCareAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PlantColorScheme,
        typography = Typography,
        content = content
    )
}
