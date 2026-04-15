package net.k1ra.flight_data_recorder_server.model.dao.projects

import net.k1ra.flight_data_recorder.model.projects.IpGeolocationData
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.exists
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

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