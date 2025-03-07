package net.k1ra.flight_data_recorder_server.routes

import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import net.k1ra.flight_data_recorder_server.plugins.AuthenticationPlugin
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel

fun Route.all_users() {
    rateLimit(RateLimitName("regular")) {
        route("/webapi/all_users") {
            install(AuthenticationPlugin.authenticationPlugin)
            get {
                call.respond(UserViewModel.getAllUsers(call.request.headers))
            }
        }
    }
}