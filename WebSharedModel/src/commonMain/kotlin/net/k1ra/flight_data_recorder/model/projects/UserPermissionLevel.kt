package net.k1ra.flight_data_recorder.model.projects

import kotlinx.serialization.Serializable

@Serializable
enum class UserPermissionLevel {
    OWNER, WRITE, READONLY
}