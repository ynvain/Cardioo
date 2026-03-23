package com.cardioo.presentation.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private fun zoned(epochMillis: Long) =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())

/** Formats date/time using the device locale (follows system language). */
fun formatLocalizedDateTime(epochMillis: Long): String {
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
        .format(zoned(epochMillis))
}

fun formatLocalizedDate(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .format(zoned(epochMillis))

fun formatLocalizedTime(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
        .format(zoned(epochMillis))
