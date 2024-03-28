package net.k1ra.flight_data_recorder.feature.deviceinfo

import platform.UIKit.UIDevice

actual object DeviceInfoGetter {
    actual fun getOsType(): String {
        return "iOS"
    }

    actual fun getOsVersion(): String {
        return UIDevice.currentDevice.systemVersion
    }

    actual fun getNumCpuCores(): Int {
        TODO("Not yet implemented")
    }

    actual fun getAvailableMemoryMb(): Int {
        TODO("Not yet implemented")
    }

    actual fun getSystemProductName(): String {
        TODO("Not yet implemented")
    }

}