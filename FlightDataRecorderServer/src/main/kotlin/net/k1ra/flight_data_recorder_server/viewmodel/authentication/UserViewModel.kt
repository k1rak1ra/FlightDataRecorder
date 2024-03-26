package net.k1ra.flight_data_recorder_server.viewmodel.authentication

import net.k1ra.flight_data_recorder.model.authentication.UserData
import net.k1ra.flight_data_recorder.model.authentication.LoginRequest
import net.k1ra.flight_data_recorder_server.model.dao.authentication.SessionsDao
import net.k1ra.flight_data_recorder_server.model.dao.authentication.UsersDao
import org.jetbrains.exposed.sql.or
import java.util.UUID
import io.ktor.http.*
import org.jetbrains.exposed.sql.transactions.transaction

object UserViewModel {
    fun login(request: LoginRequest) : UserData? = transaction {
        var user: UserData? = null

        val userFromDb = UsersDao
            .find{ (UsersDao.UsersTable.username eq request.emailOrUsername) or (UsersDao.UsersTable.email eq request.emailOrUsername) }
            .limit(1)
            .firstOrNull()

        if (userFromDb != null && PasswordViewModel.verify(request.password, userFromDb.password)) {
            user = UserData(
                name = userFromDb.name,
                picture = userFromDb.picture,
                email = userFromDb.email,
                username = userFromDb.username,
                uid = userFromDb.uid,
                token = createSession(userFromDb),
                admin = userFromDb.admin,
                native = userFromDb.native
            )
        }

        return@transaction user
    }

    fun createSession(user: UsersDao) : String = transaction {
        var newToken = UUID.randomUUID().toString()

        while (!SessionsDao.find { SessionsDao.SessionsTable.token eq newToken }.empty())
            newToken = UUID.randomUUID().toString()

        SessionsDao.new {
            uid = user
            token = newToken
        }

        return@transaction newToken
    }

    fun verifySession(headers: Headers) : Boolean = transaction  {
        val authInfo = headers["Authorization"]

        if (authInfo != null && authInfo.startsWith("Bearer ")) {
            val headerSplit = authInfo.replace("Bearer ","").split(":")

            //Segment 0: UID
            //Segment 1: Token
            return@transaction SessionsDao.find { SessionsDao.SessionsTable.token eq headerSplit[1] }.limit(1).firstOrNull()?.uid?.uid == headerSplit[0]
        }

        return@transaction false
    }

    fun uidFromHeader(headers: Headers) : String {
        val authInfo = headers["Authorization"]
        val headerSplit = authInfo!!.replace("Bearer ","").split(":")
        return headerSplit[0]
    }
}