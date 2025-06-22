package com.example.moneytracker.util

import kotlin.math.roundToInt

/**
 * Formats a Double to a String with 2 decimal places.
 * This is a basic implementation. For more complex currency formatting (e.g., locale-specific symbols, grouping),
 * expect/actual or a dedicated KMM currency formatting library would be needed.
 */
fun Double.toCurrencyString(): String {
    val a = this * 100
    val rounded = (this * 100).roundToInt()
    val major = rounded / 100
    val minor = rounded % 100
    val minorString = if (minor < 10) "0$minor" else minor.toString()
    return "$major,$minorString"
}

/**
 * Formats a Double to a String with a specified number of decimal places.
 * Uses comma as decimal separator.
 */
fun Double.formatDecimalPlaces(digits: Int): String {
    if (digits <= 0) {
        return this.roundToInt().toString()
    }
    var factor = 1.0
    repeat(digits) { factor *= 10 }
    val rounded = (this * factor).roundToInt()

    val major = rounded / factor.toInt()
    val minor = rounded % factor.toInt()

    val minorString = minor.toString().padStart(digits, '0')
    return "$major,$minorString"
}
