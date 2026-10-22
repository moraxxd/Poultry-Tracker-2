package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenPrimaryDark,
    onPrimary = ForestGreenOnPrimaryDark,
    primaryContainer = ForestGreenPrimaryContainerDark,
    onPrimaryContainer = ForestGreenOnPrimaryContainerDark,
    secondary = SproutSecondaryDark,
    onSecondary = SproutOnSecondaryDark,
    secondaryContainer = SproutSecondaryContainerDark,
    onSecondaryContainer = SproutOnSecondaryContainerDark,
    tertiary = HarvestAmberTertiaryDark,
    onTertiary = HarvestAmberOnTertiaryDark,
    tertiaryContainer = HarvestAmberTertiaryContainerDark,
    onTertiaryContainer = HarvestAmberOnTertiaryContainerDark,
    background = FarmBackgroundDark,
    surface = FarmSurfaceDark,
    surfaceVariant = FarmSurfaceVariantDark,
    onBackground = FarmOnSurfaceDark,
    onSurface = FarmOnSurfaceDark,
    onSurfaceVariant = FarmOnSurfaceVariantDark,
    outline = FarmOutlineLight,
    error = FarmError,
    onError = FarmOnError
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = ForestGreenPrimaryContainerLight(),
    onPrimaryContainer = ForestGreenOnPrimaryContainer,
    secondary = SproutSecondary,
    onSecondary = SproutOnSecondary,
    secondaryContainer = SproutSecondaryContainer,
    onSecondaryContainer = SproutOnSecondaryContainer,
    tertiary = HarvestAmberTertiary,
    onTertiary = HarvestAmberOnTertiary,
    tertiaryContainer = HarvestAmberTertiaryContainer,
    onTertiaryContainer = HarvestAmberOnTertiaryContainer,
    background = FarmBackgroundLight,
    surface = FarmSurfaceLight,
    surfaceVariant = FarmSurfaceVariantLight,
    onBackground = FarmOnSurfaceLight,
    onSurface = FarmOnSurfaceLight,
    onSurfaceVariant = FarmOnSurfaceVariantLight,
    outline = FarmOutlineLight,
    error = FarmError,
    onError = FarmOnError
)

private fun ForestGreenPrimaryContainerLight() = ForestGreenPrimaryContainer

@Composable
fun PoultryFarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep farm branding by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
