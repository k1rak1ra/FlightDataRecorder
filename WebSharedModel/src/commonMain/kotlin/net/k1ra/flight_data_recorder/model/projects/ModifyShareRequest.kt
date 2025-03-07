package net.k1ra.flight_data_recorder.model.projects

import kotlinx.serialization.Serializable

@Serializable
data class ModifyShareRequest(
    val uid: String,
    val level: UserPermissionLevel
)