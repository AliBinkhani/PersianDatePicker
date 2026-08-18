/*
 * Vendored and simplified from androidx.compose.material3:material3-android:1.4.0
 * (DatePickerDialog.kt / DatePickerDialog.android.kt), because the real DatePickerDialog wraps
 * its content in `Box(Modifier.weight(1f, fill = false))` inside a `Column(SpaceBetween)`, capped
 * with `Surface.heightIn(max = 568.dp)` — a measurement combination that exists upstream so the
 * dialog can shrink when switching into Input display mode. This library's DatePicker has no
 * Input mode, so that dynamic-height machinery is both unnecessary and, in practice, the source
 * of a real layout bug when wrapping this library's DatePicker: the weekday letter row would
 * overlap the first row of day numbers instead of stacking above it (reproduced with the real
 * androidx.compose.material3.DatePickerDialog + DatePicker too, so it isn't specific to this
 * library's port). Using a plain Dialog with a naturally wrap-content-height Surface (a fixed max
 * height only as a safety cap, not as the primary sizing mechanism) avoids that measurement path
 * entirely.
 *
 * Copyright 2023 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 */

package com.hooshkar.persiandatepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A dialog for displaying a [DatePicker]. Date pickers let people select a date.
 *
 * @param onDismissRequest called when the user tries to dismiss the Dialog by clicking outside or
 *   pressing the back button. This is not called when the dismiss button is clicked.
 * @param confirmButton button which is meant to confirm a proposed action, thus resolving what
 *   triggered the dialog. The dialog does not set up any events for this button, nor does it
 *   control its enablement, so those need to be set up by the caller.
 * @param modifier the [Modifier] to be applied to this dialog's content.
 * @param dismissButton button which is meant to dismiss the dialog. The dialog does not set up any
 *   events for this button so they need to be set up by the caller.
 * @param shape defines the dialog's surface shape as well its shadow
 * @param tonalElevation when [DatePickerColors.containerColor] is a surface color, a higher
 *   elevation will result in a darker color in light theme and lighter color in dark theme
 * @param colors [DatePickerColors] that will be used to resolve the colors used for this date
 *   picker in different states. See [DatePickerDefaults.colors].
 * @param properties typically platform specific properties to further configure the dialog
 * @param content the content of the dialog (i.e. a [DatePicker], for example)
 */
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    shape: Shape = DatePickerDefaults.shape,
    tonalElevation: Dp = DatePickerDefaults.TonalElevation,
    colors: DatePickerColors = DatePickerDefaults.colors(),
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Surface(
            modifier = modifier.requiredWidth(DatePickerTokens.ContainerWidth).wrapContentHeight(),
            shape = shape,
            color = colors.containerColor,
            tonalElevation = tonalElevation,
        ) {
            Column contentColumn@{
                Box { this@contentColumn.content() }
                Row(
                    modifier = Modifier.align(Alignment.End).padding(DialogButtonsPadding),
                    horizontalArrangement = Arrangement.spacedBy(DialogButtonsSpacing, Alignment.End),
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

private val DialogButtonsPadding = PaddingValues(bottom = 8.dp, end = 6.dp, top = 8.dp)
private val DialogButtonsSpacing = 8.dp
