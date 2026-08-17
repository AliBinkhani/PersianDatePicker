/*
 * Vendored and trimmed from androidx.compose.material3:material3-android:1.4.0
 * (commonMain/androidx/compose/material3/internal/CalendarModel.kt and the Android actuals),
 * because the original types are `internal` to the material3 module and cannot be reused directly.
 *
 * Copyright 2022 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 */

package com.hooshkar.jalalidatepicker

import android.icu.text.DateFormat
import android.icu.text.DisplayContext
import android.icu.util.TimeZone
import androidx.compose.runtime.Immutable
import androidx.compose.ui.util.fastMap
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Date

internal const val DaysInWeek: Int = 7

internal const val MillisecondsIn24Hours: Long = 86400000L

/**
 * Represents a calendar date.
 *
 * @param year the date's year
 * @param month the date's month
 * @param dayOfMonth the date's day of month
 * @param utcTimeMillis the date representation in _UTC_ milliseconds from the epoch
 */
@Immutable
internal data class CalendarDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val utcTimeMillis: Long,
) : Comparable<CalendarDate> {
    override operator fun compareTo(other: CalendarDate): Int =
        this.utcTimeMillis.compareTo(other.utcTimeMillis)
}

/**
 * Represents a calendar month.
 *
 * @param year the month's year
 * @param month the calendar month as an integer (e.g. JANUARY as 1, December as 12)
 * @param numberOfDays the number of days in the month
 * @param daysFromStartOfWeekToFirstOfMonth the number of days from the start of the week to the
 *   first day of the month
 * @param startUtcTimeMillis the first day of the month in _UTC_ milliseconds from the epoch
 */
@Immutable
internal data class CalendarMonth(
    val year: Int,
    val month: Int,
    val numberOfDays: Int,
    val daysFromStartOfWeekToFirstOfMonth: Int,
    val startUtcTimeMillis: Long,
) {
    val endUtcTimeMillis: Long = startUtcTimeMillis + (numberOfDays * MillisecondsIn24Hours) - 1

    /** Returns the position of a [CalendarMonth] within given years range. */
    fun indexIn(years: IntRange): Int {
        return (year - years.first) * 12 + month - 1
    }
}

/**
 * A calendar model that abstracts the date computations the date picker needs.
 *
 * @param locale a [CalendarLocale] to be used by this model
 */
internal abstract class CalendarModel(val locale: CalendarLocale) {

    // A map for caching formatter related results for better performance.
    internal val formatterCache = mutableMapOf<String, Any>()

    /** A [CalendarDate] representing the current day. */
    abstract val today: CalendarDate

    /**
     * Hold the first day of the week at the current `Locale` as an integer. The integer value
     * follows the ISO-8601 standard and refer to Monday as 1, and Sunday as 7.
     */
    abstract val firstDayOfWeek: Int

    /**
     * Holds a list of weekday names, starting from Monday as the first day in the list.
     *
     * Each item in this list is a [Pair] that holds the full name of the day, and its short
     * abbreviation letter(s).
     */
    abstract val weekdayNames: List<Pair<String, String>>

    /**
     * Returns a [CalendarDate] from a given _UTC_ time in milliseconds.
     *
     * The returned date will hold milliseconds value that represent the start of the day, which
     * may be different than the one provided to this function.
     *
     * @param timeInMillis UTC milliseconds from the epoch
     */
    abstract fun getCanonicalDate(timeInMillis: Long): CalendarDate

    /**
     * Returns a [CalendarMonth] from a given _UTC_ time in milliseconds.
     *
     * @param timeInMillis UTC milliseconds from the epoch for the first day the month
     */
    abstract fun getMonth(timeInMillis: Long): CalendarMonth

    /**
     * Returns a [CalendarMonth] from a given [CalendarDate].
     *
     * Note: This function ignores the [CalendarDate.dayOfMonth] value and just uses the date's
     * year and month to resolve a [CalendarMonth].
     *
     * @param date a [CalendarDate] to resolve into a month
     */
    abstract fun getMonth(date: CalendarDate): CalendarMonth

    /**
     * Returns a [CalendarMonth] from a given [year] and [month].
     *
     * @param year the month's year
     * @param month an integer representing a month (e.g. JANUARY as 1, December as 12)
     */
    abstract fun getMonth(year: Int, month: Int): CalendarMonth

