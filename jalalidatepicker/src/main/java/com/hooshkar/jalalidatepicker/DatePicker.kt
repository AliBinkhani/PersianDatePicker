/*
 * Vendored and adapted from androidx.compose.material3:material3-android:1.4.0
 * (commonMain/androidx/compose/material3/DatePicker.kt), because the real DatePicker depends on
 * many types that are `internal` to the material3 module (CalendarModel, Strings, Icons, design
 * tokens, ...) and can't be reused or subclassed from outside of it.
 *
 * Differences from the original, for this first "make it work" pass:
 * - Only the calendar-grid "Picker" display mode is implemented (no keyboard "Input" mode / mode
 *   toggle yet).
 * - No date-range selection support (that lives in material3's DateRangePicker.kt, not ported).
 * - Colors are read directly from the public MaterialTheme.colorScheme instead of vendoring
 *   material3's internal design-token indirection (DatePickerModalTokens -> ColorSchemeKeyTokens
 *   -> fromToken). See [DatePickerTokens].
 * - Animations use plain `tween`/`spring` specs instead of material3's internal MotionScheme
 *   token system, which has no public equivalent in this version.
 *
 * Copyright 2022 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 */

package com.hooshkar.jalalidatepicker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.horizontalScrollAxisRange
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlinx.coroutines.launch

/**
 * A Jalali-ready copy of Material Design's date picker.
 *
 * Date pickers let people select a date and preferably should be embedded into Dialogs.
 *
 * By default, a date picker lets you pick a date via a calendar UI.
 *
 * @param state state of the date picker. See [rememberDatePickerState].
 * @param modifier the [Modifier] to be applied to this date picker
 * @param dateFormatter a [DatePickerFormatter] that provides formatting skeletons for dates
 *   display
 * @param colors [DatePickerColors] that will be used to resolve the colors used for this date
 *   picker in different states. See [DatePickerDefaults.colors].
 * @param title the title to be displayed in the date picker
 * @param headline the headline to be displayed in the date picker
 */
@Composable
fun DatePicker(
    state: DatePickerState,
    modifier: Modifier = Modifier,
    dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
    colors: DatePickerColors = DatePickerDefaults.colors(),
    title: (@Composable () -> Unit)? = {
        DatePickerDefaults.DatePickerTitle(
            modifier = Modifier.padding(DatePickerTitlePadding),
            contentColor = colors.titleContentColor,
        )
    },
    headline: (@Composable () -> Unit)? = {
        DatePickerDefaults.DatePickerHeadline(
            selectedDateMillis = state.selectedDateMillis,
            dateFormatter = dateFormatter,
            locale = state.locale,
            modifier = Modifier.padding(DatePickerHeadlinePadding),
            contentColor = colors.headlineContentColor,
        )
    },
) {
    val calendarModel =
        remember(state.locale, state.calendarType) {
            if (state is BaseDatePickerStateImpl) {
                state.calendarModel
            } else {
                createCalendarModel(state.locale, state.calendarType)
            }
        }
    DateEntryContainer(
        modifier = modifier,
        title = title,
        headline = headline,
        headlineTextStyle = DatePickerTokens.HeaderHeadlineFont,
        headerMinHeight = DatePickerTokens.HeaderContainerHeight,
        colors = colors,
    ) {
        DatePickerContent(
            selectedDateMillis = state.selectedDateMillis,
            displayedMonthMillis = state.displayedMonthMillis,
            onDateSelectionChange = { dateInMillis -> state.selectedDateMillis = dateInMillis },
            onDisplayedMonthChange = { monthInMillis ->
                state.displayedMonthMillis = monthInMillis
            },
            calendarModel = calendarModel,
            yearRange = state.yearRange,
            dateFormatter = dateFormatter,
            selectableDates = state.selectableDates,
            colors = colors,
        )
    }
}

/**
 * A state object that can be hoisted to observe the date picker state. See
 * [rememberDatePickerState].
 */
@Stable
interface DatePickerState {

    /**
     * A timestamp that represents the selected date _start_ of the day in _UTC_ milliseconds from
     * the epoch.
     *
     * @throws IllegalArgumentException in case the value is set with a timestamp that does not
     *   fall within the [yearRange].
     */
    var selectedDateMillis: Long?

    /**
     * A timestamp that represents the currently displayed month _start_ date in _UTC_
     * milliseconds from the epoch.
     *
     * @throws IllegalArgumentException in case the value is set with a timestamp that does not
     *   fall within the [yearRange].
     */
    var displayedMonthMillis: Long

    /** A [DisplayMode] that represents the current UI mode (i.e. picker or input). */
    var displayMode: DisplayMode

    /** The [CalendarType] (Gregorian or Persian/Jalali) this picker's dates are expressed in. */
    val calendarType: CalendarType

    /** An [IntRange] that holds the year range that the date picker will be limited to. */
    val yearRange: IntRange

    /**
     * A [SelectableDates] that is consulted to check if a date is allowed.
     *
     * In case a date is not allowed to be selected, it will appear disabled in the UI.
     */
    val selectableDates: SelectableDates

    /**
     * A locale that will be used when formatting dates, determining the input format, week-days,
     * and more.
     */
    val locale: CalendarLocale
}

/** An interface that controls the selectable dates and years in the date pickers UI. */
@Stable
interface SelectableDates {

    /**
     * Returns true if the date item representing the [utcTimeMillis] should be enabled for
     * selection in the UI.
     */
    fun isSelectableDate(utcTimeMillis: Long) = true

    /**
     * Returns true if a given [year] should be enabled for selection in the UI. When a year is
     * defined as non selectable, all the dates in that year will also be non selectable.
     */
    fun isSelectableYear(year: Int) = true
}

/** A date formatter interface used by [DatePicker]. */
interface DatePickerFormatter {

    /**
     * Format a given [monthMillis] to a string representation of the month and the year (i.e.
     * January 2023).
     *
     * @param monthMillis timestamp in _UTC_ milliseconds from the epoch that represents the month
     * @param locale a [CalendarLocale] to use when formatting the month and year
     */
    fun formatMonthYear(monthMillis: Long?, locale: CalendarLocale): String?

    /**
     * Format a given [dateMillis] to a string representation of the date (i.e. Mar 27, 2021).
     *
     * @param dateMillis timestamp in _UTC_ milliseconds from the epoch that represents the date
     * @param locale a [CalendarLocale] to use when formatting the date
     * @param forContentDescription indicates that the requested formatting is for content
     *   description. In these cases, the output may include a more descriptive wording that will
     *   be passed to a screen readers.
     */
    fun formatDate(dateMillis: Long?, locale: CalendarLocale, forContentDescription: Boolean = false): String?
}

/**
 * Represents the different modes that a date picker can be at.
 *
 * Note: only [Picker] is implemented so far; a keyboard [Input] mode may be added later.
 */
@Immutable
@JvmInline
value class DisplayMode internal constructor(internal val value: Int) {

    companion object {
        /** Date picker mode */
        val Picker = DisplayMode(0)
    }

    override fun toString() = "Picker"
}

