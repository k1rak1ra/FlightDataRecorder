package net.k1ra.flight_data_recorder_server.model.dao.authentication

import net.k1ra.flight_data_recorder.model.authentication.ClientUserData
import net.k1ra.flight_data_recorder.model.authentication.UserRole
import net.k1ra.flight_data_recorder_server.model.dao.housekeeping.LiveSessionDao
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

class SessionsDao(id: EntityID<Int>) : Entity<Int>(id) {
    object SessionsTable : IntIdTable() {
        val user = reference("user", UsersDao.UsersTable)
        val token = char("token", 36)
        val lastLogin = datetime("lastLogin")

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
    var lastLogin by SessionsTable.lastLogin

    val liveSessionData by LiveSessionDao optionalReferrersOn LiveSessionDao.LiveSessionTable.session
}

fun SessionsDao.toClientUserData() : ClientUserData = transaction {
    return@transaction ClientUserData(
        user.name,
        user.picture,
        user.email,
        user.username,
        user.uid,
        token,
        user.native,
        user.role
    )
}