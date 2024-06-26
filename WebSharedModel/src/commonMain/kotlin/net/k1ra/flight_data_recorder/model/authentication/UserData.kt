package net.k1ra.flight_data_recorder.model.authentication

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val name: String, //User's display name
    val picture: String, //Url of user's profile picture
    val email: String, //User's email
    val username: String, //User's username
    val uid: String, //User ID
    val token: String, //Session token
    val admin: Boolean, //Is user an admin or not
    val native: Boolean, //Does account come from built-in account system or an external source like LDAP
)