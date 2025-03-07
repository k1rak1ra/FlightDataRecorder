package net.k1ra.flight_data_recorder.model.authentication

import kotlinx.serialization.Serializable

@Serializable
data class SimpleUserData(
    val uid: String,
    val name: String,
    val profilePicture: String?,
)