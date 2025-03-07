package net.k1ra.flight_data_recorder.model.projects

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LogLine(
    val datetime: LocalDateTime,
    val contents: JsonObject,
    val deviceId: String,
)