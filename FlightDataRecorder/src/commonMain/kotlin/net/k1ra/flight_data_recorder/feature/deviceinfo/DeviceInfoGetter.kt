package net.k1ra.flight_data_recorder.feature.deviceinfo

expect object DeviceInfoGetter {

    fun getOsType() : String

    fun getOsVersion() : String

    fun getNumCpuCores() : Int

    fun getAvailableMemoryMb() : Int

    fun getSystemProductName() : String
}