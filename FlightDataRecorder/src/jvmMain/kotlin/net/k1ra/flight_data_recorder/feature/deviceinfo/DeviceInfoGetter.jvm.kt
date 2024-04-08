package net.k1ra.flight_data_recorder.feature.deviceinfo

import oshi.SystemInfo


internal actual object DeviceInfoGetter {
    private val si = SystemInfo()

    actual fun getOsType(): String {
        return si.operatingSystem.family
    }

    actual fun getOsVersion(): String {
        return si.operatingSystem.versionInfo.version
    }

    actual fun getNumCpuCores(): Int {
        return si.hardware.processor.physicalProcessorCount
    }

    actual fun getAvailableMemoryMb(): Int {
        return (si.hardware.memory.total / 1048576).toInt()
    }

    actual fun getSystemProductName(): String {
        return si.hardware.computerSystem.model
    }

}