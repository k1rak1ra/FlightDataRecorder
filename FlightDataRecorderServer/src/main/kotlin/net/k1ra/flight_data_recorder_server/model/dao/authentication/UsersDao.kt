package net.k1ra.flight_data_recorder_server.model.dao.authentication

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
        val picture = text("picture")
        val email = text("email")
        val username = text("username")
        val password = text("password")
        val uid = char("uid", 36)
        val admin = bool("admin")
        val native = bool("native")

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
                        admin = true
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
    var admin by UsersTable.admin
    var native by UsersTable.native
}