package net.k1ra.flight_data_recorder_server.viewmodel.authentication

import de.mkammerer.argon2.Argon2Factory


object PasswordViewModel {
    private val argon2 = Argon2Factory.create(
        Argon2Factory.Argon2Types.ARGON2id,
        32,
        64
    )

    fun hash(input: String) : String {
        return argon2.hash(10, 65536, 1, input.toCharArray())
    }

    fun verify(input: String, hash: String) : Boolean {
        return argon2.verify(hash, input.toCharArray())
    }
}