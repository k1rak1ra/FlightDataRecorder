package net.k1ra.flight_data_recorder_server.model.dao.housekeeping

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

class ServerInstanceDao(id: EntityID<Int>) : Entity<Int>(id) {
    object ServerInstanceTable : IntIdTable() {
        val lastUpdate = datetime("lastUpdate")

        fun initDb() {
            transaction {
                if (!ServerInstanceTable.exists())
                    SchemaUtils.create(ServerInstanceTable)
            }
        }
    }

    companion object : EntityClass<Int, ServerInstanceDao>(ServerInstanceTable)

    val sessions by LiveSessionDao referrersOn LiveSessionDao.LiveSessionTable.instance
    var lastUpdate by ServerInstanceTable.lastUpdate
}