package com.f1tracker.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * CompositionLocal for the app's accent color.
 * Uses F1 Red (#E10600) as the primary accent.
 */
val LocalAccentColor = compositionLocalOf { Color(0xFFE10600) }

/**
 * CompositionLocal for minimal UI mode.
 * When true, components use flatter/cleaner styling.
 */
val LocalMinimalMode = compositionLocalOf { true }
