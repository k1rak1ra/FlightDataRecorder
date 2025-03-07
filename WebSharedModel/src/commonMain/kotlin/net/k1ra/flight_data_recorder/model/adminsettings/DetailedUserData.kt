package net.k1ra.flight_data_recorder.model.adminsettings

import kotlinx.serialization.Serializable
import net.k1ra.flight_data_recorder.model.authentication.UserRole

@Serializable
data class DetailedUserData(
    val name: String, //User's display name
    val profilePicture: String?, //Url of user's profile picture
    val email: String, //User's email
    val username: String, //User's username
    val uid: String, //User ID
    val native: Boolean, //Does account come from built-in account system or an external source like LDAP
    val role: UserRole
)