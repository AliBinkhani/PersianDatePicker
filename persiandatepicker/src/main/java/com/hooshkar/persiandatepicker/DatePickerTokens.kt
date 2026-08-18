/*
 * A simplified stand-in for material3's internal design-token indirection
 * (tokens/DatePickerModalTokens.kt -> ColorSchemeKeyTokens/TypographyKeyTokens/ShapeKeyTokens ->
 * fromToken(...)), which is `internal` to the material3 module and therefore not reusable here.
 *
 * Instead of vendoring the whole token chain, the values below read directly from the public
 * MaterialTheme.colorScheme / .typography / .shapes, resolved to the same tokens the original
 * DatePickerModalTokens object pointed to (see the source at material3-android:1.4.0).
 */

package com.hooshkar.persiandatepicker

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object DatePickerTokens {
    val ContainerWidth: Dp = 360.0.dp
    val HeaderContainerHeight: Dp = 120.0.dp
    val DateContainerWidth: Dp = 40.0.dp
    val DateContainerHeight: Dp = 40.0.dp
    val DateTodayContainerOutlineWidth: Dp = 1.0.dp
    val SelectionYearContainerWidth: Dp = 72.0.dp
    val SelectionYearContainerHeight: Dp = 36.0.dp

    val ContainerShape: Shape
        @Composable get() = MaterialTheme.shapes.extraLarge

    val DateContainerShape: Shape
        get() = CircleShape

    val SelectionYearStateLayerShape: Shape
        get() = CircleShape

    val HeaderHeadlineFont: TextStyle
        @Composable get() = MaterialTheme.typography.headlineLarge

    val HeaderSupportingTextFont: TextStyle
        @Composable get() = MaterialTheme.typography.labelLarge

    val DateLabelTextFont: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge

    val WeekdaysLabelTextFont: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge

    val SelectionYearLabelTextFont: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge
}

/** The alpha applied to disabled content, matching Material3's disabled-content convention. */
internal const val DisabledAlpha: Float = 0.38f
