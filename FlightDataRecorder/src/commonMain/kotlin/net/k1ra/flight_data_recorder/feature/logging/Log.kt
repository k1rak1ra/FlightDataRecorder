package net.k1ra.flight_data_recorder.feature.logging

import net.k1ra.flight_data_recorder.feature.config.FlightDataRecorderConfig
import net.k1ra.flight_data_recorder.feature.config.PlatformSpecificInit

object Log {
    fun wtf(tag: String, message: String) = processLog(tag, message, LogLevels.WTF)

    fun e(tag: String, message: String) = processLog(tag, message, LogLevels.ERROR)

    fun w(tag: String, message: String) = processLog(tag, message, LogLevels.WARNING)

    fun i(tag: String, message: String) = processLog(tag, message, LogLevels.INFO)

    fun d(tag: String, message: String) = processLog(tag, message, LogLevels.DEBUG)

    fun v(tag: String, message: String) = processLog(tag, message, LogLevels.VERBOSE)

    private fun processLog(tag: String, message: String, level: LogLevels) {
        if (!didPlatformSpecificInit) {
            didPlatformSpecificInit = true
            PlatformSpecificInit.init()
        }

        if (FlightDataRecorderConfig.logLevel.ordinal <= level.ordinal) {
            LogPrinter.printLog(tag, message, level)


        }
    }

    private var didPlatformSpecificInit = false
}