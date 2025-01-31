package net.k1ra.flight_data_recorder.feature.cryptography

import korlibs.crypto.SecureRandom
import net.k1ra.sharedprefkmm.SharedPreferences
import net.k1ra.sharedprefkmm.util.Constants

internal object KeyManager {
    private var key: ByteArray? = null

    suspend fun getKey(prefs: SharedPreferences) : ByteArray {
        if (key != null)
            return key as ByteArray

        key = prefs.get<ByteArray>("FlightDataRecorderKey")

        if (key == null) {
            key = generateNewKey()
            prefs.set("FlightDataRecorderKey", key)
        }

        return key as ByteArray
    }

    private fun generateNewKey() : ByteArray {
        val byteArray = ByteArray(Constants.AES_256_KEY_LENGTH)
        SecureRandom.nextBytes(byteArray)
        return byteArray
    }
}