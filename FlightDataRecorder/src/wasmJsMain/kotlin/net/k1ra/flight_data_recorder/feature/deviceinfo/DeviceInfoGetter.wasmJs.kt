package net.k1ra.flight_data_recorder.feature.deviceinfo

import kotlinx.browser.window

actual object DeviceInfoGetter {
    actual fun getOsType(): String {
        return "Web"
    }

    actual fun getOsVersion(): String {
        return window.navigator.platform
    }

    actual fun getNumCpuCores(): Int {
        return window.navigator.hardwareConcurrency.toInt()
    }

    actual fun getAvailableMemoryMb(): Int {
        return 0
    }

    actual fun getSystemProductName(): String {
        return window.navigator.userAgent
    }

}