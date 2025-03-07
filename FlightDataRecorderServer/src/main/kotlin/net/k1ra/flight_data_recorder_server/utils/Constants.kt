package net.k1ra.flight_data_recorder_server.utils

object Constants {
    val MAXMIND_API_KEY: String = System.getenv("MAXMIND_API_KEY")
    val MAXMIND_ACCOUNT_ID = System.getenv("MAXMIND_ACCOUNT_ID").toInt()

    const val MAX_LOG_LINES = 1000
    const val PROFILE_PICTURE_MAX_SIZE = 6000000

    val S3_BUCKET_NAME: String = System.getenv("FDR_S3_BUCKET_NAME")
    val S3_REGION: String = System.getenv("FDR_S3_REGION")

    val LDAP_SERVER: String? = System.getenv("LDAP_SERVER")
    val LDAP_TLS_ENABLED = System.getenv("LDAP_TLS_ENABLED") == "true"
    val LDAP_BIND_USER: String? = System.getenv("LDAP_BIND_USER")
    val LDAP_BIND_PASSWORD: String? = System.getenv("LDAP_BIND_PASSWORD")
    val LDAP_ADMIN_GROUP_NAME: String? = System.getenv("LDAP_ADMIN_GROUP_NAME")
    val LDAP_GROUP_DN: String? = System.getenv("LDAP_GROUP_DN")
    val LDAP_GROUP_FILTER: String? = System.getenv("LDAP_GROUP_FILTER")
    val LDAP_USER_DN: String? = System.getenv("LDAP_USER_DN")
    val LDAP_USER_FILTER: String? = System.getenv("LDAP_USER_FILTER")
    val LDAP_USER_UID_ATTRIBUTE: String? = System.getenv("LDAP_USER_UID_ATTRIBUTE")
}