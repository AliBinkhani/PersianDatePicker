/*
 * Public, calendar-system-agnostic date types for this library's API surface — used in place of
 * raw UTC-millisecond timestamps (uncomfortable to read/construct by hand) and in place of
 * `java.time.LocalDate`/`java.util.Calendar` (whose *fields* are always Gregorian, so they cannot
 * represent a Persian/Jalali date's year/month/day directly: `LocalDate.of(1405, 5, 26)` is a real
 * but absurd Gregorian date, not "26 Mordad 1405").
 *
 * A raw epoch-millisecond instant (`Long`, `java.util.Date`, or a `LocalDate` used only to name a
 * specific day rather than to carry Persian-intended field values) has no such problem: it names a
 * point in time, not a set of calendar fields, so converting it to/from [CalendarDate] via this
 * picker's [CalendarModel] is always safe and correct — see [CalendarDate.fromEpochMillis] and
 * friends in DatePickerInterop.kt.
 */

package com.hooshkar.jalalidatepicker

/**
 * A plain date in a specific calendar system: [year]/[month]/[dayOfMonth] as [calendarType]
 * defines them — e.g. Persian year 1405, month 5 (Mordad), day 26 when [calendarType] is
 * [CalendarType.PERSIAN]. This is the primary way to read and write dates on a [DatePickerState];
 * see [DatePickerState.selectedDate] and [DatePickerState.displayedMonth].
 *
 * [calendarType] makes a [CalendarDate] self-describing, so a [DatePickerState] can safely accept
 * one in a different calendar system than its own: assigning to [DatePickerState.selectedDate]
 * auto-converts (via [toPersian]/[toGregorian]) to the state's [DatePickerState.calendarType].
 * There is never a need to convert a [CalendarDate] by hand before handing it to a picker.
 *
 * To convert to/from a UTC-millisecond instant, `java.util.Date`, or `java.time.LocalDate` — e.g.
 * because a database or REST API only deals in timestamps — see [fromEpochMillis]/[toEpochMillis]
 * and friends in DatePickerInterop.kt. Never construct this class by copying fields out of a
 * `LocalDate`/`Calendar`, since those are always Gregorian.
 *
 * @param year the date's year
 * @param month the date's month (1-based, e.g. 1..12)
 * @param dayOfMonth the date's day of month (1-based)
 * @param calendarType the calendar system [year]/[month]/[dayOfMonth] are expressed in
 */
data class CalendarDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val calendarType: CalendarType,
) {
    companion object
}

/**
 * A plain year/month pair expressed in whatever calendar system a [DatePickerState] is using —
 * the type of [DatePickerState.displayedMonth].
 *
 * @param year the month's year
 * @param month the month itself (1-based, e.g. 1..12)
 */
data class CalendarYearMonth(val year: Int, val month: Int)

/**
 * One [CalendarModel] per [CalendarType], used only for [CalendarDate]'s field-math conversions
 * ([fromEpochMillis], [toEpochMillis], [inCalendarType]) — never for display.
 *
 * A neutral [java.util.Locale.ROOT] is enough here: converting between a calendar system's fields
 * and a UTC instant depends only on the `ca` (calendar) extension [createCalendarModel] applies,
 * never on language/region — those only drive locale-sensitive *display* concerns (weekday names,
 * first day of week), which is why [CalendarDate]'s conversions need no [CalendarLocale] parameter.
 */
private val mathModels: Map<CalendarType, CalendarModel> =
    CalendarType.entries.associateWith { createCalendarModel(java.util.Locale.ROOT, it) }

/**
 * Converts [utcTimeMillis] — a UTC-epoch-millisecond instant, the same representation used by
 * `System.currentTimeMillis()`, `java.util.Date.getTime()`, or a typical database/REST API
 * timestamp — into the [CalendarType.GREGORIAN] [CalendarDate] naming that UTC day.
 *
 * An instant carries no calendar-system information of its own, so decoding it always yields
 * Gregorian fields — the same convention `java.time`/`java.util.Calendar` use. Call [toPersian] on
 * the result if you need Persian fields, or just assign it directly to a Persian
 * [DatePickerState.selectedDate], which auto-converts.
 */
fun CalendarDate.Companion.fromEpochMillis(utcTimeMillis: Long): CalendarDate {
    val resolved = mathModels.getValue(CalendarType.GREGORIAN).getCanonicalDate(utcTimeMillis)
    return CalendarDate(resolved.year, resolved.month, resolved.dayOfMonth, CalendarType.GREGORIAN)
}

/**
 * Converts this [CalendarDate] to a UTC-epoch-millisecond instant representing the start of that
 * day, interpreting [year]/[month]/[dayOfMonth] according to this date's own [calendarType] —
 * always safe to call regardless of which calendar system this date is in.
 */
fun CalendarDate.toEpochMillis(): Long {
    val monthStartMillis = mathModels.getValue(calendarType).getMonth(year, month).startUtcTimeMillis
    return monthStartMillis + (dayOfMonth - 1) * MillisecondsIn24Hours
}

/** Converts this [CalendarDate] to the equivalent date in [targetType]'s calendar system. */
internal fun CalendarDate.inCalendarType(targetType: CalendarType): CalendarDate {
    if (calendarType == targetType) return this
    val resolved = mathModels.getValue(targetType).getCanonicalDate(toEpochMillis())
    return CalendarDate(resolved.year, resolved.month, resolved.dayOfMonth, targetType)
}

/** Converts this [CalendarDate] to the equivalent Persian (Jalali) date. A no-op if already Persian. */
fun CalendarDate.toPersian(): CalendarDate = inCalendarType(CalendarType.PERSIAN)

/** Converts this [CalendarDate] to the equivalent Gregorian date. A no-op if already Gregorian. */
fun CalendarDate.toGregorian(): CalendarDate = inCalendarType(CalendarType.GREGORIAN)