    /**
     * Returns a [CalendarMonth] that is computed by adding a number of months, given as
     * [addedMonthsCount], to a given month.
     *
     * @param from the [CalendarMonth] to add to
     * @param addedMonthsCount the number of months to add
     */
    abstract fun plusMonths(from: CalendarMonth, addedMonthsCount: Int): CalendarMonth

    /**
     * Formats a [CalendarMonth] into a string with a given date format skeleton.
     *
     * @param month a [CalendarMonth] to format
     * @param skeleton a date format skeleton
     * @param locale the [CalendarLocale] to use when formatting the given month
     */
    fun formatWithSkeleton(
        month: CalendarMonth,
        skeleton: String,
        locale: CalendarLocale = this.locale,
    ): String = formatWithSkeleton(month.startUtcTimeMillis, skeleton, locale, formatterCache)

    /**
     * Formats a [CalendarDate] into a string with a given date format skeleton.
     *
     * @param date a [CalendarDate] to format
     * @param skeleton a date format skeleton
     * @param locale the [CalendarLocale] to use when formatting the given date
     */
    fun formatWithSkeleton(
        date: CalendarDate,
        skeleton: String,
        locale: CalendarLocale = this.locale,
    ): String = formatWithSkeleton(date.utcTimeMillis, skeleton, locale, formatterCache)
}

/** Selects which calendar system a [CalendarModel] operates in. */
enum class CalendarType {
    /** The standard Gregorian calendar. */
    GREGORIAN,

    /** The Persian (Jalali / Solar Hijri) calendar. */
    PERSIAN,
}

/**
 * Returns a [CalendarModel] to be used by the date picker.
 *
 * The returned model's [CalendarModel.locale] always carries an explicit `ca` (calendar) Unicode
 * locale extension matching [calendarType]. This is not just for [CalendarType.PERSIAN]: some
 * regions (e.g. `IR`) have a non-Gregorian calendar as their CLDR default, so
 * [CalendarType.GREGORIAN] needs the explicit `gregory` override too, or ICU-backed formatting
 * (see [formatWithSkeleton]) would silently render Persian month names/digits under a "Gregorian"
 * picker.
 */
internal fun createCalendarModel(locale: CalendarLocale, calendarType: CalendarType): CalendarModel {
    val calendarKeyword = if (calendarType == CalendarType.PERSIAN) "persian" else "gregory"
    val effectiveLocale: CalendarLocale =
        java.util.Locale.Builder().setLocale(locale).setUnicodeLocaleKeyword("ca", calendarKeyword).build()
    return when (calendarType) {
        CalendarType.GREGORIAN -> CalendarModelImpl(effectiveLocale)
        CalendarType.PERSIAN -> PersianCalendarModelImpl(effectiveLocale)
    }
}

/**
 * Formats a UTC timestamp into a string with a given date format skeleton.
 *
 * A skeleton is similar to, and uses the same format characters as described in
 * [Unicode Technical Standard #35](https://unicode.org/reports/tr35/tr35-dates.html#Date_Field_Symbol_Table)
 *
 * One difference is that order is irrelevant. For example, "MMMMd" will return "MMMM d" in the
 * en_US locale, but "d. MMMM" in the de_CH locale.
 *
 * @param utcTimeMillis a UTC timestamp to format (milliseconds from epoch)
 * @param skeleton a date format skeleton
 * @param locale the [CalendarLocale] to use when formatting the given timestamp
 * @param cache a [MutableMap] for caching formatter related results for better performance
 */
internal fun formatWithSkeleton(
    utcTimeMillis: Long,
    skeleton: String,
    locale: CalendarLocale,
    cache: MutableMap<String, Any>,
): String {
    val instanceForSkeleton =
        cache.getOrPut(key = "S:$skeleton${locale.toLanguageTag()}") {
            val instanceForSkeleton = DateFormat.getInstanceForSkeleton(skeleton, locale)
            instanceForSkeleton.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE)
            // Note: ICU uses the terms GMT and UTC interchangeably, as it does not handle leap
            // seconds or historical behavior.
            instanceForSkeleton.timeZone = TimeZone.GMT_ZONE
            instanceForSkeleton
        } as DateFormat
    return instanceForSkeleton.format(Date(utcTimeMillis))
}