/**
 * Creates a [DatePickerState] for a [DatePicker] that is remembered across compositions.
 *
 * To create a date picker state outside composition, see the `DatePickerState` function.
 *
 * @param initialSelectedDateMillis timestamp in _UTC_ milliseconds from the epoch that represents
 *   an initial selection of a date. Provide a `null` to indicate no selection.
 * @param initialDisplayedMonthMillis timestamp in _UTC_ milliseconds from the epoch that
 *   represents an initial selection of a month to be displayed to the user. By default, in case
 *   an `initialSelectedDateMillis` is provided, the initial displayed month would be the month of
 *   the selected date. Otherwise, in case `null` is provided, the displayed month would be the
 *   current one.
 * @param calendarType the [CalendarType] (Gregorian or Persian/Jalali) this picker's dates are
 *   expressed in
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited
 *   to
 * @param initialDisplayMode an initial [DisplayMode] that this state will hold
 * @param selectableDates a [SelectableDates] that is consulted to check if a date is allowed. In
 *   case a date is not allowed to be selected, it will appear disabled in the UI.
 */
@Composable
fun rememberDatePickerState(
    initialSelectedDateMillis: Long? = null,
    initialDisplayedMonthMillis: Long? = initialSelectedDateMillis,
    calendarType: CalendarType = CalendarType.PERSIAN,
    yearRange: IntRange =
        if (calendarType == CalendarType.PERSIAN) {
            DatePickerDefaults.PersianYearRange
        } else {
            DatePickerDefaults.YearRange
        },
    initialDisplayMode: DisplayMode = DisplayMode.Picker,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
): DatePickerState {
    val locale = defaultLocale()
    return rememberSaveable(saver = DatePickerStateImpl.Saver(selectableDates, locale, calendarType)) {
            DatePickerStateImpl(
                initialSelectedDateMillis = initialSelectedDateMillis,
                initialDisplayedMonthMillis = initialDisplayedMonthMillis,
                yearRange = yearRange,
                initialDisplayMode = initialDisplayMode,
                selectableDates = selectableDates,
                calendarType = calendarType,
                locale = locale,
            )
        }
        .apply {
            // Update the state's selectable dates if they were changed.
            this.selectableDates = selectableDates
        }
}

/**
 * Creates a [DatePickerState].
 *
 * For most cases, you are advised to use the [rememberDatePickerState] when in a composition.
 *
 * @param locale the [CalendarLocale] that will be used when formatting dates, determining the
 *   input format, displaying the week-day, determining the first day of the week, and more.
 * @param initialSelectedDateMillis timestamp in _UTC_ milliseconds from the epoch that represents
 *   an initial selection of a date. Provide a `null` to indicate no selection. Note that the
 *   state's [DatePickerState.selectedDateMillis] will provide a timestamp that represents the
 *   _start_ of the day, which may be different than the provided initialSelectedDateMillis.
 * @param initialDisplayedMonthMillis timestamp in _UTC_ milliseconds from the epoch that
 *   represents an initial selection of a month to be displayed to the user. In case `null` is
 *   provided, the displayed month would be the current one.
 * @param calendarType the [CalendarType] (Gregorian or Persian/Jalali) this picker's dates are
 *   expressed in
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited
 *   to
 * @param initialDisplayMode an initial [DisplayMode] that this state will hold
 * @param selectableDates a [SelectableDates] that is consulted to check if a date is allowed. In
 *   case a date is not allowed to be selected, it will appear disabled in the UI.
 * @throws [IllegalArgumentException] if the initial selected date or displayed month represent a
 *   year that is out of the year range.
 * @see rememberDatePickerState
 */
fun DatePickerState(
    locale: CalendarLocale,
    initialSelectedDateMillis: Long? = null,
    initialDisplayedMonthMillis: Long? = initialSelectedDateMillis,
    calendarType: CalendarType = CalendarType.PERSIAN,
    yearRange: IntRange =
        if (calendarType == CalendarType.PERSIAN) {
            DatePickerDefaults.PersianYearRange
        } else {
            DatePickerDefaults.YearRange
        },
    initialDisplayMode: DisplayMode = DisplayMode.Picker,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
): DatePickerState =
    DatePickerStateImpl(
        initialSelectedDateMillis = initialSelectedDateMillis,
        initialDisplayedMonthMillis = initialDisplayedMonthMillis,
        yearRange = yearRange,
        initialDisplayMode = initialDisplayMode,
        selectableDates = selectableDates,
        calendarType = calendarType,
        locale = locale,
    )

/** Contains default values used by the [DatePicker]. */
@Stable
object DatePickerDefaults {

    /**
     * Creates a [DatePickerColors] that will potentially animate between the provided colors
     * according to the Material specification.
     */
    @Composable fun colors() = defaultDatePickerColors()

    /**
     * Creates a [DatePickerColors] that will potentially animate between the provided colors
     * according to the Material specification.
     */
    @Composable
    fun colors(
        containerColor: Color = Color.Unspecified,
        titleContentColor: Color = Color.Unspecified,
        headlineContentColor: Color = Color.Unspecified,
        weekdayContentColor: Color = Color.Unspecified,
        navigationContentColor: Color = Color.Unspecified,
        yearContentColor: Color = Color.Unspecified,
        disabledYearContentColor: Color = Color.Unspecified,
        currentYearContentColor: Color = Color.Unspecified,
        selectedYearContentColor: Color = Color.Unspecified,
        disabledSelectedYearContentColor: Color = Color.Unspecified,
        selectedYearContainerColor: Color = Color.Unspecified,
        disabledSelectedYearContainerColor: Color = Color.Unspecified,
        dayContentColor: Color = Color.Unspecified,
        disabledDayContentColor: Color = Color.Unspecified,
        selectedDayContentColor: Color = Color.Unspecified,
        disabledSelectedDayContentColor: Color = Color.Unspecified,
        selectedDayContainerColor: Color = Color.Unspecified,
        disabledSelectedDayContainerColor: Color = Color.Unspecified,
        todayContentColor: Color = Color.Unspecified,
        todayDateBorderColor: Color = Color.Unspecified,
        dividerColor: Color = Color.Unspecified,
    ): DatePickerColors =
        defaultDatePickerColors().copy(
            containerColor = containerColor,
            titleContentColor = titleContentColor,
            headlineContentColor = headlineContentColor,
            weekdayContentColor = weekdayContentColor,
            navigationContentColor = navigationContentColor,
            yearContentColor = yearContentColor,
            disabledYearContentColor = disabledYearContentColor,
            currentYearContentColor = currentYearContentColor,
            selectedYearContentColor = selectedYearContentColor,
            disabledSelectedYearContentColor = disabledSelectedYearContentColor,
            selectedYearContainerColor = selectedYearContainerColor,
            disabledSelectedYearContainerColor = disabledSelectedYearContainerColor,
            dayContentColor = dayContentColor,
            disabledDayContentColor = disabledDayContentColor,
            selectedDayContentColor = selectedDayContentColor,
            disabledSelectedDayContentColor = disabledSelectedDayContentColor,
            selectedDayContainerColor = selectedDayContainerColor,
            disabledSelectedDayContainerColor = disabledSelectedDayContainerColor,
            todayContentColor = todayContentColor,
            todayDateBorderColor = todayDateBorderColor,
            dividerColor = dividerColor,
        )

