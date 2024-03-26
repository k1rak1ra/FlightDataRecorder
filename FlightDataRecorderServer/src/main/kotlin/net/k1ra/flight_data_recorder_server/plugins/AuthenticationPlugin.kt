package net.k1ra.flight_data_recorder_server.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.response.respond
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel

object AuthenticationPlugin {
    val authenticationPlugin = createRouteScopedPlugin("AuthenticationPlugin") {
        onCall { call ->
            if (UserViewModel.verifySession(call.request.headers))
                return@onCall

            //Otherwise, return error
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}