/*
 * Interop helpers for bridging [CalendarDate] with the timestamp-based types most other code
 * deals in: a UTC-millisecond `Long`, `java.util.Date`, and `java.time.LocalDate`.
 *
 * These all route through [CalendarDate.fromEpochMillis]/[CalendarDate.toEpochMillis], which are
 * calendar-agnostic (an instant carries no calendar-system information), so no [DatePickerState]
 * or [CalendarLocale] is needed here: a [CalendarDate] already knows which calendar system its own
 * fields are in. `fromXxx` conversions always produce Gregorian fields (see
 * [CalendarDate.fromEpochMillis]) — call [CalendarDate.toPersian] on the result, or just assign it
 * directly to a Persian [DatePickerState.selectedDate], which auto-converts.
 */

package com.hooshkar.jalalidatepicker

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date

/** Converts [date] (a `java.util.Date`, i.e. a UTC instant) into the Gregorian [CalendarDate] naming that day. */
fun CalendarDate.Companion.fromJavaDate(date: Date): CalendarDate = fromEpochMillis(date.time)

/** Converts this [CalendarDate] to a `java.util.Date`. */
fun CalendarDate.toJavaDate(): Date = Date(toEpochMillis())

/**
 * Converts [localDate] into the Gregorian [CalendarDate] naming that day. [localDate] is treated
 * as naming one specific day (its fields are read only to resolve a UTC instant at that day's
 * start), which matches `LocalDate`'s own definition — its fields are always Gregorian. Safe for
 * any `LocalDate` that already correctly names the day you mean (e.g. `LocalDate.now()`).
 */
fun CalendarDate.Companion.fromLocalDate(localDate: LocalDate): CalendarDate =
    fromEpochMillis(localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())

/**
 * Converts this [CalendarDate] to a `java.time.LocalDate`. Note that the returned `LocalDate`'s
 * own fields are always Gregorian — e.g. for a Persian date this is *not* "year 1405, month 5, day
 * 26" but the equivalent Gregorian calendar day.
 */
fun CalendarDate.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(toEpochMillis()).atZone(ZoneOffset.UTC).toLocalDate()
