package com.nullsec.messenger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NullDark = darkColorScheme(
    primary = Color(0xFF68F0B2),
    secondary = Color(0xFF2CE7A0),
    tertiary = Color(0xFF92A7A1),
    background = Color(0xFF050607),
    surface = Color(0xFF0E1416),
    onPrimary = Color(0xFF00140A),
    onSecondary = Color(0xFF00140A),
    onTertiary = Color(0xFF0B1011),
    onBackground = Color(0xFFF2F6F4),
    onSurface = Color(0xFFF2F6F4)
)

@Composable
fun NullMessengerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NullDark,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
