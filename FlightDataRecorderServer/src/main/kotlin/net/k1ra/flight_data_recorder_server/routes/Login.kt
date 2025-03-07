package net.k1ra.flight_data_recorder_server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import net.k1ra.flight_data_recorder.model.authentication.ClientUserData
import net.k1ra.flight_data_recorder.model.authentication.LoginRequest
import net.k1ra.flight_data_recorder_server.utils.Constants
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.LdapViewModel
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel

fun Route.login() {
    rateLimit(RateLimitName("protected")) {
        route("/webapi/login") {
            post {
                val request: LoginRequest = call.receive()

                var user: ClientUserData? = null

                //Try LDAP login first
                if (!Constants.LDAP_SERVER.isNullOrEmpty())
                    user = LdapViewModel.doLogin(request)

                //LDAP login didn't succeed try native
                if (user == null)
                    user = UserViewModel.login(request)

                if (user != null)
                    call.respond(user)
                else
                    call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}