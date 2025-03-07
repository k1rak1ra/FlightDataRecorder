package net.k1ra.flight_data_recorder_dashboard.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val titleFontSize = 32.sp
val secondaryFontSize = 24.sp
val tertiaryFontSize = 16.sp
val mobileColumnWidth = Modifier.fillMaxWidth().padding(16.dp)
val desktopColumnWidth = Modifier.width(400.dp).padding(16.dp)
val mobileDesktopThresholdDp = 420.dp
val mobileDesktopThresholdDpForSideBySide = 1800.dp

const val SHAREDPREF_COLLECTION_NAME = "FlightDataRecorderWebPanel"
const val USERDATA_KEY = "UserData"