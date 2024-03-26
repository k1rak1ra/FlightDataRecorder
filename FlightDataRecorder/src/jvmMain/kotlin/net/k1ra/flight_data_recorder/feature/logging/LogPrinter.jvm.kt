package net.k1ra.flight_data_recorder.feature.logging

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal actual object LogPrinter {
    actual fun printLog(tag: String, message: String, level: LogLevels) {
        println("${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString().replace("T"," ")} [$tag] ${level.name} $message")
    }
}