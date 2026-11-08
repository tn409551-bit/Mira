package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisBackground,
    primaryContainer = JarvisSurfaceVariant,
    onPrimaryContainer = JarvisCyanLight,
    secondary = JarvisPurple,
    onSecondary = JarvisBackground,
    secondaryContainer = JarvisSurfaceElevated,
    onSecondaryContainer = JarvisPurpleNeon,
    tertiary = JarvisBlue,
    onTertiary = JarvisBackground,
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorder,
    outlineVariant = JarvisBorderLight,
    error = JarvisRed,
    onError = JarvisBackground
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = JarvisTypography,
        content = content
    )
}
