package net.k1ra.flight_data_recorder_server.viewmodel.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.long
import net.k1ra.flight_data_recorder.feature.logging.Log
import net.k1ra.flight_data_recorder_server.model.dao.logging.LogsDao
import net.k1ra.flight_data_recorder_server.model.dao.projects.ProjectsDao
import org.jetbrains.exposed.sql.transactions.transaction

object LogsViewModel {
    fun insertBatch(targetProject: ProjectsDao, logs: JsonArray) {
        logs.forEach { line ->
            CoroutineScope(Dispatchers.IO).launch {
                transaction {
                    try {
                        line as JsonObject
                        val logDatetime = Instant.fromEpochMilliseconds((line["DateTime"] as JsonPrimitive).long).toLocalDateTime(TimeZone.UTC)

                        LogsDao.new {
                            project = targetProject
                            datetime = logDatetime
                            logLine = line
                        }

                    } catch (e: Exception) {
                        Log.e("BatchInsert", "MALFORMED LOG LINE $e")
                    }
                }
            }
        }
    }
}