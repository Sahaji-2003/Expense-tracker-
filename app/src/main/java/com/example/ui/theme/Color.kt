package com.example.ui.theme

import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance

// Sleek Interface Theme Palette
val SleekBg: Color @Composable get() = MaterialTheme.colorScheme.background
val SleekSurface: Color @Composable get() = MaterialTheme.colorScheme.surface
val SleekSurfaceVariant: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val SleekTextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onBackground
val SleekTextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val SleekMutedText: Color @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF64748B) else Color(0xFF64748B)
val SleekDivider: Color @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0x0CFFFFFF) else Color(0x11000000)

// Material 3 Dark theme mapping for Sleek Interface
val PrimaryDark: Color @Composable get() = MaterialTheme.colorScheme.primary
val IndigoDarkAccent: Color @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF6366F1) else Color(0xFF4F46E5)
val SecondaryDark = Color(0xFF94A3B8)
val TertiaryDark = Color(0xFF10B981) // Emerald budget anchor
val BackgroundDark = Color(0xFF0A0A0A)
val SurfaceDark = Color(0xFF1E1E1E)
val OnBackgroundDark = Color(0xFFFFFFFF)
val OnSurfaceDark = Color(0xFFFFFFFF)
val OnPrimaryDark = Color(0xFF0A0A0A)
val OnSecondaryDark = Color(0xFF0A0A0A)

