package net.k1ra.flight_data_recorder.model.projects

import kotlinx.serialization.Serializable

@Serializable
data class IpGeolocationData(
    val addr: String,
    val city: String,
    val state: String,
    val country: String,
    val latitude: String,
    val longitude: String
)