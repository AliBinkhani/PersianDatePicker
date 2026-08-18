/*
 * Vendored from androidx.compose.material3:material3-android:1.4.0
 * (internal/ProvideContentColorTextStyle.kt), because the original is `internal` to the material3
 * module.
 *
 * Copyright 2023 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 */

package com.hooshkar.persiandatepicker

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * A convenience method to provide values to both LocalContentColor and LocalTextStyle in one
 * call. This is less expensive than nesting calls to CompositionLocalProvider.
 *
 * Text styles will be merged with the current value of LocalTextStyle.
 */
@Composable
internal fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit,
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content,
    )
}
