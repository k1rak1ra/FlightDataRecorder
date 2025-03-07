package net.k1ra.flight_data_recorder_server.viewmodel.authentication

import net.k1ra.flight_data_recorder.model.authentication.ClientUserData
import net.k1ra.flight_data_recorder.model.authentication.LoginRequest
import net.k1ra.flight_data_recorder_server.model.dao.authentication.SessionsDao
import net.k1ra.flight_data_recorder_server.model.dao.authentication.UsersDao
import org.jetbrains.exposed.sql.or
import java.util.UUID
import io.ktor.http.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import net.k1ra.flight_data_recorder.model.authentication.SimpleUserData
import net.k1ra.flight_data_recorder.model.authentication.UserRole
import net.k1ra.flight_data_recorder_server.model.dao.authentication.toClientUserData
import net.k1ra.flight_data_recorder_server.model.dao.authentication.toSimpleUserData
import net.k1ra.flight_data_recorder_server.model.dao.housekeeping.LiveSessionDao
import net.k1ra.flight_data_recorder_server.viewmodel.housekeeping.ServerInstanceManagementViewModel
import net.k1ra.flight_data_recorder_server.viewmodel.projects.IpGeolocationViewModel
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock

object UserViewModel {
    fun login(request: LoginRequest) : ClientUserData? = transaction {
        var user: ClientUserData? = null

        val userFromDb = UsersDao
            .find{ (UsersDao.UsersTable.username eq request.emailOrUsername) or (UsersDao.UsersTable.email eq request.emailOrUsername) }
            .limit(1)
            .firstOrNull()

        if (userFromDb != null && PasswordViewModel.verify(request.password, userFromDb.password))
            user = createSession(userFromDb).toClientUserData()

        return@transaction user
    }

    fun createSession(sessUser: UsersDao) : SessionsDao = transaction {
        var newToken = UUID.randomUUID().toString()

        while (!SessionsDao.find { SessionsDao.SessionsTable.token eq newToken }.empty())
            newToken = UUID.randomUUID().toString()

        val session = SessionsDao.new {
            user = sessUser
            token = newToken
            lastLogin = Clock.systemUTC().instant().toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        }

        return@transaction session
    }

    fun verifySession(headers: Headers, ipStr: String) : Boolean = transaction  {
        val authInfo = headers["Authorization"]

        if (authInfo != null && authInfo.startsWith("Bearer ")) {
            val headerSplit = authInfo.replace("Bearer ","").split(":")

            //Segment 0: UID
            //Segment 1: Token
            val sessionDao = SessionsDao.find { SessionsDao.SessionsTable.token eq headerSplit[1] }.limit(1).firstOrNull()
            val user = sessionDao?.user

            return@transaction if (user?.uid == headerSplit[0]) {
                sessionDao.lastLogin = Clock.systemUTC().instant().toKotlinInstant().toLocalDateTime(
                    TimeZone.UTC)

                if (sessionDao.liveSessionData.empty() ) {
                    val ipGeoData = IpGeolocationViewModel.get(ipStr)

                    LiveSessionDao.new {
                        instance = ServerInstanceManagementViewModel.instanceDao!!
                        session = sessionDao
                        ipAddr = ipStr
                        ipCity = ipGeoData.city
                        ipState = ipGeoData.state
                        ipCountry = ipGeoData.country
                        ipLatitude = ipGeoData.latitude
                        ipLongitude = ipGeoData.longitude
                        lastUpdated = Clock.systemUTC().instant().toKotlinInstant().toLocalDateTime(TimeZone.UTC)
                    }.flush()
                } else {
                    sessionDao.liveSessionData.first().lastUpdated = Clock.systemUTC().instant().toKotlinInstant().toLocalDateTime(
                        TimeZone.UTC)
                }

                true
            } else {
                false
            }
        }

        return@transaction false
    }