    @Composable
    private fun defaultDatePickerColors(): DatePickerColors {
        val colorScheme = MaterialTheme.colorScheme
        return DatePickerColors(
            containerColor = colorScheme.surfaceContainerHigh,
            titleContentColor = colorScheme.onSurfaceVariant,
            headlineContentColor = colorScheme.onSurfaceVariant,
            weekdayContentColor = colorScheme.onSurface,
            navigationContentColor = colorScheme.onSurfaceVariant,
            yearContentColor = colorScheme.onSurfaceVariant,
            disabledYearContentColor = colorScheme.onSurfaceVariant.copy(alpha = DisabledAlpha),
            currentYearContentColor = colorScheme.primary,
            selectedYearContentColor = colorScheme.onPrimary,
            disabledSelectedYearContentColor = colorScheme.onPrimary.copy(alpha = DisabledAlpha),
            selectedYearContainerColor = colorScheme.primary,
            disabledSelectedYearContainerColor = colorScheme.primary.copy(alpha = DisabledAlpha),
            dayContentColor = colorScheme.onSurface,
            disabledDayContentColor = colorScheme.onSurface.copy(alpha = DisabledAlpha),
            selectedDayContentColor = colorScheme.onPrimary,
            disabledSelectedDayContentColor = colorScheme.onPrimary.copy(alpha = DisabledAlpha),
            selectedDayContainerColor = colorScheme.primary,
            disabledSelectedDayContainerColor = colorScheme.primary.copy(alpha = DisabledAlpha),
            todayContentColor = colorScheme.primary,
            todayDateBorderColor = colorScheme.primary,
            dividerColor = colorScheme.outlineVariant,
        )
    }

    /**
     * Returns a [DatePickerFormatter].
     *
     * The date formatter will apply the best possible localized form of the given skeleton and
     * Locale. A skeleton is similar to, and uses the same format characters as, a Unicode <a
     * href="http://www.unicode.org/reports/tr35/#Date_Format_Patterns">UTS #35</a> pattern.
     *
     * @param yearSelectionSkeleton a date format skeleton used to format the date picker's year
     *   selection menu button (e.g. "March 2021").
     * @param selectedDateSkeleton a date format skeleton used to format a selected date (e.g.
     *   "Mar 27, 2021")
     * @param selectedDateDescriptionSkeleton a date format skeleton used to format a selected
     *   date to be used as content description for screen readers (e.g. "Saturday, March 27,
     *   2021")
     */
    fun dateFormatter(
        yearSelectionSkeleton: String = YearMonthSkeleton,
        selectedDateSkeleton: String = YearAbbrMonthDaySkeleton,
        selectedDateDescriptionSkeleton: String = YearMonthWeekdayDaySkeleton,
    ): DatePickerFormatter =
        DatePickerFormatterImpl(
            yearSelectionSkeleton = yearSelectionSkeleton,
            selectedDateSkeleton = selectedDateSkeleton,
            selectedDateDescriptionSkeleton = selectedDateDescriptionSkeleton,
        )

    /**
     * A default date picker title composable.
     *
     * @param modifier a [Modifier] to be applied for the title
     * @param contentColor the content color of this title
     */
    @Composable
    fun DatePickerTitle(modifier: Modifier = Modifier, contentColor: Color = colors().titleContentColor) {
        Text(text = getString(string = Strings.DatePickerTitle), modifier = modifier, color = contentColor)
    }

    /**
     * A default date picker headline composable that displays a default headline text when there
     * is no date selection, and an actual date string when there is.
     *
     * @param selectedDateMillis a timestamp that represents the selected date _start_ of the day
     *   in _UTC_ milliseconds from the epoch
     * @param dateFormatter a [DatePickerFormatter]
     * @param modifier a [Modifier] to be applied for the headline
     * @param contentColor the content color of this headline
     * @param locale the [CalendarLocale] to format [selectedDateMillis] with. Defaults to the
     *   platform locale, but callers that know their picker's actual calendar-aware locale (e.g.
     *   [DatePicker] itself, via `state.locale`) should pass it explicitly so the headline text
     *   uses the same calendar system (Gregorian/Persian) as the calendar grid.
     */
    @Composable
    fun DatePickerHeadline(
        selectedDateMillis: Long?,
        dateFormatter: DatePickerFormatter,
        modifier: Modifier = Modifier,
        contentColor: Color = colors().headlineContentColor,
        locale: CalendarLocale = defaultLocale(),
    ) {
        val formattedDate =
            dateFormatter.formatDate(dateMillis = selectedDateMillis, locale = locale)
        val verboseDateDescription =
            dateFormatter.formatDate(
                dateMillis = selectedDateMillis,
                locale = locale,
                forContentDescription = true,
            ) ?: getString(Strings.DatePickerNoSelectionDescription)

        val headlineText = formattedDate ?: getString(Strings.DatePickerHeadline)

        val headlineDescription =
            formatHeadlineDescription(
                getString(Strings.DatePickerHeadlineDescription),
                verboseDateDescription,
            )

        Text(
            text = headlineText,
            modifier =
                modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = headlineDescription
                },
            color = contentColor,
            maxLines = 1,
        )
    }

    /**
     * Creates and remembers a [FlingBehavior] that will represent natural fling curve with snap
     * to the most visible month in the months list.
     *
     * @param lazyListState a [LazyListState]
     */
    @Composable
    internal fun rememberSnapFlingBehavior(lazyListState: LazyListState): FlingBehavior {
        val snapAnimationSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 400)
        return remember(lazyListState) {
            val original = SnapLayoutInfoProvider(lazyListState)
            val snapLayoutInfoProvider =
                object : SnapLayoutInfoProvider by original {
                    override fun calculateApproachOffset(
                        velocity: Float,
                        decayOffset: Float,
                    ): Float = 0.0f
                }

            snapFlingBehavior(
                snapLayoutInfoProvider = snapLayoutInfoProvider,
                decayAnimationSpec = exponentialDecay(),
                snapAnimationSpec = snapAnimationSpec,
            )
        }
    }

    /** The range of Gregorian years for the date picker dialogs. */
    val YearRange: IntRange = IntRange(1900, 2100)

    /** The range of Persian (Jalali) years for the date picker dialogs. */
    val PersianYearRange: IntRange = IntRange(1300, 1500)

    /** The default tonal elevation used for the date picker dialog. */
    val TonalElevation: Dp = 0.dp

    /** The default shape for date picker dialogs. */
    val shape: Shape
        @Composable get() = DatePickerTokens.ContainerShape

    /** A default [SelectableDates] that allows all dates to be selected. */
    val AllDates: SelectableDates = object : SelectableDates {}

    /**
     * A date format skeleton used to format the date picker's year selection menu button (e.g.
     * "March 2021")
     */
    const val YearMonthSkeleton: String = "yMMMM"

    /** A date format skeleton used to format a selected date (e.g. "Mar 27, 2021") */
    const val YearAbbrMonthDaySkeleton: String = "yMMMd"

    /**
     * A date format skeleton used to format a selected date to be used as content description for
     * screen readers (e.g. "Saturday, March 27, 2021")
     */
    const val YearMonthWeekdayDaySkeleton: String = "yMMMMEEEEd"
}

