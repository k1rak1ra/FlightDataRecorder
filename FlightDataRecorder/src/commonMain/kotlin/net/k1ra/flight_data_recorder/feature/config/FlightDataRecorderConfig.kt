package net.k1ra.flight_data_recorder.feature.config

import net.k1ra.flight_data_recorder.feature.logging.LogLevels

object FlightDataRecorderConfig {
    var logLevel = LogLevels.VERBOSE //Logs that are lower priority than this will be completely ignored
    var logServer: String? = null
    var appKey: String? = null
    var batchLimit = 20 //Once there's this many log lines, the logs will be uploaded
}