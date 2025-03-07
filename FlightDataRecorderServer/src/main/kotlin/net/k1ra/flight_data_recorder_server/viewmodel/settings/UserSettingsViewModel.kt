package net.k1ra.flight_data_recorder_server.viewmodel.settings

import io.ktor.http.Headers
import net.k1ra.flight_data_recorder.model.settings.PasswordUpdateRequest
import net.k1ra.flight_data_recorder.model.settings.PersonalInformationUpdateRequest
import net.k1ra.flight_data_recorder.model.settings.ProfileUpdateRequest
import net.k1ra.flight_data_recorder_server.utils.Constants
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.PasswordViewModel
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel
import net.k1ra.flight_data_recorder_server.viewmodel.aws.S3ViewModel
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Base64

class UserSettingsViewModel(headers: Headers) {
    val userDao = UserViewModel.userFromHeader(headers)

    suspend fun updateProfile(profileUpdateRequest: ProfileUpdateRequest) {
        if (profileUpdateRequest.username.isBlank())
            throw Exception("blank")

        if (profileUpdateRequest.newProfilePicture != null) {
            val byteArray = Base64.getDecoder().decode(profileUpdateRequest.newProfilePicture)

            if (byteArray.size > Constants.PROFILE_PICTURE_MAX_SIZE)
                throw Exception("fileSize")

            val profilePicUrl = userDao.picture ?: "https://${Constants.S3_BUCKET_NAME}.s3.${Constants.S3_REGION}.amazonaws.com/${userDao.uid}_0"
            val lastUrlSegment = profilePicUrl.split("/").last()
            val imageIndexNumber = lastUrlSegment.substring(lastUrlSegment.length-1, lastUrlSegment.length).toInt()
            val newImageIndex = imageIndexNumber+1
            val lastUrlSegmentWithoutIndexNumber = lastUrlSegment.substring(0, lastUrlSegment.length-1)

            try {
                S3ViewModel.uploadToBucket(byteArray, "$lastUrlSegmentWithoutIndexNumber$newImageIndex")

                if (imageIndexNumber > 0)
                    S3ViewModel.deleteFromBucket("$lastUrlSegmentWithoutIndexNumber$imageIndexNumber")

                transaction {
                    userDao.picture = "https://${Constants.S3_BUCKET_NAME}.s3.${Constants.S3_REGION}.amazonaws.com/$lastUrlSegmentWithoutIndexNumber$newImageIndex"
                }
            } catch (e: Exception) {
                throw Exception("uploadFail")
            }
        }

        if (profileUpdateRequest.username != userDao.username) {
            if (UserViewModel.verifyUsernameIsUnique(profileUpdateRequest.username))
                transaction {
                    userDao.username = profileUpdateRequest.username
                }
            else
                throw Exception("usernameNotUnique")
        }
    }

    fun updatePersonalInformation(personalInformationUpdateRequest: PersonalInformationUpdateRequest) = transaction {
        if (personalInformationUpdateRequest.email.isBlank() || personalInformationUpdateRequest.name.isBlank())
            throw Exception("blank")

        if (personalInformationUpdateRequest.email != userDao.email) {
            if (UserViewModel.verifyEmailIsUnique(personalInformationUpdateRequest.email))
                userDao.email = personalInformationUpdateRequest.email
            else
                throw Exception("emailNotUnique")
        }

        userDao.name = personalInformationUpdateRequest.name
    }

    fun updatePassword(passwordUpdateRequest: PasswordUpdateRequest) = transaction {
        if (PasswordViewModel.verify(passwordUpdateRequest.oldPassword, userDao.password)) {
            if (passwordUpdateRequest.newPassword.length < 8)
                throw Exception("passwordLength")

            userDao.password = PasswordViewModel.hash(passwordUpdateRequest.newPassword)
        } else {
            throw Exception("oldPassword")
        }
    }
}