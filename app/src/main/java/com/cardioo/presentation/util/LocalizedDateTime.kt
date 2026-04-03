package com.cardioo.presentation.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

fun zoned(epochMillis: Long): ZonedDateTime =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())


fun formatLocalizedDate(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .format(zoned(epochMillis))

fun formatLocalizedTime(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
        .format(zoned(epochMillis))

fun formatLocalizedDateWithoutYear(epochMillis: Long): String {
    val localizedDate = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
        .format(zoned(epochMillis))
    //remove trailing dot in localized string
    return if (localizedDate.endsWith(".")) localizedDate.substring(
        0, localizedDate.length - 1
    ) else localizedDate

}

fun formatLocalizedDayOfWeek(epochMillis: Long): String =
    zoned(epochMillis).dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())

fun getYear(epochMillis: Long): Int =
    zoned(epochMillis).year

