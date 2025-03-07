package net.k1ra.flight_data_recorder_server.model.dao.authentication

import net.k1ra.flight_data_recorder.model.adminsettings.DetailedUserData
import net.k1ra.flight_data_recorder.model.authentication.SimpleUserData
import net.k1ra.flight_data_recorder.model.authentication.UserRole
import net.k1ra.flight_data_recorder_server.model.dao.projects.ProjectsDao
import net.k1ra.flight_data_recorder_server.model.dao.projects.SharePermissionsDao
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.PasswordViewModel
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction

class UsersDao(id: EntityID<Int>) : Entity<Int>(id) {
    object UsersTable: IntIdTable() {
        val name = text("name")
        val picture = text("picture").nullable()
        val email = text("email")
        val username = text("username")
        val password = text("password")
        val uid = text("uid")
        val native = bool("native")
        val role = enumeration<UserRole>("role")

        fun initDb() {
            transaction {
                if (!UsersTable.exists()) {
                    SchemaUtils.create(UsersTable)

                    UsersDao.new {
                        name = "Root"
                        picture = "http://localhost:8091/extraimg/one.gif" //Temporary because screw CORS
                        email = "root"
                        username = "root"
                        password = PasswordViewModel.hash("Change Me!")
                        uid = "ROOT"
                        role = UserRole.ADMIN
                        native = true
                    }
                }
            }
        }
    }

    companion object : EntityClass<Int, UsersDao>(UsersTable)

    var name by UsersTable.name
    var picture by UsersTable.picture
    var email by UsersTable.email
    var username by UsersTable.username
    var password by UsersTable.password
    var uid by UsersTable.uid
    var native by UsersTable.native
    var role by UsersTable.role

    //Objects belonging to this user
    val sessions by SessionsDao referrersOn SessionsDao.SessionsTable.user
    val ownedProjects by ProjectsDao referrersOn ProjectsDao.ProjectsTable.owner
    val projectsSharedWithUser by SharePermissionsDao referrersOn SharePermissionsDao.SharePermissionsTable.user
}

fun UsersDao.toSimpleUserData() : SimpleUserData = transaction{
    return@transaction SimpleUserData(
        uid,
        name,
        picture
    )
}

fun UsersDao.toDetailedUserData() : DetailedUserData = transaction {
    return@transaction DetailedUserData(
        name,
        picture,
        email,
        username,
        uid,
        native,
        role
    )
}