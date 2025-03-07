package net.k1ra.flight_data_recorder_server.model.dao.projects

import net.k1ra.flight_data_recorder.model.projects.ShareData
import net.k1ra.flight_data_recorder.model.projects.UserPermissionLevel
import net.k1ra.flight_data_recorder_server.model.dao.authentication.UsersDao
import net.k1ra.flight_data_recorder_server.model.dao.authentication.toSimpleUserData
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
        val permissionLevel = enumeration<UserPermissionLevel>("permissionLevel")

        fun initDb() {
            transaction {
                if (!SharePermissionsTable.exists())
                    SchemaUtils.create(SharePermissionsTable)
            }
        }
    }

    companion object : EntityClass<Int, SharePermissionsDao>(SharePermissionsTable)

    var user by UsersDao referencedOn SharePermissionsTable.user
    var project by ProjectsDao referencedOn SharePermissionsTable.project
    var permissionLevel by SharePermissionsTable.permissionLevel
}

fun SharePermissionsDao.toShareData() : ShareData = transaction {
    return@transaction ShareData(
        user.toSimpleUserData(),
        permissionLevel
    )
}