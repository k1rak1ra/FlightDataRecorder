package net.k1ra.flight_data_recorder_server.routes

import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import net.k1ra.flight_data_recorder_server.plugins.AuthenticationPlugin
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel

fun Route.dashboard() {
    route("/webapi/dashboard") {
        install(AuthenticationPlugin.authenticationPlugin)
        get {
           call.respondText("Authenticated successfully as ${UserViewModel.uidFromHeader(call.request.headers)}")
        }
    }
}