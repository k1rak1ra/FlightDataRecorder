package net.k1ra.flight_data_recorder_server.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel

object AdminAuthenticationPlugin {
    val adminAuthenticationPlugin = createRouteScopedPlugin("AdminAuthenticationPlugin") {
        onCall { call ->
            if (UserViewModel.verifyAdmin(call.request.headers, call.request.origin.remoteAddress))
                return@onCall

            //Otherwise, return error
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}