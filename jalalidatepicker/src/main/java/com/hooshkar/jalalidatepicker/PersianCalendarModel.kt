/*
 * A [CalendarModel] implementation for the Persian (Jalali / Solar Hijri) calendar.
 *
 * Field math (year/month/day, month length, leap-year detection, month arithmetic) is delegated
 * to `android.icu.util.Calendar`, resolved to `android.icu.util.PersianCalendar` via a locale
 * carrying the `-u-ca-persian` Unicode extension (see [createCalendarModel]). This mirrors the
 * technique of building `SimpleDateFormat(pattern, ULocale("fa_IR@calendar=persian"))` for
 * formatting, except a plain `java.util.Locale` with the extension works equally well for both
 * `android.icu.util.Calendar` and `android.icu.text.DateFormat` — no `ULocale` needed.
 *
 * Verified on-device (API 36 emulator, ICU): `Calendar.getInstance(GMT, fa-IR-u-ca-persian)`
 * resolves to `android.icu.util.PersianCalendar` (a public, non-hidden ICU class); Esfand length
 * across 1402..1405 came back 29/30/29/29, confirming leap years are computed correctly.
 *
 * Day-of-week (needed to align the first day of a month within its week row) is instead derived
 * from `java.time`, since a given UTC instant has one fixed weekday regardless of which calendar
 * labels its year/month/day — this avoids having to hand-write a mapping between ICU's
 * Sunday-first (1..7) day-of-week numbering and this codebase's Monday-first (1..7) convention.
 */

package com.hooshkar.jalalidatepicker

import android.icu.util.Calendar as IcuCalendar
import android.icu.util.TimeZone as IcuTimeZone
import java.time.Instant
import java.time.ZoneOffset
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import androidx.compose.ui.util.fastMap

internal class PersianCalendarModelImpl(locale: CalendarLocale) : CalendarModel(locale = locale) {

    // Weekday/first-day-of-week are region properties, not calendar-system properties, but we
    // strip the `ca` extension before handing the locale to `java.time` anyway, purely so this
    // stays decoupled from any ICU-specific interpretation of that extension.
    private val weekLocale: CalendarLocale =
        java.util.Locale.Builder().setLocale(locale).clearExtensions().build()

    private fun newCalendar(): IcuCalendar = IcuCalendar.getInstance(IcuTimeZone.GMT_ZONE, locale)

    override val today: CalendarDate
        get() {
            val cal = newCalendar()
            val year = cal.get(IcuCalendar.YEAR)
            val month = cal.get(IcuCalendar.MONTH) + 1
            val dayOfMonth = cal.get(IcuCalendar.DAY_OF_MONTH)
            return CalendarDate(year, month, dayOfMonth, startOfDayMillis(year, month, dayOfMonth))
        }

    override val firstDayOfWeek: Int = WeekFields.of(weekLocale).firstDayOfWeek.value

    override val weekdayNames: List<Pair<String, String>> =
        // This will start with Monday as the first day, according to ISO-8601 (weekday *names*
        // don't depend on the calendar system, only on the locale's language).
        with(weekLocale) {
            DayOfWeek.entries.fastMap {
                it.getDisplayName(TextStyle.FULL_STANDALONE, /* locale= */ this) to
                    it.getDisplayName(TextStyle.NARROW_STANDALONE, /* locale= */ this)
            }
        }

    override fun getCanonicalDate(timeInMillis: Long): CalendarDate {
        val cal = newCalendar()
        cal.timeInMillis = timeInMillis
        val year = cal.get(IcuCalendar.YEAR)
        val month = cal.get(IcuCalendar.MONTH) + 1
        val dayOfMonth = cal.get(IcuCalendar.DAY_OF_MONTH)
        return CalendarDate(year, month, dayOfMonth, startOfDayMillis(year, month, dayOfMonth))
    }

    override fun getMonth(timeInMillis: Long): CalendarMonth {
        val cal = newCalendar()
        cal.timeInMillis = timeInMillis
        return getMonth(cal.get(IcuCalendar.YEAR), cal.get(IcuCalendar.MONTH) + 1)
    }

    override fun getMonth(date: CalendarDate): CalendarMonth = getMonth(date.year, date.month)

    override fun getMonth(year: Int, month: Int): CalendarMonth {
        val cal = firstOfMonthCalendar(year, month)
        val startMillis = cal.timeInMillis
        val numberOfDays = cal.getActualMaximum(IcuCalendar.DAY_OF_MONTH)
        val isoDayOfWeek = isoDayOfWeekOf(startMillis)
        val difference = isoDayOfWeek - firstDayOfWeek
        val daysFromStartOfWeekToFirstOfMonth = if (difference < 0) difference + DaysInWeek else difference
        return CalendarMonth(
            year = year,
            month = month,
            numberOfDays = numberOfDays,
            daysFromStartOfWeekToFirstOfMonth = daysFromStartOfWeekToFirstOfMonth,
            startUtcTimeMillis = startMillis,
        )
    }

    override fun plusMonths(from: CalendarMonth, addedMonthsCount: Int): CalendarMonth {
        if (addedMonthsCount == 0) return from
        val cal = firstOfMonthCalendar(from.year, from.month)
        cal.add(IcuCalendar.MONTH, addedMonthsCount)
        return getMonth(cal.get(IcuCalendar.YEAR), cal.get(IcuCalendar.MONTH) + 1)
    }

    override fun toString(): String = "PersianCalendarModel"

    private fun firstOfMonthCalendar(year: Int, month: Int): IcuCalendar {
        val cal = newCalendar()
        cal.clear()
        cal.set(IcuCalendar.YEAR, year)
        cal.set(IcuCalendar.MONTH, month - 1)
        cal.set(IcuCalendar.DAY_OF_MONTH, 1)
        return cal
    }

    private fun startOfDayMillis(year: Int, month: Int, dayOfMonth: Int): Long {
        val cal = newCalendar()
        cal.clear()
        cal.set(IcuCalendar.YEAR, year)
        cal.set(IcuCalendar.MONTH, month - 1)
        cal.set(IcuCalendar.DAY_OF_MONTH, dayOfMonth)
        return cal.timeInMillis
    }

    /** Returns the ISO-8601 day of week (1=Monday..7=Sunday) of the given UTC instant. */
    private fun isoDayOfWeekOf(utcMillis: Long): Int =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).dayOfWeek.value
}