private fun formatHeadlineDescription(template: String, verboseDateDescription: String): String =
    template.format(verboseDateDescription)

/**
 * Represents the colors used by the date picker.
 *
 * @constructor create an instance with arbitrary colors, see [DatePickerDefaults.colors] for the
 *   default implementation that follows Material specifications.
 */
@Immutable
class DatePickerColors(
    val containerColor: Color,
    val titleContentColor: Color,
    val headlineContentColor: Color,
    val weekdayContentColor: Color,
    val navigationContentColor: Color,
    val yearContentColor: Color,
    val disabledYearContentColor: Color,
    val currentYearContentColor: Color,
    val selectedYearContentColor: Color,
    val disabledSelectedYearContentColor: Color,
    val selectedYearContainerColor: Color,
    val disabledSelectedYearContainerColor: Color,
    val dayContentColor: Color,
    val disabledDayContentColor: Color,
    val selectedDayContentColor: Color,
    val disabledSelectedDayContentColor: Color,
    val selectedDayContainerColor: Color,
    val disabledSelectedDayContainerColor: Color,
    val todayContentColor: Color,
    val todayDateBorderColor: Color,
    val dividerColor: Color,
) {
    /**
     * Returns a copy of this DatePickerColors, optionally overriding some of the values. This
     * uses the Color.Unspecified to mean "use the value from the source".
     */
    fun copy(
        containerColor: Color = this.containerColor,
        titleContentColor: Color = this.titleContentColor,
        headlineContentColor: Color = this.headlineContentColor,
        weekdayContentColor: Color = this.weekdayContentColor,
        navigationContentColor: Color = this.navigationContentColor,
        yearContentColor: Color = this.yearContentColor,
        disabledYearContentColor: Color = this.disabledYearContentColor,
        currentYearContentColor: Color = this.currentYearContentColor,
        selectedYearContentColor: Color = this.selectedYearContentColor,
        disabledSelectedYearContentColor: Color = this.disabledSelectedYearContentColor,
        selectedYearContainerColor: Color = this.selectedYearContainerColor,
        disabledSelectedYearContainerColor: Color = this.disabledSelectedYearContainerColor,
        dayContentColor: Color = this.dayContentColor,
        disabledDayContentColor: Color = this.disabledDayContentColor,
        selectedDayContentColor: Color = this.selectedDayContentColor,
        disabledSelectedDayContentColor: Color = this.disabledSelectedDayContentColor,
        selectedDayContainerColor: Color = this.selectedDayContainerColor,
        disabledSelectedDayContainerColor: Color = this.disabledSelectedDayContainerColor,
        todayContentColor: Color = this.todayContentColor,
        todayDateBorderColor: Color = this.todayDateBorderColor,
        dividerColor: Color = this.dividerColor,
    ) =
        DatePickerColors(
            containerColor.takeOrElse { this.containerColor },
            titleContentColor.takeOrElse { this.titleContentColor },
            headlineContentColor.takeOrElse { this.headlineContentColor },
            weekdayContentColor.takeOrElse { this.weekdayContentColor },
            navigationContentColor.takeOrElse { this.navigationContentColor },
            yearContentColor.takeOrElse { this.yearContentColor },
            disabledYearContentColor.takeOrElse { this.disabledYearContentColor },
            currentYearContentColor.takeOrElse { this.currentYearContentColor },
            selectedYearContentColor.takeOrElse { this.selectedYearContentColor },
            disabledSelectedYearContentColor.takeOrElse { this.disabledSelectedYearContentColor },
            selectedYearContainerColor.takeOrElse { this.selectedYearContainerColor },
            disabledSelectedYearContainerColor.takeOrElse {
                this.disabledSelectedYearContainerColor
            },
            dayContentColor.takeOrElse { this.dayContentColor },
            disabledDayContentColor.takeOrElse { this.disabledDayContentColor },
            selectedDayContentColor.takeOrElse { this.selectedDayContentColor },
            disabledSelectedDayContentColor.takeOrElse { this.disabledSelectedDayContentColor },
            selectedDayContainerColor.takeOrElse { this.selectedDayContainerColor },
            disabledSelectedDayContainerColor.takeOrElse { this.disabledSelectedDayContainerColor },
            todayContentColor.takeOrElse { this.todayContentColor },
            todayDateBorderColor.takeOrElse { this.todayDateBorderColor },
            dividerColor.takeOrElse { this.dividerColor },
        )

    /**
     * Represents the content color for a calendar day.
     *
     * @param isToday indicates that the color is for a date that represents today
     * @param selected indicates that the color is for a selected day
     * @param enabled indicates that the day is enabled for selection
     */
    @Composable
    internal fun dayContentColor(isToday: Boolean, selected: Boolean, enabled: Boolean): State<Color> {
        val target =
            when {
                selected && enabled -> selectedDayContentColor
                selected && !enabled -> disabledSelectedDayContentColor
                isToday && enabled -> todayContentColor
                enabled -> dayContentColor
                else -> disabledDayContentColor
            }
        return animateColorAsState(target, tween(durationMillis = 200))
    }

    /**
     * Represents the container color for a calendar day.
     *
     * @param selected indicates that the color is for a selected day
     * @param enabled indicates that the day is enabled for selection
     * @param animate whether or not to animate a container color change
     */
    @Composable
    internal fun dayContainerColor(selected: Boolean, enabled: Boolean, animate: Boolean): State<Color> {
        val target =
            if (selected) {
                if (enabled) selectedDayContainerColor else disabledSelectedDayContainerColor
            } else {
                Color.Transparent
            }
        return if (animate) {
            animateColorAsState(target, tween(durationMillis = 200))
        } else {
            rememberUpdatedState(target)
        }
    }

    /**
     * Represents the content color for a calendar year.
     *
     * @param currentYear indicates that the color is for a year that represents the current year
     * @param selected indicates that the color is for a selected year
     * @param enabled indicates that the year is enabled for selection
     */
    @Composable
    internal fun yearContentColor(currentYear: Boolean, selected: Boolean, enabled: Boolean): State<Color> {
        val target =
            when {
                selected && enabled -> selectedYearContentColor
                selected && !enabled -> disabledSelectedYearContentColor
                currentYear && enabled -> currentYearContentColor
                enabled -> yearContentColor
                else -> disabledYearContentColor
            }
        return animateColorAsState(target, tween(durationMillis = 200))
    }

    /**
     * Represents the container color for a calendar year.
     *
     * @param selected indicates that the color is for a selected day
     * @param enabled indicates that the year is enabled for selection
     */
    @Composable
    internal fun yearContainerColor(selected: Boolean, enabled: Boolean): State<Color> {
        val target =
            if (selected) {
                if (enabled) selectedYearContainerColor else disabledSelectedYearContainerColor
            } else {
                Color.Transparent
            }
        return animateColorAsState(target, tween(durationMillis = 200))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is DatePickerColors) return false
        if (containerColor != other.containerColor) return false
        if (titleContentColor != other.titleContentColor) return false
        if (headlineContentColor != other.headlineContentColor) return false
        if (weekdayContentColor != other.weekdayContentColor) return false
        if (yearContentColor != other.yearContentColor) return false
        if (disabledYearContentColor != other.disabledYearContentColor) return false
        if (currentYearContentColor != other.currentYearContentColor) return false
        if (selectedYearContentColor != other.selectedYearContentColor) return false
        if (disabledSelectedYearContentColor != other.disabledSelectedYearContentColor) return false
        if (selectedYearContainerColor != other.selectedYearContainerColor) return false
        if (disabledSelectedYearContainerColor != other.disabledSelectedYearContainerColor) {
            return false
        }
        if (dayContentColor != other.dayContentColor) return false
        if (disabledDayContentColor != other.disabledDayContentColor) return false
        if (selectedDayContentColor != other.selectedDayContentColor) return false
        if (disabledSelectedDayContentColor != other.disabledSelectedDayContentColor) return false
        if (selectedDayContainerColor != other.selectedDayContainerColor) return false
        if (disabledSelectedDayContainerColor != other.disabledSelectedDayContainerColor) {
            return false
        }
        if (todayContentColor != other.todayContentColor) return false
        if (todayDateBorderColor != other.todayDateBorderColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + titleContentColor.hashCode()
        result = 31 * result + headlineContentColor.hashCode()
        result = 31 * result + weekdayContentColor.hashCode()
        result = 31 * result + yearContentColor.hashCode()
        result = 31 * result + disabledYearContentColor.hashCode()
        result = 31 * result + currentYearContentColor.hashCode()
        result = 31 * result + selectedYearContentColor.hashCode()
        result = 31 * result + disabledSelectedYearContentColor.hashCode()
        result = 31 * result + selectedYearContainerColor.hashCode()
        result = 31 * result + disabledSelectedYearContainerColor.hashCode()
        result = 31 * result + dayContentColor.hashCode()
        result = 31 * result + disabledDayContentColor.hashCode()
        result = 31 * result + selectedDayContentColor.hashCode()
        result = 31 * result + disabledSelectedDayContentColor.hashCode()
        result = 31 * result + selectedDayContainerColor.hashCode()
        result = 31 * result + disabledSelectedDayContainerColor.hashCode()
        result = 31 * result + todayContentColor.hashCode()
        result = 31 * result + todayDateBorderColor.hashCode()
        return result
    }
}

