package net.k1ra.flight_data_recorder.model.adminsettings

import kotlinx.serialization.Serializable

@Serializable
data class UserCreationRequest(
    val profilePicture: String?,
    val username: String,
    val name: String,
    val email: String,
    val password: String,
    val isAdmin: Boolean
)