package net.k1ra.flight_data_recorder.model.projects

import kotlinx.serialization.Serializable
import net.k1ra.flight_data_recorder.model.authentication.SimpleUserData

@Serializable
data class ProjectData(
    val currentSessions: List<IpGeolocationData>,
    val queriedLogLines: List<LogLine>,
    val name: String,
    val projectId: String,
    val permissionLevel: UserPermissionLevel,
    val owner: SimpleUserData,
    val shares: List<ShareData>
)