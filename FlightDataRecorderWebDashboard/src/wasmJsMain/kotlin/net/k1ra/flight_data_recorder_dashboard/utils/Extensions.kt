package net.k1ra.flight_data_recorder_dashboard.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

fun LocalDateTime.formatDateTime(): String {
    val format = LocalDateTime.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        dayOfMonth()

        char(' ')

        hour()
        char(':')
        minute()
        char(':')
        second()
    }

    return format.format(this)
}