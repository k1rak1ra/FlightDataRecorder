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

fun Route.batchUpload() {
    route("/client/batchupload") {
        post {
            val appKey = call.request.header("Authorization")?.replace("Bearer ","") ?: ""
            val data = Json.parseToJsonElement(call.receiveText()) as JsonArray

            println("APP KEY $appKey")
            println(data.toString())

           call.respond(HttpStatusCode.OK)
        }
    }
}