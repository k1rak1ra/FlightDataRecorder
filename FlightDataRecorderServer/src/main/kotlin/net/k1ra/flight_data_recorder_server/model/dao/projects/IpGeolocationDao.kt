package net.k1ra.flight_data_recorder_server.model.dao.projects

import net.k1ra.flight_data_recorder.model.projects.IpGeolocationData
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

class IpGeolocationDao(id: EntityID<Int>) : Entity<Int>(id) {
    object IpGeolocationTable : IntIdTable() {
        val ipAddr = text("ipAddr")
        val city = text("city")
        val state = text("state")
        val country = text("country")
        val latitude = text("latitude")
        val longitude = text("longitude")

        fun initDb() {
            transaction {
                if (!IpGeolocationTable.exists())
                    SchemaUtils.create(IpGeolocationTable)
            }
        }
    }

    companion object : EntityClass<Int, IpGeolocationDao>(IpGeolocationTable)

    var ipAddr by IpGeolocationTable.ipAddr
    var city by IpGeolocationTable.city
    var state by IpGeolocationTable.state
    var country by IpGeolocationTable.country
    var latitude by IpGeolocationTable.latitude
    var longitude by IpGeolocationTable.longitude
}

fun IpGeolocationDao.toIpGeolocationData() : IpGeolocationData {
    return IpGeolocationData(
        ipAddr,
        city,
        state,
        country,
        latitude,
        longitude
    )
}