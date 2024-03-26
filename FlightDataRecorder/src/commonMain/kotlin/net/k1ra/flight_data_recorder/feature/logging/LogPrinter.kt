package net.k1ra.flight_data_recorder.feature.logging

internal expect object LogPrinter {
    fun printLog(tag: String, message: String, level: LogLevels)
}