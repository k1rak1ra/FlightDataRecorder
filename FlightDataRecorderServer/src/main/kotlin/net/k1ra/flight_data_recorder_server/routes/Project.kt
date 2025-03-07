package net.k1ra.flight_data_recorder_server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import net.k1ra.flight_data_recorder.model.projects.LogSearchQuery
import net.k1ra.flight_data_recorder.model.projects.ProjectCreationRequest
import net.k1ra.flight_data_recorder.model.projects.ProjectUpdateRequest
import net.k1ra.flight_data_recorder_server.plugins.AuthenticationPlugin
import net.k1ra.flight_data_recorder_server.viewmodel.projects.ProjectsViewModel

fun Route.project() {
    rateLimit(RateLimitName("regular")) {
        route("/webapi/project") {
            install(AuthenticationPlugin.authenticationPlugin)
            post {
                val request: ProjectCreationRequest = call.receive()

                try {
                    ProjectsViewModel.createProject(request.name, call.request.headers)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Forbidden, e.message!!)
                }
            }
        }

        route("/webapi/project/{projectId}") {
            install(AuthenticationPlugin.authenticationPlugin)
            post {
                val query: LogSearchQuery = call.receive()

                try {
                    val data = ProjectsViewModel.getProjectData(call.parameters["projectId"]!!, call.request.headers, query.query)

                    if (data != null)
                        call.respond(data)
                    else
                        call.respond(HttpStatusCode.NotFound)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.Forbidden, e.message!!)
                }
            }
            patch {
                val request: ProjectUpdateRequest = call.receive()

                if (ProjectsViewModel.doesProjectExit(call.parameters["projectId"]!!, call.request.headers)) {
                    try {
                        if (request.updateOwner != null) {
                            ProjectsViewModel.updateOwner(request.updateOwner!!, call.request.headers, call.parameters["projectId"]!!)
                        } else if (request.deleteShare != null) {
                            ProjectsViewModel.deleteShare(request.deleteShare!!, call.request.headers, call.parameters["projectId"]!!)
                        } else if (request.addShare != null) {
                            ProjectsViewModel.addShare(request.addShare!!, call.request.headers, call.parameters["projectId"]!!)
                        } else if (request.editShare != null) {
                            ProjectsViewModel.editShare(request.editShare!!, call.request.headers, call.parameters["projectId"]!!)
                        }

                        val data = ProjectsViewModel.getProjectData(call.parameters["projectId"]!!, call.request.headers, request.query)!!
                        call.respond(data)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.Forbidden, e.message!!)
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}