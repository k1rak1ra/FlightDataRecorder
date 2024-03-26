package net.k1ra.flight_data_recorder.model.authentication

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val emailOrUsername: String,
    val password: String
)