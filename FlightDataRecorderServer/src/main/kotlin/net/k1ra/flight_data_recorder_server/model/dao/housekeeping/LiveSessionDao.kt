package net.k1ra.flight_data_recorder_server.model.dao.housekeeping

import net.k1ra.flight_data_recorder_server.model.dao.authentication.SessionsDao
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

class LiveSessionDao(id: EntityID<Int>) : Entity<Int>(id) {
    object LiveSessionTable : IntIdTable() {
        val instance = reference("instance", ServerInstanceDao.ServerInstanceTable)
        val session = reference("session", SessionsDao.SessionsTable).nullable()
        val ipAddr = text("ipAddr")
        val ipCity = text("ipCity")
        val ipState = text("ipState")
        val ipCountry = text("ipCountry")
        val ipLatitude = text("ipLatitude")
        val ipLongitude = text("ipLongitude")
        val lastUpdated = datetime("lastUpdated")

        fun initDb() {
            transaction {
                if (!LiveSessionTable.exists())
                    SchemaUtils.create(LiveSessionTable)
            }
        }
    }

    companion object : EntityClass<Int, LiveSessionDao>(LiveSessionTable)

    var instance by ServerInstanceDao referencedOn LiveSessionTable.instance
    var session by SessionsDao optionalReferencedOn LiveSessionTable.session
    var ipAddr by LiveSessionTable.ipAddr
    var ipCity by LiveSessionTable.ipCity
    var ipState by LiveSessionTable.ipState
    var ipCountry by LiveSessionTable.ipCountry
    var ipLatitude by LiveSessionTable.ipLatitude
    var ipLongitude by LiveSessionTable.ipLongitude
    var lastUpdated by LiveSessionTable.lastUpdated
}