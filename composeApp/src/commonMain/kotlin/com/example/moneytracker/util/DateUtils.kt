package com.example.moneytracker.util

import kotlinx.datetime.*

fun LocalDate.atStartOfMonth(): LocalDate {
    return LocalDate(year, month, 1)
}

fun getCurrentMonthStart(): LocalDate {
    return Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .atStartOfMonth()
}

fun formatDate(date: LocalDate): String {
    return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
} 