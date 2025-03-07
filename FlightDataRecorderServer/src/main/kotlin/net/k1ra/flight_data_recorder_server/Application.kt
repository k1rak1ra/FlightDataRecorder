package net.k1ra.flight_data_recorder_server

import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.calllogging.processingTimeMillis
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import net.k1ra.flight_data_recorder_server.model.dao.authentication.SessionsDao
import net.k1ra.flight_data_recorder_server.model.dao.authentication.UsersDao
import net.k1ra.flight_data_recorder_server.model.dao.logging.LogsDao
import net.k1ra.flight_data_recorder_server.model.dao.projects.ProjectsDao
import net.k1ra.flight_data_recorder_server.model.dao.projects.SharePermissionsDao
import net.k1ra.flight_data_recorder_server.routes.batchUpload
import net.k1ra.flight_data_recorder_server.routes.dashboard
import net.k1ra.flight_data_recorder_server.routes.login
import org.jetbrains.exposed.sql.Database
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import net.k1ra.flight_data_recorder_server.model.dao.housekeeping.LiveSessionDao
import net.k1ra.flight_data_recorder_server.model.dao.housekeeping.ServerInstanceDao
import net.k1ra.flight_data_recorder_server.model.dao.projects.IpGeolocationDao
import net.k1ra.flight_data_recorder_server.routes.all_users
import net.k1ra.flight_data_recorder_server.routes.logout
import net.k1ra.flight_data_recorder_server.routes.manage_users
import net.k1ra.flight_data_recorder_server.routes.project
import net.k1ra.flight_data_recorder_server.routes.user_settings
import net.k1ra.flight_data_recorder_server.viewmodel.housekeeping.ServerInstanceManagementViewModel
import org.slf4j.event.Level
import java.net.URI
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

fun main() {
    //Setup database based on environment variables
    val dbUri = URI(System.getenv("FDR_DATABASE_URL"))
    val username: String = dbUri.userInfo.split(":")[0]
    val password: String = dbUri.userInfo.split(":")[1]
    val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path
    Database.connect(dbUrl, "org.postgresql.Driver", user = username, password = password)

    //DB init
    UsersDao.UsersTable.initDb()
    SessionsDao.SessionsTable.initDb()
    ProjectsDao.ProjectsTable.initDb()
    SharePermissionsDao.SharePermissionsTable.initDb()
    LogsDao.LogsTable.initDb()
    IpGeolocationDao.IpGeolocationTable.initDb()
    ServerInstanceDao.ServerInstanceTable.initDb()
    LiveSessionDao.LiveSessionTable.initDb()

    //Set up server instance reporting in DB
    ServerInstanceManagementViewModel.initInstance()

    //Housekeeper thread init
    CoroutineScope(Dispatchers.IO).launch {
        while(true) {
            try {
                ServerInstanceManagementViewModel.heartbeat()

                delay(Duration.ofMinutes(1))
            } catch (e: Exception) {
                e.printStackTrace()
                //Exceptions can be caused by connection issues, don't want this thread to crash!
            }
        }
    }

    val port = (System.getenv("FDR_PORT") ?: "8091").toInt()
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
    install(XForwardedHeaders)

    install(CORS) {
        anyHost()
        anyMethod()

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    install(CallLogging) {
        level = Level.DEBUG

        //This can be abused to globally get the response time for a particular endpoint, or amount of time spent in game
        //To check performance
        filter { call ->
            call.processingTimeMillis()
            true
        }
    }

    install(RateLimit) {
        global {
            rateLimiter(limit = 1000, refillPeriod = 1.seconds)
        }

        register(RateLimitName("protected")) {
            rateLimiter(limit = 10, refillPeriod = 60.seconds)
        }

        register(RateLimitName("regular")) {
            rateLimiter(limit = 20, refillPeriod = 1.seconds)
        }
    }

    routing {
        //Static site
        staticResources("/", "site")

        //Static extra images
        staticResources("/extraimg", "extraimg")

        //Web dashboard API
        login()
        dashboard()
        project()
        user_settings()
        manage_users()
        all_users()
        logout()

        //Client API
        batchUpload()
    }
}