/**
 * A [CalendarModel] implementation backed by `java.time` (available since API 26, which is this
 * library's `minSdk`).
 *
 * @param locale a [CalendarLocale] to be used by this model
 */
internal class CalendarModelImpl(locale: CalendarLocale) : CalendarModel(locale = locale) {

    override val today
        get(): CalendarDate {
            val systemLocalDate = LocalDate.now()
            return CalendarDate(
                year = systemLocalDate.year,
                month = systemLocalDate.monthValue,
                dayOfMonth = systemLocalDate.dayOfMonth,
                utcTimeMillis =
                    systemLocalDate
                        .atTime(LocalTime.MIDNIGHT)
                        .atZone(utcTimeZoneId)
                        .toInstant()
                        .toEpochMilli(),
            )
        }

    override val firstDayOfWeek: Int = WeekFields.of(locale).firstDayOfWeek.value

    override val weekdayNames: List<Pair<String, String>> =
        // This will start with Monday as the first day, according to ISO-8601.
        with(locale) {
            DayOfWeek.entries.fastMap {
                it.getDisplayName(TextStyle.FULL_STANDALONE, /* locale= */ this) to
                    it.getDisplayName(TextStyle.NARROW_STANDALONE, /* locale= */ this)
            }
        }

    override fun getCanonicalDate(timeInMillis: Long): CalendarDate {
        val localDate = Instant.ofEpochMilli(timeInMillis).atZone(utcTimeZoneId).toLocalDate()
        return CalendarDate(
            year = localDate.year,
            month = localDate.monthValue,
            dayOfMonth = localDate.dayOfMonth,
            utcTimeMillis = localDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000,
        )
    }

    override fun getMonth(timeInMillis: Long): CalendarMonth {
        return getMonth(
            Instant.ofEpochMilli(timeInMillis).atZone(utcTimeZoneId).withDayOfMonth(1).toLocalDate()
        )
    }

    override fun getMonth(date: CalendarDate): CalendarMonth {
        return getMonth(LocalDate.of(date.year, date.month, 1))
    }

    override fun getMonth(year: Int, month: Int): CalendarMonth {
        return getMonth(LocalDate.of(year, month, 1))
    }

    override fun plusMonths(from: CalendarMonth, addedMonthsCount: Int): CalendarMonth {
        if (addedMonthsCount <= 0) return from

        val firstDayLocalDate = from.toLocalDate()
        val laterMonth = firstDayLocalDate.plusMonths(addedMonthsCount.toLong())
        return getMonth(laterMonth)
    }

    override fun toString(): String {
        return "CalendarModel"
    }

    companion object {
        /** Holds a UTC [ZoneId]. */
        internal val utcTimeZoneId: ZoneId = ZoneId.of("UTC")

        internal fun getCachedDateTimeFormatter(
            pattern: String,
            locale: CalendarLocale,
            cache: MutableMap<String, Any>,
        ): DateTimeFormatter {
            return cache.getOrPut(key = "P:$pattern${locale.toLanguageTag()}") {
                DateTimeFormatter.ofPattern(pattern, locale)
                    .withDecimalStyle(DecimalStyle.of(locale))
            } as DateTimeFormatter
        }
    }

    private fun getMonth(firstDayLocalDate: LocalDate): CalendarMonth {
        val difference = firstDayLocalDate.dayOfWeek.value - firstDayOfWeek
        val daysFromStartOfWeekToFirstOfMonth =
            if (difference < 0) {
                difference + DaysInWeek
            } else {
                difference
            }
        val firstDayEpochMillis =
            firstDayLocalDate
                .atTime(LocalTime.MIDNIGHT)
                .atZone(utcTimeZoneId)
                .toInstant()
                .toEpochMilli()
        return CalendarMonth(
            year = firstDayLocalDate.year,
            month = firstDayLocalDate.monthValue,
            numberOfDays = firstDayLocalDate.lengthOfMonth(),
            daysFromStartOfWeekToFirstOfMonth = daysFromStartOfWeekToFirstOfMonth,
            startUtcTimeMillis = firstDayEpochMillis,
        )
    }

    private fun CalendarMonth.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(startUtcTimeMillis).atZone(utcTimeZoneId).toLocalDate()
    }
}
