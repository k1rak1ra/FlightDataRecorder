package net.k1ra.flight_data_recorder_dashboard.features.base.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.person
import net.k1ra.eizo.EizoImage
import net.k1ra.flight_data_recorder_dashboard.di.userDataInstance
import net.k1ra.flight_data_recorder_dashboard.utils.titleFontSize
import org.jetbrains.compose.resources.painterResource

@Composable
fun DashboardTopRow(navController: NavController, title: String) {
    val userData = remember { userDataInstance }

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = titleFontSize)

        EizoImage(
            url = userData.value?.profilePicture ?: "",
            fallbackPainter = painterResource(Res.drawable.person),
            modifier = Modifier.size(60.dp).clip(CircleShape).clickable {
                navController.navigate("settings_home")
            }
        )
    }
}