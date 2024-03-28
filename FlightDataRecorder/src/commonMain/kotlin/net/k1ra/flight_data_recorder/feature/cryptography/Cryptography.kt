package net.k1ra.flight_data_recorder.feature.cryptography

import korlibs.crypto.AES
import korlibs.crypto.CipherPadding
import korlibs.crypto.SecureRandom
import net.k1ra.sharedprefkmm.SharedPreferences
import net.k1ra.sharedprefkmm.util.Constants

internal class Cryptography(private val prefs: SharedPreferences) {
    suspend fun runAes(input: ByteArray, iv: ByteArray, cipherMode: CipherMode) : ByteArray {
        return when(cipherMode) {
            CipherMode.ENCRYPT -> AES.encryptAesCbc(input, KeyManager.getKey(prefs), iv, CipherPadding.PKCS7Padding)
            CipherMode.DECRYPT -> AES.decryptAesCbc(input, KeyManager.getKey(prefs), iv, CipherPadding.PKCS7Padding)
        }
    }

    fun generateIv() : ByteArray {
        val ivBytes = ByteArray(Constants.AES_256_IV_LENGTH)
        SecureRandom.nextBytes(ivBytes)

        return ivBytes
    }
}