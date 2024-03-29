package net.k1ra.flight_data_recorder_server.model.dao.projects

import net.k1ra.flight_data_recorder_server.model.dao.authentication.UsersDao
import net.k1ra.flight_data_recorder_server.model.dao.logging.LogsDao
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction

class ProjectsDao(id: EntityID<Int>) : Entity<Int>(id) {
    object ProjectsTable : IntIdTable() {
        val owner = reference("owner", UsersDao.UsersTable)
        val appKey = char("appKey", 36)
        val name = text("name")

        fun initDb() {
            transaction {
                if (!ProjectsTable.exists())
                    SchemaUtils.create(ProjectsTable)
            }
        }
    }

    companion object : EntityClass<Int, ProjectsDao>(ProjectsTable)

    var owner by UsersDao referencedOn ProjectsTable.owner
    var appKey by ProjectsTable.appKey
    var name by ProjectsTable.name

    //Objects belonging to this project
    val shares by SharePermissionsDao referrersOn SharePermissionsDao.SharePermissionsTable.project
    val logs by LogsDao referrersOn LogsDao.LogsTable.project
}