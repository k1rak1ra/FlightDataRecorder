package net.k1ra.flight_data_recorder_server.viewmodel.aws

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.Delete
import aws.sdk.kotlin.services.s3.model.DeleteObjectsRequest
import aws.sdk.kotlin.services.s3.model.ObjectIdentifier
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import net.k1ra.flight_data_recorder_server.utils.Constants

object S3ViewModel {
    suspend fun uploadToBucket(data: ByteArray, newTag: String) : String? {
        val request = PutObjectRequest {
            bucket = Constants.S3_BUCKET_NAME
            this.body = ByteStream.fromBytes(data)
            key = newTag
        }

        S3Client{ region = Constants.S3_REGION }.use { s3 ->
            val response = s3.putObject(request)

            return response.eTag
        }
    }

    suspend fun deleteFromBucket(etag: String) {
        val objectId = ObjectIdentifier { key = etag }

        val delOb = Delete { objects = listOf(objectId) }

        val request = DeleteObjectsRequest {
            bucket = Constants.S3_BUCKET_NAME
            delete = delOb
        }

        S3Client { region = Constants.S3_REGION }.use { s3 ->
            s3.deleteObjects(request)
        }
    }
}