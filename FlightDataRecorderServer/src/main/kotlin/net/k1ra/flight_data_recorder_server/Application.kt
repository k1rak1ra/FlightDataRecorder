package net.k1ra.flight_data_recorder_server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
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
    ProjectsDao.ProjectsTable.initDb()
    SharePermissionsDao.SharePermissionsTable.initDb()
    LogsDao.LogsTable.initDb()

    /* //Example transaction for fetching log lines via JSON
    transaction {
        val platformIsAndroid = LogsDao.LogsTable.logLine.extract<String>("OSType") eq "Android"

        val testProject = ProjectsDao.find { ProjectsDao.ProjectsTable.appKey eq "TEST-KEY" }.limit(1).first()

        val query = LogsDao.LogsTable.select(LogsDao.LogsTable.columns).where {
            (LogsDao.LogsTable.project eq testProject.id) and (platformIsAndroid)
        }.orderBy(LogsDao.LogsTable.datetime)

        val logRows = LogsDao.wrapRows(query).toList().map { it.logLine }
        println(logRows.size)
    }*/

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