    fun verifyAdmin(headers: Headers, ipStr: String) : Boolean = transaction  {
        val authInfo = headers["Authorization"]

        if (authInfo != null && authInfo.startsWith("Bearer ")) {
            val headerSplit = authInfo.replace("Bearer ","").split(":")

            //Segment 0: UID
            //Segment 1: Token
            val sessionDao = SessionsDao.find { SessionsDao.SessionsTable.token eq headerSplit[1] }.limit(1).firstOrNull()
            val user = sessionDao?.user

            return@transaction if (user?.uid == headerSplit[0] && user.role == UserRole.ADMIN) {
                sessionDao.lastLogin = Clock.systemUTC().instant().toKotlinInstant().toLocalDateTime(
                    TimeZone.UTC)

                if (sessionDao.liveSessionData.empty() ) {
                    val ipGeoData = IpGeolocationViewModel.get(ipStr)

                    LiveSessionDao.new {
                        instance = ServerInstanceManagementViewModel.instanceDao!!
                        session = sessionDao
                        ipAddr = ipStr
                        ipCity = ipGeoData.city
                        ipState = ipGeoData.state
                        ipCountry = ipGeoData.country
                        ipLatitude = ipGeoData.latitude
                        ipLongitude = ipGeoData.longitude
                        lastUpdated = Clock.systemUTC().instant().toKotlinInstant().toLocalDateTime(TimeZone.UTC)
                    }.flush()
                } else {
                    sessionDao.liveSessionData.first().lastUpdated = Clock.systemUTC().instant().toKotlinInstant().toLocalDateTime(
                        TimeZone.UTC)
                }

                true
            } else {
                false
            }
        }

        return@transaction false
    }

    fun processLdapUser(ldapUid: String, isAdmin: Boolean, ldapUsername: String, attributes: Map<String, String>) : ClientUserData = transaction {
        val userDao = fetchByUidIfExists(ldapUid) ?: UsersDao.new {
            name = attributes["displayName"]!!
            picture = null
            email = attributes["mail"] ?: ldapUsername
            username = ldapUsername
            password = ""
            uid = ldapUid
            role = if (isAdmin) { UserRole.ADMIN } else { UserRole.USER }
            native = false
        }

        return@transaction createSession(userDao).toClientUserData()
    }

    fun userFromHeader(headers: Headers) : UsersDao = transaction {
        val authInfo = headers["Authorization"]
        val headerSplit = authInfo!!.replace("Bearer ","").split(":")
        return@transaction UsersDao.find { UsersDao.UsersTable.uid eq headerSplit[0] }.first()
    }

    fun sessionFromHeader(headers: Headers) : SessionsDao = transaction {
        val authInfo = headers["Authorization"]
        val headerSplit = authInfo!!.replace("Bearer ","").split(":")
        return@transaction SessionsDao.find { SessionsDao.SessionsTable.token eq headerSplit[1] }.limit(1).first()
    }

    fun verifyEmailIsUnique(email: String) : Boolean = transaction {
        return@transaction UsersDao.find { UsersDao.UsersTable.email eq email }.empty()
    }

    fun verifyUsernameIsUnique(username: String) : Boolean = transaction {
        return@transaction UsersDao.find { UsersDao.UsersTable.username eq username }.empty()
    }

    fun fetchByUidIfExists(uid: String) : UsersDao? = transaction {
        return@transaction UsersDao.find { UsersDao.UsersTable.uid eq uid }.limit(1).firstOrNull()
    }

    fun logout(headers: Headers) = transaction {
        val session = sessionFromHeader(headers)

        session.liveSessionData.forEach {
            transaction {
                it.delete()
                it.flush()
            }
        }

        session.delete()
    }

    fun getAllUsers(headers: Headers) : List<SimpleUserData> = transaction {
        val user = userFromHeader(headers)

        return@transaction UsersDao.all().filter { it.id.value != user.id.value }.map { it.toSimpleUserData() }
    }
}