package net.k1ra.flight_data_recorder_server.model.dao.logging

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.k1ra.flight_data_recorder.model.projects.LogLine
import net.k1ra.flight_data_recorder_server.model.dao.projects.ProjectsDao
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.exists
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.json.jsonb

class LogsDao(id: EntityID<Int>) : Entity<Int>(id) {
    object LogsTable : IntIdTable() {
        val project = reference("project", ProjectsDao.ProjectsTable)
        val datetime = datetime("datetime")
        val logLine = jsonb("logLine", {jsonObject ->  jsonObject.toString()}, {s: String ->  Json.parseToJsonElement(s) as JsonObject})
        val ipAddr = text("ipAddr")
        val deviceId = varchar("deviceId", 36)

        fun initDb() {
            transaction {
                if (!LogsTable.exists())
                    SchemaUtils.create(LogsTable)
            }
        }
    }

    companion object : EntityClass<Int, LogsDao>(LogsTable)

    var project by ProjectsDao referencedOn LogsTable.project
    var datetime by LogsTable.datetime
    var logLine by LogsTable.logLine
    var ipAddr by LogsTable.ipAddr
    var deviceId by LogsTable.deviceId
}

fun LogsDao.toLogLine() : LogLine {
    return LogLine(
        datetime,
        logLine,
        deviceId
    )
}