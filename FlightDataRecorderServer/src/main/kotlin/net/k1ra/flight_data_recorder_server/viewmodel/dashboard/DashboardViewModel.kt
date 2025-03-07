package net.k1ra.flight_data_recorder_server.viewmodel.dashboard

import io.ktor.http.Headers
import net.k1ra.flight_data_recorder.model.dashboard.DashboardData
import net.k1ra.flight_data_recorder.model.projects.SimpleProjectData
import net.k1ra.flight_data_recorder_server.model.dao.authentication.toClientUserData
import net.k1ra.flight_data_recorder_server.model.dao.projects.toSimpleProjectData
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel
import org.jetbrains.exposed.sql.transactions.transaction

class DashboardViewModel(headers: Headers) {
    private val session = UserViewModel.sessionFromHeader(headers)

    fun getDashboardData() : DashboardData = transaction {
        val projectList = arrayListOf<SimpleProjectData>()

        projectList.addAll(session.user.ownedProjects.map { it.toSimpleProjectData() })
        projectList.addAll(session.user.projectsSharedWithUser.map { it.project.toSimpleProjectData() })

        return@transaction DashboardData(
            session.toClientUserData(),
            projectList
        )
    }
}