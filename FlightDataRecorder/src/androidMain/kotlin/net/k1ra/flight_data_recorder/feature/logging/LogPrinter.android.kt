package net.k1ra.flight_data_recorder.feature.logging

import android.util.Log

internal actual object LogPrinter {
    actual fun printLog(tag: String, message: String, level: LogLevels) {
        when (level) {
            LogLevels.WTF -> Log.wtf(tag, message)
            LogLevels.ERROR -> Log.e(tag, message)
            LogLevels.WARNING -> Log.w(tag, message)
            LogLevels.INFO -> Log.i(tag, message)
            LogLevels.DEBUG -> Log.d(tag, message)
            LogLevels.VERBOSE -> Log.v(tag, message)
        }
    }
}