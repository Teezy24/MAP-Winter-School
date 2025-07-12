package com.example.studybuddy.ui.theme

import android.app.Activity
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
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun StudyBuddyTheme(
    isDarkMode: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            background = DarkBackground,
            primary = DarkPrimary,
            onPrimary = DarkOnPrimary,
            surface = DarkBackground,
            onSurface = DarkOnPrimary
        )
    } else {
        lightColorScheme(
            background = LightBackground,
            primary = LightPrimary,
            onPrimary = LightOnPrimary,
            surface = LightBackground,
            onSurface = LightOnPrimary
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}