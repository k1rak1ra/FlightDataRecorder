package net.k1ra.flight_data_recorder_server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import net.k1ra.flight_data_recorder.model.adminsettings.UserCreationRequest
import net.k1ra.flight_data_recorder.model.adminsettings.UserPasswordUpdateRequest
import net.k1ra.flight_data_recorder.model.adminsettings.UserUpdateRequest
import net.k1ra.flight_data_recorder_server.plugins.AdminAuthenticationPlugin
import net.k1ra.flight_data_recorder_server.plugins.AuthenticationPlugin
import net.k1ra.flight_data_recorder_server.viewmodel.adminsettings.UserManagementViewModel

fun Route.manage_users() {
    rateLimit(RateLimitName("regular")) {
        route("/webapi/manage_users") {
            install(AdminAuthenticationPlugin.adminAuthenticationPlugin)
            get {
                call.respond(UserManagementViewModel.getUsers())
            }
            post {
                val request: UserCreationRequest = call.receive()

                try {
                    UserManagementViewModel.createUser(request)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Forbidden, e.message!!)
                }
            }
        }

        route("/webapi/manage_users/{uid}") {
            install(AdminAuthenticationPlugin.adminAuthenticationPlugin)
            post {
                val request: UserUpdateRequest = call.receive()

                try {
                    UserManagementViewModel.updateUser(request, call.parameters["uid"]!!)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    if (e.message == "notFound")
                        call.respond(HttpStatusCode.NotFound, e.message!!)
                    else
                        call.respond(HttpStatusCode.Forbidden, e.message!!)
                }
            }
            patch {
                val request: UserPasswordUpdateRequest = call.receive()

                try {
                    UserManagementViewModel.updateUserPassword(request, call.parameters["uid"]!!)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    if (e.message == "notFound")
                        call.respond(HttpStatusCode.NotFound, e.message!!)
                    else
                        call.respond(HttpStatusCode.Forbidden, e.message!!)
                }
            }
        }
    }
}