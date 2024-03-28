package net.k1ra.flight_data_recorder.feature.deviceinfo

import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice

actual object DeviceInfoGetter {
    actual fun getOsType(): String {
        return "iOS"
    }

    actual fun getOsVersion(): String {
        return UIDevice.currentDevice.systemVersion
    }

    actual fun getNumCpuCores(): Int {
        return NSProcessInfo.processInfo.activeProcessorCount.toInt()
    }

    actual fun getAvailableMemoryMb(): Int {
        return (NSProcessInfo.processInfo.physicalMemory / 1048576u).toInt()
    }

    actual fun getSystemProductName(): String {
        return UIDevice.currentDevice.name
    }

}