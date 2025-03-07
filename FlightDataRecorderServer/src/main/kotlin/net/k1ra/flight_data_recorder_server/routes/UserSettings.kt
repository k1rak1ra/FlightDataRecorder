package net.k1ra.flight_data_recorder_server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import net.k1ra.flight_data_recorder.model.settings.UserSettingsUpdateRequest
import net.k1ra.flight_data_recorder_server.plugins.AuthenticationPlugin
import net.k1ra.flight_data_recorder_server.viewmodel.settings.UserSettingsViewModel

fun Route.user_settings() {
    rateLimit(RateLimitName("regular")) {
        route("/webapi/user_settings") {
            install(AuthenticationPlugin.authenticationPlugin)
            post {
                val request: UserSettingsUpdateRequest = call.receive()

                val viewModel = UserSettingsViewModel(call.request.headers)

                try {
                    if (request.profileUpdateRequest != null) {
                        viewModel.updateProfile(request.profileUpdateRequest!!)
                    } else if (request.personalInformationUpdateRequest != null) {
                        viewModel.updatePersonalInformation(request.personalInformationUpdateRequest!!)
                    } else if (request.passwordUpdateRequest != null) {
                        viewModel.updatePassword(request.passwordUpdateRequest!!)
                    }

                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.Forbidden, e.message!!)
                }
            }
        }
    }
}