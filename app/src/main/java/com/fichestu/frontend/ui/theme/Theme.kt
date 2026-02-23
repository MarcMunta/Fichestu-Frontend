package com.fichestu.frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CasinoDarkColorScheme = darkColorScheme(
    primary             = Gold,
    onPrimary           = NightBlue,
    primaryContainer    = GoldDark,
    onPrimaryContainer  = PureWhite,

    secondary           = ChipRed,
    onSecondary         = PureWhite,
    secondaryContainer  = ChipRed,
    onSecondaryContainer = PureWhite,

    error               = ChipRed,
    onError             = PureWhite,
    errorContainer      = ChipRed,
    onErrorContainer    = PureWhite,

    background          = NightBlue,
    onBackground        = PureWhite,

    surface             = PanelBlue,
    onSurface           = PureWhite,
    surfaceVariant      = InputBg,
    onSurfaceVariant    = TextSecondary,
    surfaceContainer    = InputBg,
    surfaceContainerHigh = PanelBlue,

    outline             = InputBorder,
    outlineVariant      = CardBorder,

    inverseSurface      = PureWhite,
    inverseOnSurface    = NightBlue
)

/**
 * Tema principal de Fichestu – estética casino premium.
 * Siempre dark (no light variant) para mantener la atmósfera de casino.
 */
@Composable
fun FichestuTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CasinoDarkColorScheme,
        typography  = CasinoTypography,
        shapes      = CasinoShapes,
        content     = content
    )
}
