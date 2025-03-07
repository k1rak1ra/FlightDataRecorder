package net.k1ra.flight_data_recorder.model.projects

import kotlinx.serialization.Serializable

@Serializable
data class ProjectUpdateRequest(
    val updateOwner: String? = null,
    val deleteShare: String? = null,
    val addShare: String? = null,
    val editShare: ModifyShareRequest? = null,
    val query: String
)