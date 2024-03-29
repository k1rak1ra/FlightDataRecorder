package net.k1ra.flight_data_recorder_server.model.dao.logging

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.k1ra.flight_data_recorder_server.model.dao.projects.ProjectsDao
import net.k1ra.flight_data_recorder_server.model.dao.projects.SharePermissionsDao
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

class LogsDao(id: EntityID<Int>) : Entity<Int>(id) {
    object LogsTable : IntIdTable() {
        val project = reference("project", ProjectsDao.ProjectsTable)
        val datetime = datetime("datetime")
        val logLine = jsonb("logLine", {jsonObject ->  jsonObject.toString()}, {s: String ->  Json.parseToJsonElement(s) as JsonObject})

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
}