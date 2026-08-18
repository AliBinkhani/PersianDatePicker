/*
 * Vendored and trimmed from androidx.compose.material3:material3-android:1.4.0
 * (internal/Strings.kt, internal/Strings.android.kt), because the originals are `internal` to the
 * material3 module. Backed by this module's own string resources (res/values/strings.xml).
 *
 * Copyright 2021 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 */

package com.hooshkar.persiandatepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.ConfigurationCompat
import java.util.Locale
import kotlin.jvm.JvmInline

@Immutable
@JvmInline
internal value class Strings constructor(val value: Int) {
    companion object {
        val DatePickerTitle
            get() = Strings(R.string.jdp_date_picker_title)

        val DatePickerHeadline
            get() = Strings(R.string.jdp_date_picker_headline)

        val DatePickerHeadlineDescription
            get() = Strings(R.string.jdp_date_picker_headline_description)

        val DatePickerNoSelectionDescription
            get() = Strings(R.string.jdp_date_picker_no_selection_description)

        val DatePickerTodayDescription
            get() = Strings(R.string.jdp_date_picker_today_description)

        val DatePickerYearPickerPaneTitle
            get() = Strings(R.string.jdp_date_picker_year_picker_pane_title)

        val DatePickerSwitchToPreviousMonth
            get() = Strings(R.string.jdp_date_picker_switch_to_previous_month)

        val DatePickerSwitchToNextMonth
            get() = Strings(R.string.jdp_date_picker_switch_to_next_month)

        val DatePickerSwitchToYearSelection
            get() = Strings(R.string.jdp_date_picker_switch_to_year_selection)

        val DatePickerSwitchToDaySelection
            get() = Strings(R.string.jdp_date_picker_switch_to_day_selection)

        val DatePickerNavigateToYearDescription
            get() = Strings(R.string.jdp_date_picker_navigate_to_year_description)

        val DatePickerSwitchToMonthSelection
            get() = Strings(R.string.jdp_date_picker_switch_to_month_selection)

        val DatePickerSwitchToPreviousYear
            get() = Strings(R.string.jdp_date_picker_switch_to_previous_year)

        val DatePickerSwitchToNextYear
            get() = Strings(R.string.jdp_date_picker_switch_to_next_year)

        val DatePickerNavigateToMonthDescription
            get() = Strings(R.string.jdp_date_picker_navigate_to_month_description)

        val DatePickerMonthPickerPaneTitle
            get() = Strings(R.string.jdp_date_picker_month_picker_pane_title)
    }
}

@Composable
@ReadOnlyComposable
internal fun getString(string: Strings): String {
    LocalConfiguration.current
    val resources = LocalContext.current.resources
    return resources.getString(string.value)
}

@Composable
@ReadOnlyComposable
internal fun getString(string: Strings, vararg formatArgs: Any): String {
    val raw = getString(string)
    val locale =
        ConfigurationCompat.getLocales(LocalConfiguration.current).get(0) ?: Locale.getDefault()
    return String.format(locale, raw, *formatArgs)
}
