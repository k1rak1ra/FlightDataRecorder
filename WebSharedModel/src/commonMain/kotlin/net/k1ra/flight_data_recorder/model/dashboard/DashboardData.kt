package net.k1ra.flight_data_recorder.model.dashboard

import kotlinx.serialization.Serializable
import net.k1ra.flight_data_recorder.model.authentication.ClientUserData
import net.k1ra.flight_data_recorder.model.projects.SimpleProjectData

@Serializable
data class DashboardData(
    val userData: ClientUserData,
    val projects: List<SimpleProjectData>
)