private fun Color.takeOrElse(block: () -> Color): Color = if (isSpecified) this else block()

private val Color.isSpecified: Boolean
    get() = this != Color.Unspecified

/**
 * An abstract for the date pickers states.
 *
 * @param initialDisplayedMonthMillis timestamp in _UTC_ milliseconds from the epoch that
 *   represents an initial selection of a month to be displayed to the user. In case `null` is
 *   provided, the displayed month would be the current one.
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited
 *   to
 * @param selectableDates a [SelectableDates] that is consulted to check if a date is allowed. In
 *   case a date is not allowed to be selected, it will appear disabled in the UI.
 * @param calendarType the [CalendarType] (Gregorian or Persian/Jalali) this state's dates are
 *   expressed in
 * @param requestedLocale a locale that will be used when formatting dates, determining the input
 *   format, week-days, and more. The effective [locale] exposed by this state is derived from
 *   this one, with an explicit `ca` (calendar) Unicode extension applied to match
 *   [calendarType] — see [createCalendarModel].
 * @throws [IllegalArgumentException] if the initial selected date or displayed month represent a
 *   year that is out of the year range.
 * @see rememberDatePickerState
 */
@Stable
internal abstract class BaseDatePickerStateImpl(
    initialDisplayedMonthMillis: Long?,
    val yearRange: IntRange,
    selectableDates: SelectableDates,
    val calendarType: CalendarType,
    requestedLocale: CalendarLocale,
) {

    val calendarModel = createCalendarModel(requestedLocale, calendarType)

    /** The effective, calendar-aware locale (carries the `ca` extension matching [calendarType]). */
    val locale: CalendarLocale
        get() = calendarModel.locale

    var selectableDates by mutableStateOf(selectableDates)

    private val _displayedMonth =
        mutableStateOf(
            if (initialDisplayedMonthMillis != null) {
                var month = calendarModel.getMonth(initialDisplayedMonthMillis)
                if (!yearRange.contains(month.year)) {
                    // The initial display month's year is out of the years range, so just set the
                    // displayed month to the current one.
                    month = calendarModel.getMonth(calendarModel.today)
                }
                month
            } else {
                // Set the displayed month to the current one.
                calendarModel.getMonth(calendarModel.today)
            }
        )

    var displayedMonthMillis: Long
        get() = _displayedMonth.value.startUtcTimeMillis
        set(monthMillis) {
            val month = calendarModel.getMonth(monthMillis)
            // Set the displayed month only if the month's year is within the years range.
            if (yearRange.contains(month.year)) {
                _displayedMonth.value = month
            }
        }
}

/**
 * A default implementation of the [DatePickerState]. See [rememberDatePickerState].
 *
 * @see rememberDatePickerState
 */
@Stable
private class DatePickerStateImpl(
    initialSelectedDateMillis: Long?,
    initialDisplayedMonthMillis: Long?,
    yearRange: IntRange,
    initialDisplayMode: DisplayMode,
    selectableDates: SelectableDates,
    calendarType: CalendarType,
    locale: CalendarLocale,
) :
    BaseDatePickerStateImpl(initialDisplayedMonthMillis, yearRange, selectableDates, calendarType, locale),
    DatePickerState {

    /** A mutable state of [CalendarDate] that represents a selected date. */
    private var _selectedDate =
        mutableStateOf(
            if (initialSelectedDateMillis != null) {
                val date = calendarModel.getCanonicalDate(initialSelectedDateMillis)
                // If the provided initial date's year is out of the years range, return null.
                // Otherwise, return the date.
                if (yearRange.contains(date.year)) date else null
            } else {
                null
            }
        )

    override var selectedDateMillis: Long?
        get() = _selectedDate.value?.utcTimeMillis
        set(dateMillis) {
            if (dateMillis != null) {
                val date = calendarModel.getCanonicalDate(dateMillis)
                // Validate that the give date is within the valid years range. In not, set the
                // selected date to null.
                _selectedDate.value = if (yearRange.contains(date.year)) date else null
            } else {
                _selectedDate.value = null
            }
        }

    /**
     * A mutable state of [DisplayMode] that represents the current display mode of the UI (i.e.
     * picker or input).
     */
    private var _displayMode = mutableStateOf(initialDisplayMode)

    override var displayMode
        get() = _displayMode.value
        set(displayMode) {
            selectedDateMillis?.let {
                displayedMonthMillis = calendarModel.getMonth(it).startUtcTimeMillis
            }
            _displayMode.value = displayMode
        }

    companion object {
        /**
         * The default [Saver] implementation for [DatePickerStateImpl].
         *
         * @param selectableDates a [SelectableDates] instance that is consulted to check if a
         *   date is allowed
         */
        fun Saver(
            selectableDates: SelectableDates,
            locale: CalendarLocale,
            calendarType: CalendarType,
        ): Saver<DatePickerStateImpl, Any> =
            listSaver(
                save = {
                    listOf(
                        it.selectedDateMillis,
                        it.displayedMonthMillis,
                        it.yearRange.first,
                        it.yearRange.last,
                        it.displayMode.value,
                    )
                },
                restore = { value ->
                    DatePickerStateImpl(
                        initialSelectedDateMillis = value[0] as Long?,
                        initialDisplayedMonthMillis = value[1] as Long?,
                        yearRange = IntRange(value[2] as Int, value[3] as Int),
                        initialDisplayMode = DisplayMode(value[4] as Int),
                        selectableDates = selectableDates,
                        calendarType = calendarType,
                        locale = locale,
                    )
                },
            )
    }
}

