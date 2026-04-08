package com.fichestu.frontend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.fichestu.frontend.R

// ── Google Fonts provider (safe: cae gracefully offline → usa system font) ─
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage    = "com.google.android.gms",
    certificates       = R.array.com_google_android_gms_fonts_certs
)

/**
 * Luckiest Guy – fuente display para el branding del casino.
 * Falla silenciosamente offline: Material3 usa el generic sans-serif como fallback.
 */
val LuckiestGuyFamily: FontFamily = FontFamily(
    Font(
        googleFont    = GoogleFont("Luckiest Guy"),
        fontProvider  = provider,
        weight        = FontWeight.Normal
    )
)

val CasinoTypography = Typography(
    // ── Título principal de la app ────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily   = LuckiestGuyFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 48.sp,
        lineHeight   = 56.sp,
        letterSpacing = 3.sp
    ),
    displayMedium = TextStyle(
        fontFamily   = LuckiestGuyFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 36.sp,
        lineHeight   = 44.sp,
        letterSpacing = 2.sp
    ),
    displaySmall = TextStyle(
        fontFamily   = LuckiestGuyFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 28.sp,
        lineHeight   = 36.sp,
        letterSpacing = 1.5.sp
    ),
    // ── Headlines ──────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Bold,
        fontSize     = 24.sp,
        lineHeight   = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 20.sp,
        lineHeight   = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 16.sp,
        lineHeight   = 24.sp
    ),
    // ── Body ───────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.1.sp
    ),
    // ── Labels ─────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Bold,
        fontSize     = 16.sp,
        lineHeight   = 20.sp,
        letterSpacing = 1.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Medium,
        fontSize     = 13.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Medium,
        fontSize     = 11.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp
    )
)
