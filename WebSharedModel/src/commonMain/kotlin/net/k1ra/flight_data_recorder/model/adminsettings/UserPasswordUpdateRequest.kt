package net.k1ra.flight_data_recorder.model.adminsettings

import kotlinx.serialization.Serializable

@Serializable
data class UserPasswordUpdateRequest(
    val password: String
)