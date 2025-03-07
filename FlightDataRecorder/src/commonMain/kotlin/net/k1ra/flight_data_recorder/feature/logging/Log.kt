package net.k1ra.flight_data_recorder.feature.logging

import net.k1ra.flight_data_recorder.feature.batching.BatchLoggingManager
import net.k1ra.flight_data_recorder.feature.config.FlightDataRecorderConfig
import net.k1ra.flight_data_recorder.feature.model.FlightDataRecorderMetadata

object Log {
    private var batchLoggingManager: BatchLoggingManager? = null

    fun wtf(tag: String, message: String, additionalMetadata: Map<String, FlightDataRecorderMetadata> = mutableMapOf()) = processLog(tag, message, LogLevels.WTF, additionalMetadata)

    fun e(tag: String, message: String, additionalMetadata: Map<String, FlightDataRecorderMetadata> = mutableMapOf()) = processLog(tag, message, LogLevels.ERROR, additionalMetadata)

    fun w(tag: String, message: String, additionalMetadata: Map<String, FlightDataRecorderMetadata> = mutableMapOf()) = processLog(tag, message, LogLevels.WARNING, additionalMetadata)

    fun i(tag: String, message: String, additionalMetadata: Map<String, FlightDataRecorderMetadata> = mutableMapOf()) = processLog(tag, message, LogLevels.INFO, additionalMetadata)

    fun d(tag: String, message: String, additionalMetadata: Map<String, FlightDataRecorderMetadata> = mutableMapOf()) = processLog(tag, message, LogLevels.DEBUG, additionalMetadata)

    fun v(tag: String, message: String, additionalMetadata: Map<String, FlightDataRecorderMetadata> = mutableMapOf()) = processLog(tag, message, LogLevels.VERBOSE, additionalMetadata)

    private fun processLog(tag: String, message: String, level: LogLevels, additionalMetadata: Map<String, FlightDataRecorderMetadata>) {
        if (FlightDataRecorderConfig.logLevel.ordinal >= level.ordinal) {
            LogPrinter.printLog(tag, message, level)

            //If log server and app keys are set, enable batch logging and uploading
            FlightDataRecorderConfig.appKey?.let { appKey ->
                if (FlightDataRecorderConfig.logServer != null) {
                    if (batchLoggingManager == null || batchLoggingManager?.appKey != FlightDataRecorderConfig.appKey)
                        batchLoggingManager = BatchLoggingManager(appKey)

                    batchLoggingManager?.consumeLog(tag, message, level, additionalMetadata)
                }
            }
        }
    }
}