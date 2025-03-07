package net.k1ra.flight_data_recorder.model.projects

import kotlinx.serialization.Serializable
import net.k1ra.flight_data_recorder.model.authentication.SimpleUserData

@Serializable
data class SimpleProjectData(
    val name: String,
    val projectId: String,
    val owner: SimpleUserData
)