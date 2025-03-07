package net.k1ra.flight_data_recorder_server.viewmodel.housekeeping

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import net.k1ra.flight_data_recorder_server.model.dao.authentication.SessionsDao
import net.k1ra.flight_data_recorder_server.model.dao.housekeeping.LiveSessionDao
import net.k1ra.flight_data_recorder_server.model.dao.housekeeping.ServerInstanceDao
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock

object ServerInstanceManagementViewModel {
    var instanceDao: ServerInstanceDao? = null

    fun initInstance() = transaction {
        instanceDao = ServerInstanceDao.new {
            lastUpdate = Clock.systemUTC().instant().toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        }
    }

    fun heartbeat() {
        transaction { instanceDao!!.lastUpdate = Clock.systemUTC().instant().toKotlinInstant().toLocalDateTime(TimeZone.UTC) }

        //Delete old instances and their LiveSessions
        val instances = transaction { ServerInstanceDao.find {
            ServerInstanceDao.ServerInstanceTable.lastUpdate less
                    Clock.systemUTC().instant().minusSeconds(20).toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        } }

        transaction {
            instances.forEach { inst ->
                inst.sessions.forEach { sess -> sess.delete() }
                inst.delete()
            }
        }

        //Delete inactive LiveSessions
        val inactiveSessions = transaction { LiveSessionDao.find {
            LiveSessionDao.LiveSessionTable.lastUpdated less
                    Clock.systemUTC().instant().minusSeconds(20).toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        } }

        val sessionsToDelete = mutableListOf<SessionsDao>()

        transaction {
            inactiveSessions.forEach {
                it.session?.let { it1 -> sessionsToDelete.add(it1) }

                it.delete()
                it.flush()
            }
        }

        transaction {
            sessionsToDelete.forEach {
                it.delete()
                it.flush()
            }
        }
    }
}