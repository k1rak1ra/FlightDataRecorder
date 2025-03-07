package net.k1ra.flight_data_recorder_server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import net.k1ra.flight_data_recorder_server.plugins.AuthenticationPlugin
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel

fun Route.logout() {
    rateLimit(RateLimitName("regular")) {
        route("/webapi/logout") {
            install(AuthenticationPlugin.authenticationPlugin)
            get {
                UserViewModel.logout(call.request.headers)

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}