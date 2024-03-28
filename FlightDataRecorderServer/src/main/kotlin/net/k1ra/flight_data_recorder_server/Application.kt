package net.k1ra.flight_data_recorder_server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import net.k1ra.flight_data_recorder.feature.logging.Log
import net.k1ra.flight_data_recorder_server.model.dao.authentication.SessionsDao
import net.k1ra.flight_data_recorder_server.model.dao.authentication.UsersDao
import net.k1ra.flight_data_recorder_server.routes.batchUpload
import net.k1ra.flight_data_recorder_server.routes.dashboard
import net.k1ra.flight_data_recorder_server.routes.login
import org.jetbrains.exposed.sql.Database
import java.net.URI

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

    val port = (System.getenv("FDR_PORT") ?: "8091").toInt()
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    routing {
        //Static site
        staticResources("/", "site")

        //Static extra images
        staticResources("/extraimg", "extraimg")

        //Web dashboard API
        login()
        dashboard()

        //Client API
        batchUpload()
    }
}