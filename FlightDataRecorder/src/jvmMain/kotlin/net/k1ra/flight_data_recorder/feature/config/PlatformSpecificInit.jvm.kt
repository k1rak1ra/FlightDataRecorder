package net.k1ra.flight_data_recorder.feature.config

actual object PlatformSpecificInit {
    actual fun init() {
        Runtime.getRuntime().addShutdownHook(Thread {
            //TODO immediately upload current batch logger store
        })
    }
}