/**
 * A date formatter used by [DatePicker].
 *
 * The date formatter will apply the best possible localized form of the given skeleton and
 * Locale. A skeleton is similar to, and uses the same format characters as, a Unicode <a
 * href="http://www.unicode.org/reports/tr35/#Date_Format_Patterns">UTS #35</a> pattern.
 */
@Immutable
private class DatePickerFormatterImpl(
    val yearSelectionSkeleton: String,
    val selectedDateSkeleton: String,
    val selectedDateDescriptionSkeleton: String,
) : DatePickerFormatter {

    // A map for caching formatter related results for better performance
    private val formatterCache = mutableMapOf<String, Any>()

    override fun formatMonthYear(monthMillis: Long?, locale: CalendarLocale): String? {
        if (monthMillis == null) return null
        return formatWithSkeleton(monthMillis, yearSelectionSkeleton, locale, formatterCache)
    }

    override fun formatDate(dateMillis: Long?, locale: CalendarLocale, forContentDescription: Boolean): String? {
        if (dateMillis == null) return null
        return formatWithSkeleton(
            dateMillis,
            if (forContentDescription) {
                selectedDateDescriptionSkeleton
            } else {
                selectedDateSkeleton
            },
            locale,
            formatterCache,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is DatePickerFormatterImpl) return false
        if (yearSelectionSkeleton != other.yearSelectionSkeleton) return false
        if (selectedDateSkeleton != other.selectedDateSkeleton) return false
        if (selectedDateDescriptionSkeleton != other.selectedDateDescriptionSkeleton) return false
        return true
    }

    override fun hashCode(): Int {
        var result = yearSelectionSkeleton.hashCode()
        result = 31 * result + selectedDateSkeleton.hashCode()
        result = 31 * result + selectedDateDescriptionSkeleton.hashCode()
        return result
    }
}

/**
 * A base container for the date picker. This container composes the top common area of the UI,
 * and accepts [content] for the actual calendar picker.
 */
@Composable
internal fun DateEntryContainer(
    modifier: Modifier,
    title: (@Composable () -> Unit)?,
    headline: (@Composable () -> Unit)?,
    colors: DatePickerColors,
    headlineTextStyle: TextStyle,
    headerMinHeight: Dp,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .sizeIn(minWidth = DatePickerTokens.ContainerWidth)
                .semantics {
                    @Suppress("DEPRECATION")
                    isContainer = true
                }
                .background(colors.containerColor)
    ) {
        DatePickerHeader(
            modifier = Modifier,
            title = title,
            titleContentColor = colors.titleContentColor,
            headlineContentColor = colors.headlineContentColor,
            minHeight = headerMinHeight,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (headline != null) Arrangement.Start else Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (headline != null) {
                        ProvideTextStyle(value = headlineTextStyle) {
                            Box(modifier = Modifier.weight(1f)) { headline() }
                        }
                    }
                }
                // Display a divider only when there is a title or a headline.
                if (title != null || headline != null) {
                    HorizontalDivider(color = colors.dividerColor)
                }
            }
        }
        content()
    }
}

@Composable
private fun DatePickerContent(
    selectedDateMillis: Long?,
    displayedMonthMillis: Long,
    onDateSelectionChange: (dateInMillis: Long) -> Unit,
    onDisplayedMonthChange: (monthInMillis: Long) -> Unit,
    calendarModel: CalendarModel,
    yearRange: IntRange,
    dateFormatter: DatePickerFormatter,
    selectableDates: SelectableDates,
    colors: DatePickerColors,
) {
    val displayedMonth = calendarModel.getMonth(displayedMonthMillis)
    val monthIndex = displayedMonth.indexIn(yearRange).coerceAtLeast(0)
    val monthsListState = rememberLazyListState(initialFirstVisibleItemIndex = monthIndex)

    // Scroll to the resolved displayedMonth, if needed.
    LaunchedEffect(monthIndex) {
        if (
            !monthsListState.isScrollInProgress &&
                monthsListState.firstVisibleItemIndex != monthIndex
        ) {
            monthsListState.scrollToItem(monthIndex)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var yearPickerVisible by rememberSaveable { mutableStateOf(false) }
    Column {
        MonthsNavigation(
            modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding),
            nextAvailable = monthsListState.canScrollForward,
            previousAvailable = monthsListState.canScrollBackward,
            yearPickerVisible = yearPickerVisible,
            yearPickerText =
                dateFormatter.formatMonthYear(
                    monthMillis = displayedMonthMillis,
                    locale = calendarModel.locale,
                ) ?: "-",
            onNextClicked = {
                coroutineScope.launch {
                    try {
                        monthsListState.animateScrollToItem(
                            monthsListState.firstVisibleItemIndex + 1
                        )
                    } catch (_: IllegalArgumentException) {
                        // Ignore. This may happen if the user clicked the "next" arrow fast while
                        // the list was still animating to the next item.
                    }
                }
            },
            onPreviousClicked = {
                coroutineScope.launch {
                    try {
                        monthsListState.animateScrollToItem(
                            monthsListState.firstVisibleItemIndex - 1
                        )
                    } catch (_: IllegalArgumentException) {
                        // Ignore. This may happen if the user clicked the "previous" arrow fast
                        // while  the list was still animating to the previous item.
                    }
                }
            },
            onYearPickerButtonClicked = { yearPickerVisible = !yearPickerVisible },
            colors = colors,
        )

        Box {
            if (!yearPickerVisible) {
                Column(modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding)) {
                    WeekDays(colors, calendarModel)
                    HorizontalMonthsList(
                        lazyListState = monthsListState,
                        selectedDateMillis = selectedDateMillis,
                        onDateSelectionChange = onDateSelectionChange,
                        onDisplayedMonthChange = onDisplayedMonthChange,
                        calendarModel = calendarModel,
                        yearRange = yearRange,
                        dateFormatter = dateFormatter,
                        selectableDates = selectableDates,
                        colors = colors,
                    )
                }
            } else {
                val yearsPaneTitle = getString(Strings.DatePickerYearPickerPaneTitle)
                Column(modifier = Modifier.semantics { paneTitle = yearsPaneTitle }) {
                    YearPicker(
                        modifier =
                            Modifier.requiredHeight(
                                    RecommendedSizeForAccessibility * (MaxCalendarRows + 1) -
                                        DividerDefaults.Thickness
                                )
                                .padding(horizontal = DatePickerHorizontalPadding),
                        displayedMonthMillis = displayedMonthMillis,
                        onYearSelected = { year ->
                            // Switch back to the monthly calendar and scroll to the selected year.
                            yearPickerVisible = !yearPickerVisible
                            coroutineScope.launch {
                                monthsListState.scrollToItem(
                                    (year - yearRange.first) * 12 + displayedMonth.month - 1
                                )
                            }
                        },
                        selectableDates = selectableDates,
                        calendarModel = calendarModel,
                        yearRange = yearRange,
                        colors = colors,
                    )
                    HorizontalDivider(color = colors.dividerColor)
                }
            }
        }
    }
}

