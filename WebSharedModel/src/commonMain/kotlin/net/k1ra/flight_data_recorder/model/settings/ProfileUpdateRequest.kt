package net.k1ra.flight_data_recorder.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class ProfileUpdateRequest(
    val newProfilePicture: String?,
    val username: String
)