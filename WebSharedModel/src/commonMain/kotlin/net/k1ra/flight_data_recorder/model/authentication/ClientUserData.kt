package net.k1ra.flight_data_recorder.model.authentication

import kotlinx.serialization.Serializable

@Serializable
data class ClientUserData(
    val name: String, //User's display name
    val profilePicture: String?, //Url of user's profile picture
    val email: String, //User's email
    val username: String, //User's username
    val uid: String, //User ID
    val sessionToken: String, //Session token
    val native: Boolean, //Does account come from built-in account system or an external source like LDAP
    val role: UserRole
)