@Composable
internal fun DatePickerHeader(
    modifier: Modifier,
    title: (@Composable () -> Unit)?,
    titleContentColor: Color,
    headlineContentColor: Color,
    minHeight: Dp,
    content: @Composable () -> Unit,
) {
    // Apply a defaultMinSize only when the title is not null.
    val heightModifier =
        if (title != null) {
            Modifier.defaultMinSize(minHeight = minHeight)
        } else {
            Modifier
        }
    Column(
        modifier.fillMaxWidth().then(heightModifier),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        if (title != null) {
            val textStyle = DatePickerTokens.HeaderSupportingTextFont
            ProvideContentColorTextStyle(contentColor = titleContentColor, textStyle = textStyle) {
                Box(contentAlignment = Alignment.BottomStart) { title() }
            }
        }
        CompositionLocalProvider(LocalContentColor provides headlineContentColor, content = content)
    }
}

/** Composes a horizontal pageable list of months. */
@Composable
private fun HorizontalMonthsList(
    lazyListState: LazyListState,
    selectedDateMillis: Long?,
    onDateSelectionChange: (dateInMillis: Long) -> Unit,
    onDisplayedMonthChange: (monthInMillis: Long) -> Unit,
    calendarModel: CalendarModel,
    yearRange: IntRange,
    dateFormatter: DatePickerFormatter,
    selectableDates: SelectableDates,
    colors: DatePickerColors,
) {
    val today = calendarModel.today
    val firstMonth =
        remember(yearRange) {
            calendarModel.getMonth(
                year = yearRange.first,
                month = 1, // January
            )
        }
    ProvideTextStyle(DatePickerTokens.DateLabelTextFont) {
        LazyRow(
            // Apply this to prevent the screen reader from scrolling to the next or previous
            // month, and instead, traverse outside the Month composable when swiping from a
            // focused first or last day of the month.
            modifier =
                Modifier.semantics {
                    horizontalScrollAxisRange = ScrollAxisRange(value = { 0f }, maxValue = { 0f })
                },
            state = lazyListState,
            flingBehavior = DatePickerDefaults.rememberSnapFlingBehavior(lazyListState),
        ) {
            items(numberOfMonthsInRange(yearRange)) {
                val month = calendarModel.plusMonths(from = firstMonth, addedMonthsCount = it)
                Box(modifier = Modifier.fillParentMaxWidth()) {
                    Month(
                        month = month,
                        onDateSelectionChange = onDateSelectionChange,
                        todayMillis = today.utcTimeMillis,
                        selectedDateMillis = selectedDateMillis,
                        dateFormatter = dateFormatter,
                        selectableDates = selectableDates,
                        colors = colors,
                        locale = calendarModel.locale,
                    )
                }
            }
        }
    }

    LaunchedEffect(lazyListState) {
        updateDisplayedMonth(
            lazyListState = lazyListState,
            onDisplayedMonthChange = onDisplayedMonthChange,
            calendarModel = calendarModel,
            yearRange = yearRange,
        )
    }
}

internal suspend fun updateDisplayedMonth(
    lazyListState: LazyListState,
    onDisplayedMonthChange: (monthInMillis: Long) -> Unit,
    calendarModel: CalendarModel,
    yearRange: IntRange,
) {
    snapshotFlow { lazyListState.firstVisibleItemIndex }
        .collect {
            val yearOffset = lazyListState.firstVisibleItemIndex / 12
            val month = lazyListState.firstVisibleItemIndex % 12 + 1
            onDisplayedMonthChange(
                calendarModel
                    .getMonth(year = yearRange.first + yearOffset, month = month)
                    .startUtcTimeMillis
            )
        }
}

