package net.k1ra.flight_data_recorder_server.model.dao.authentication

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction

class SessionsDao(id: EntityID<Int>) : Entity<Int>(id) {
    object SessionsTable : IntIdTable() {
        val user = reference("user", UsersDao.UsersTable)
        val token = char("token", 36)

        fun initDb() {
            transaction {
                if (!SessionsTable.exists())
                    SchemaUtils.create(SessionsTable)
            }
        }
    }

    companion object : EntityClass<Int, SessionsDao>(SessionsTable)

    var user by UsersDao referencedOn SessionsTable.user
    var token by SessionsTable.token
}