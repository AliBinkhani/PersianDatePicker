/*
 * Vendored and trimmed from androidx.compose.material3:material3-android:1.4.0
 * (CalendarLocale.kt / CalendarLocale.android.kt / CalendarLocale.jvm.kt), because the originals
 * are `internal` to the material3 module.
 *
 * Copyright 2023 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 */

package com.hooshkar.persiandatepicker

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import java.text.NumberFormat
import java.util.Locale
import java.util.WeakHashMap

/**
 * Represents a Locale for the calendar. This locale will be used when formatting dates,
 * determining the input format, and more.
 */
internal typealias CalendarLocale = Locale

/** Returns the default [CalendarLocale]. */
@Composable
@ReadOnlyComposable
internal fun defaultLocale(): CalendarLocale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocalConfiguration.current.locales[0]
    } else {
        ConfigurationCompat.getLocales(LocalConfiguration.current).get(0) ?: Locale.getDefault()
    }
}

private val cachedNumberFormatters = WeakHashMap<String, NumberFormat>()

/** Returns a string representation of an integer for the given [locale]. */
internal fun Int.toLocalString(
    minDigits: Int = 1,
    maxDigits: Int = 40,
    isGroupingUsed: Boolean = false,
    locale: CalendarLocale? = null,
): String {
    val resolvedLocale = locale ?: Locale.getDefault()
    val key = "$minDigits.$maxDigits.$isGroupingUsed.${resolvedLocale.toLanguageTag()}"
    val formatter =
        cachedNumberFormatters.getOrPut(key) {
            NumberFormat.getIntegerInstance(/* inLocale= */ resolvedLocale).apply {
                this.isGroupingUsed = isGroupingUsed
                this.minimumIntegerDigits = minDigits
                this.maximumIntegerDigits = maxDigits
            }
        }
    return formatter.format(this)
}
