package net.k1ra.flight_data_recorder_server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import net.k1ra.flight_data_recorder.model.authentication.UserData
import net.k1ra.flight_data_recorder.model.authentication.LoginRequest
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel

fun Route.login() {
    route("/webapi/login") {
        post {
            val request: LoginRequest = call.receive()

            var user: UserData? = null

            //Try LDAP login first TODO implement LDAP at some point
            //if (ServerSetting.getLdapLoginEnabled())
                //user = Ldap().doLogin(request)

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