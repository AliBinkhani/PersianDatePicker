/*
 * Vendored and trimmed from androidx.compose.material3:material3-android:1.4.0
 * (jvmMain/androidx/compose/material3/DatePicker.jvm.kt): convenience overloads that let callers
 * work with java.time.LocalDate / YearMonth instead of raw UTC millisecond timestamps. Safe to
 * use unconditionally since this library's minSdk (26) already guarantees java.time support.
 *
 * Copyright 2024 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 */

package com.hooshkar.jalalidatepicker

import androidx.compose.runtime.Composable
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

/**
 * Creates a [DatePickerState] for a [DatePicker] that is remembered across compositions, using
 * [LocalDate] and [YearMonth] instead of raw UTC milliseconds.
 */
@Composable
fun rememberDatePickerState(
    initialSelectedDate: LocalDate?,
    initialDisplayedMonth: YearMonth? = initialSelectedDate?.let { YearMonth.from(it) },
    yearRange: IntRange = DatePickerDefaults.YearRange,
    initialDisplayMode: DisplayMode = DisplayMode.Picker,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
): DatePickerState =
    rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDate?.let { getLocalDateMillisUtc(it) },
        initialDisplayedMonthMillis = initialDisplayedMonth?.let { getYearMonthMillisUtc(it) },
        yearRange = yearRange,
        initialDisplayMode = initialDisplayMode,
        selectableDates = selectableDates,
    )

/** Sets the [DatePickerState.selectedDateMillis] based on a given [LocalDate]. */
fun DatePickerState.setSelectedDate(date: LocalDate?) {
    this.selectedDateMillis = getLocalDateMillisUtc(date)
}

/** Returns a [LocalDate] representation of the selected date, or `null` if there is none. */
fun DatePickerState.getSelectedDate(): LocalDate? = getLocalDate(this.selectedDateMillis)

/** Sets the [DatePickerState.displayedMonthMillis] based on a given [YearMonth]. */
fun DatePickerState.setDisplayedMonth(yearMonth: YearMonth) {
    this.displayedMonthMillis = getYearMonthMillisUtc(yearMonth)
}

/** Returns a [YearMonth] representation of the displayed month in this [DatePickerState]. */
fun DatePickerState.getDisplayedMonth(): YearMonth =
    getYearMonth(millisUtcFirstOfMonth = this.displayedMonthMillis)

private fun getYearMonth(millisUtcFirstOfMonth: Long): YearMonth {
    val zonedDateTimeUtc = Instant.ofEpochMilli(millisUtcFirstOfMonth).atZone(ZoneOffset.UTC)
    return YearMonth.from(zonedDateTimeUtc)
}

private fun getYearMonthMillisUtc(yearMonth: YearMonth): Long {
    val firstDayOfMonth = yearMonth.atDay(1)
    val localDateTimeAtStart = firstDayOfMonth.atStartOfDay()
    return localDateTimeAtStart.toInstant(ZoneOffset.UTC).toEpochMilli()
}

private fun getLocalDate(millisUtc: Long?): LocalDate? {
    if (millisUtc == null) return null
    return Instant.ofEpochMilli(millisUtc).atZone(ZoneOffset.UTC).toLocalDate()
}

private fun getLocalDateMillisUtc(date: LocalDate?): Long? {
    return if (date == null) {
        null
    } else {
        val localDateTimeAtStart = date.atStartOfDay()
        localDateTimeAtStart.toInstant(ZoneOffset.UTC).toEpochMilli()
    }
}
