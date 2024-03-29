package net.k1ra.flight_data_recorder_server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import net.k1ra.flight_data_recorder_server.viewmodel.logging.LogsViewModel
import net.k1ra.flight_data_recorder_server.viewmodel.projects.ProjectsViewModel

fun Route.batchUpload() {
    route("/client/batchupload") {
        post {
            val project = ProjectsViewModel.getProject(call.request.header("Authorization")?.replace("Bearer ","") ?: "")

            if (project != null) {
                LogsViewModel.insertBatch(project, Json.parseToJsonElement(call.receiveText()) as JsonArray)

                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}