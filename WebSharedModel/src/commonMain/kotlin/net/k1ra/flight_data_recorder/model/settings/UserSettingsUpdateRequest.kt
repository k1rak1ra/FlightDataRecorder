package net.k1ra.flight_data_recorder.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class UserSettingsUpdateRequest(
    val profileUpdateRequest: ProfileUpdateRequest? = null,
    val personalInformationUpdateRequest: PersonalInformationUpdateRequest? = null,
    val passwordUpdateRequest: PasswordUpdateRequest? = null
)