/** Composes the weekdays letters. */
@Composable
internal fun WeekDays(colors: DatePickerColors, calendarModel: CalendarModel) {
    val firstDayOfWeek = calendarModel.firstDayOfWeek
    val weekdays = calendarModel.weekdayNames
    val dayNames = arrayListOf<Pair<String, String>>()
    // Start with firstDayOfWeek - 1 as the days are 1-based.
    for (i in firstDayOfWeek - 1 until weekdays.size) {
        dayNames.add(weekdays[i])
    }
    for (i in 0 until firstDayOfWeek - 1) {
        dayNames.add(weekdays[i])
    }
    val textStyle = DatePickerTokens.WeekdaysLabelTextFont

    Row(
        modifier =
            Modifier.defaultMinSize(minHeight = RecommendedSizeForAccessibility).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dayNames.fastForEach {
            Box(
                modifier =
                    Modifier.clearAndSetSemantics { contentDescription = it.first }
                        .sizeIn(
                            minWidth = DatePickerTokens.DateContainerWidth,
                            minHeight = DatePickerTokens.DateContainerHeight,
                        )
                        .size(
                            width = LocalMinimumInteractiveComponentSize.current,
                            height = LocalMinimumInteractiveComponentSize.current,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = it.second,
                    modifier = Modifier.wrapContentSize(),
                    color = colors.weekdayContentColor,
                    style = textStyle,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** A composable that renders a calendar month and displays a date selection. */
@Composable
internal fun Month(
    month: CalendarMonth,
    onDateSelectionChange: (dateInMillis: Long) -> Unit,
    todayMillis: Long,
    selectedDateMillis: Long?,
    dateFormatter: DatePickerFormatter,
    selectableDates: SelectableDates,
    colors: DatePickerColors,
    locale: CalendarLocale,
) {
    var cellIndex = 0
    Column(
        modifier = Modifier.requiredHeight(RecommendedSizeForAccessibility * MaxCalendarRows),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (weekIndex in 0 until MaxCalendarRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (dayIndex in 0 until DaysInWeek) {
                    if (
                        cellIndex < month.daysFromStartOfWeekToFirstOfMonth ||
                            cellIndex >=
                                (month.daysFromStartOfWeekToFirstOfMonth + month.numberOfDays)
                    ) {
                        // Empty cell.
                        Spacer(
                            modifier =
                                Modifier.sizeIn(
                                        minWidth = DatePickerTokens.DateContainerWidth,
                                        minHeight = DatePickerTokens.DateContainerHeight,
                                    )
                                    .size(
                                        width = LocalMinimumInteractiveComponentSize.current,
                                        height = LocalMinimumInteractiveComponentSize.current,
                                    )
                        )
                    } else {
                        val dayNumber = cellIndex - month.daysFromStartOfWeekToFirstOfMonth
                        val dateInMillis =
                            month.startUtcTimeMillis + (dayNumber * MillisecondsIn24Hours)
                        val isToday = dateInMillis == todayMillis
                        val selected = dateInMillis == selectedDateMillis
                        val dayContentDescription = dayContentDescription(isToday = isToday)
                        val formattedDateDescription =
                            dateFormatter.formatDate(
                                dateInMillis,
                                locale,
                                forContentDescription = true,
                            ) ?: ""
                        Day(
                            text = (dayNumber + 1).toLocalString(locale = locale),
                            modifier = Modifier,
                            selected = selected,
                            onClick = { onDateSelectionChange(dateInMillis) },
                            animateChecked = selected,
                            enabled =
                                remember(dateInMillis, selectableDates) {
                                    with(selectableDates) {
                                        isSelectableYear(month.year) &&
                                            isSelectableDate(dateInMillis)
                                    }
                                },
                            today = isToday,
                            description =
                                if (dayContentDescription != null) {
                                    "$dayContentDescription, $formattedDateDescription"
                                } else {
                                    formattedDateDescription
                                },
                            colors = colors,
                        )
                    }
                    cellIndex++
                }
            }
        }
    }
}

/** Returns the number of months within the given year range. */
internal fun numberOfMonthsInRange(yearRange: IntRange) =
    (yearRange.last - yearRange.first + 1) * 12

@Composable
private fun dayContentDescription(isToday: Boolean): String? =
    if (isToday) getString(string = Strings.DatePickerTodayDescription) else null

@Composable
private fun Day(
    text: String,
    modifier: Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    animateChecked: Boolean,
    enabled: Boolean,
    today: Boolean,
    description: String,
    colors: DatePickerColors,
) {
    Surface(
        selected = selected,
        onClick = onClick,
        modifier =
            modifier.semantics(mergeDescendants = true) {
                this.text = AnnotatedString(description)
                this.role = Role.Button
            },
        enabled = enabled,
        shape = DatePickerTokens.DateContainerShape,
        color =
            colors
                .dayContainerColor(selected = selected, enabled = enabled, animate = animateChecked)
                .value,
        border =
            if (today && !selected) {
                BorderStroke(DatePickerTokens.DateTodayContainerOutlineWidth, colors.todayDateBorderColor)
            } else {
                null
            },
    ) {
        Box(
            modifier =
                Modifier.requiredSize(DatePickerTokens.DateContainerWidth, DatePickerTokens.DateContainerHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                modifier = Modifier.clearAndSetSemantics {},
                color = colors.dayContentColor(isToday = today, selected = selected, enabled = enabled).value,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun YearPicker(
    modifier: Modifier,
    displayedMonthMillis: Long,
    onYearSelected: (year: Int) -> Unit,
    selectableDates: SelectableDates,
    calendarModel: CalendarModel,
    yearRange: IntRange,
    colors: DatePickerColors,
) {
    ProvideTextStyle(value = DatePickerTokens.SelectionYearLabelTextFont) {
        val currentYear = calendarModel.getMonth(calendarModel.today).year
        val displayedYear = calendarModel.getMonth(displayedMonthMillis).year
        val lazyGridState =
            rememberLazyGridState(
                initialFirstVisibleItemIndex = max(0, displayedYear - yearRange.first - YearsInRow)
            )
        LazyVerticalGrid(
            columns = GridCells.Fixed(YearsInRow),
            modifier = modifier.background(colors.containerColor),
            state = lazyGridState,
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(YearsVerticalPadding),
        ) {
            items(yearRange.count()) {
                val selectedYear = it + yearRange.first
                val localizedYear = selectedYear.toLocalString(locale = calendarModel.locale)
                Year(
                    text = localizedYear,
                    modifier =
                        Modifier.requiredSize(
                            width = DatePickerTokens.SelectionYearContainerWidth,
                            height = DatePickerTokens.SelectionYearContainerHeight,
                        ),
                    selected = selectedYear == displayedYear,
                    currentYear = selectedYear == currentYear,
                    onClick = { onYearSelected(selectedYear) },
                    enabled = selectableDates.isSelectableYear(selectedYear),
                    description =
                        formatDatePickerNavigateToYearString(
                            getString(Strings.DatePickerNavigateToYearDescription),
                            localizedYear,
                        ),
                    colors = colors,
                )
            }
        }
    }
}

private fun formatDatePickerNavigateToYearString(template: String, localizedYear: String): String =
    template.format(localizedYear)

@Composable
private fun Year(
    text: String,
    modifier: Modifier,
    selected: Boolean,
    currentYear: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    description: String,
    colors: DatePickerColors,
) {
    val border =
        remember(currentYear, selected) {
            if (currentYear && !selected) {
                BorderStroke(DatePickerTokens.DateTodayContainerOutlineWidth, colors.todayDateBorderColor)
            } else {
                null
            }
        }
    Surface(
        selected = selected,
        onClick = onClick,
        modifier =
            modifier.semantics(mergeDescendants = true) {
                this.text = AnnotatedString(description)
                this.role = Role.Button
            },
        enabled = enabled,
        shape = DatePickerTokens.SelectionYearStateLayerShape,
        color = colors.yearContainerColor(selected = selected, enabled = enabled).value,
        border = border,
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                modifier = Modifier.clearAndSetSemantics {},
                color =
                    colors
                        .yearContentColor(currentYear = currentYear, selected = selected, enabled = enabled)
                        .value,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * A composable that shows a year menu button and a couple of buttons that enable navigation
 * between displayed months.
 */
@Composable
private fun MonthsNavigation(
    modifier: Modifier,
    nextAvailable: Boolean,
    previousAvailable: Boolean,
    yearPickerVisible: Boolean,
    yearPickerText: String,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    onYearPickerButtonClicked: () -> Unit,
    colors: DatePickerColors,
) {
    Row(
        modifier = modifier.fillMaxWidth().requiredHeight(MonthYearHeight),
        horizontalArrangement = if (yearPickerVisible) Arrangement.Start else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // A menu button for selecting a year.
        YearPickerMenuButton(onClick = onYearPickerButtonClicked, expanded = yearPickerVisible) {
            Text(
                text = yearPickerText,
                modifier =
                    Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = yearPickerText
                    },
                color = colors.navigationContentColor,
            )
        }
        // Show arrows for traversing months (only visible when the year selection is off)
        if (!yearPickerVisible) {
            CompositionLocalProvider(LocalContentColor provides colors.navigationContentColor) {
                Row {
                    IconButtonWithTooltip(
                        onClick = onPreviousClicked,
                        enabled = previousAvailable,
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = getString(Strings.DatePickerSwitchToPreviousMonth),
                    )

                    IconButtonWithTooltip(
                        onClick = onNextClicked,
                        enabled = nextAvailable,
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = getString(Strings.DatePickerSwitchToNextMonth),
                    )
                }
            }
        }
    }
}

@Composable
private fun YearPickerMenuButton(
    onClick: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
        elevation = null,
        border = null,
    ) {
        content()
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Icon(
            Icons.Filled.ArrowDropDown,
            contentDescription =
                if (expanded) {
                    getString(Strings.DatePickerSwitchToDaySelection)
                } else {
                    getString(Strings.DatePickerSwitchToYearSelection)
                },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconButtonWithTooltip(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(contentDescription) } },
        state = rememberTooltipState(),
    ) {
        IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}

internal val RecommendedSizeForAccessibility = 48.dp
internal val MonthYearHeight = 56.dp
internal val DatePickerHorizontalPadding = 12.dp

private val DatePickerTitlePadding = PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)
private val DatePickerHeadlinePadding = PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp)

private val YearsVerticalPadding = 16.dp

private const val MaxCalendarRows = 6
private const val YearsInRow: Int = 3
