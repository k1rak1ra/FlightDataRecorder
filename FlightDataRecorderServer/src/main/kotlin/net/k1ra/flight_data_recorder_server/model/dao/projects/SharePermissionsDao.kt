package net.k1ra.flight_data_recorder_server.model.dao.projects

import net.k1ra.flight_data_recorder_server.model.dao.authentication.UsersDao
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction

class SharePermissionsDao(id: EntityID<Int>) : Entity<Int>(id) {
    object SharePermissionsTable : IntIdTable() {
        val user = reference("user", UsersDao.UsersTable)
        val project = reference("project", ProjectsDao.ProjectsTable)
        val permissionLevel = enumeration("permissionLevel", PermissionLevel::class)

        fun initDb() {
            transaction {
                if (!SharePermissionsTable.exists())
                    SchemaUtils.create(SharePermissionsTable)
            }
        }
    }

    enum class PermissionLevel {
        WRITE, READONLY
    }

    companion object : EntityClass<Int, SharePermissionsDao>(SharePermissionsTable)

    var user by UsersDao referencedOn SharePermissionsTable.user
    var project by ProjectsDao referencedOn SharePermissionsTable.project
    var permissionLevel by SharePermissionsTable.permissionLevel
}