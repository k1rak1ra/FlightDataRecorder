package net.k1ra.flight_data_recorder.feature.deviceinfo

import android.app.ActivityManager
import android.content.Context
import net.k1ra.sharedprefkmm.SharedPrefKmmInitContentProvider


actual object DeviceInfoGetter {
    actual fun getOsType(): String {
        return "Android"
    }

    actual fun getOsVersion(): String {
        return android.os.Build.VERSION.SDK_INT.toString()
    }

    actual fun getNumCpuCores(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    actual fun getAvailableMemoryMb(): Int {
        val activityManager= SharedPrefKmmInitContentProvider.appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemory = memoryInfo.totalMem

        return (totalMemory / 1048576).toInt()
    }

    actual fun getSystemProductName(): String {
        return android.os.Build.MODEL
    }

}