package net.k1ra.flight_data_recorder_server.viewmodel.adminsettings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.flight_data_recorder.model.adminsettings.DetailedUserData
import net.k1ra.flight_data_recorder.model.adminsettings.UserCreationRequest
import net.k1ra.flight_data_recorder.model.adminsettings.UserPasswordUpdateRequest
import net.k1ra.flight_data_recorder.model.adminsettings.UserUpdateRequest
import net.k1ra.flight_data_recorder.model.authentication.UserRole
import net.k1ra.flight_data_recorder_server.model.dao.authentication.UsersDao
import net.k1ra.flight_data_recorder_server.model.dao.authentication.toDetailedUserData
import net.k1ra.flight_data_recorder_server.utils.Constants
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.PasswordViewModel
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel
import net.k1ra.flight_data_recorder_server.viewmodel.aws.S3ViewModel
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Base64
import java.util.UUID

object UserManagementViewModel {

    fun getUsers() : List<DetailedUserData> = transaction {
        UsersDao.all().map { it.toDetailedUserData() }
    }

    fun createUser(request: UserCreationRequest) = transaction {
        if (!UserViewModel.verifyEmailIsUnique(request.email))
            throw Exception("emailNotUnique")

        if (!UserViewModel.verifyUsernameIsUnique(request.username))
            throw Exception("usernameNotUnique")

        var newUid = UUID.randomUUID().toString()

        //Make sure UID is unique
        while (!UsersDao.find { UsersDao.UsersTable.uid eq newUid }.empty())
            newUid = UUID.randomUUID().toString()

        if (request.profilePicture != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val byteArray = Base64.getDecoder().decode(request.profilePicture)
                S3ViewModel.uploadToBucket(byteArray, "${newUid}_0")
            }
        }

        UsersDao.new {
            name = request.name
            picture = if (request.profilePicture != null) {"https://${Constants.S3_BUCKET_NAME}.s3.${Constants.S3_REGION}.amazonaws.com/${newUid}_0"} else {null}
            email = request.email
            username = request.username
            password = PasswordViewModel.hash(request.password)
            uid = newUid
            role = if (request.isAdmin) {UserRole.ADMIN} else {UserRole.USER}
            native = true
        }
    }

    suspend fun updateUser(request: UserUpdateRequest, uid: String) {
        val userDao = UserViewModel.fetchByUidIfExists(uid) ?: throw Exception("notFound")

        if (request.profilePicture != null) {
            val byteArray = Base64.getDecoder().decode(request.profilePicture)
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

        if (request.username != userDao.username) {
            if (UserViewModel.verifyUsernameIsUnique(request.username))
                transaction {
                    userDao.username = request.username
                }
            else
                throw Exception("usernameNotUnique")
        }

        if (request.email.isBlank() || request.name.isBlank())
            throw Exception("blank")

        if (request.email != userDao.email) {
            if (UserViewModel.verifyEmailIsUnique(request.email))
                transaction {
                    userDao.email = request.email
                }
            else
                throw Exception("emailNotUnique")
        }

        transaction {
            userDao.name = request.name
            userDao.role = if (request.isAdmin) {UserRole.ADMIN} else {UserRole.USER}
        }
    }

    fun updateUserPassword(request: UserPasswordUpdateRequest, uid: String) = transaction {
        val userDao = UserViewModel.fetchByUidIfExists(uid) ?: throw Exception("notFound")
        userDao.password = PasswordViewModel.hash(request.password)
    }
}