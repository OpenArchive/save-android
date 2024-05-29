package net.opendasharchive.openarchive.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun Theme(
    content: @Composable () -> Unit
) {
    val isDarkTheme by rememberUpdatedState(newValue = isSystemInDarkTheme())

    val colors = getThemeColors(isDarkTheme)

    val dimensions = getThemeDimensions(isDarkTheme)

    CompositionLocalProvider(
        LocalDimensions provides dimensions,
        LocalColors provides colors,
    ) {
        MaterialTheme(
            colorScheme = colors.material,
            content = content
        )
    }
}


val ThemeColors: ColorTheme @Composable get() = LocalColors.current
val ThemeDimensions: DimensionsTheme @Composable get() = LocalDimensions.current
