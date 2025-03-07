package net.k1ra.flight_data_recorder_dashboard.features.project.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.k1ra.flight_data_recorder.model.projects.ProjectData
import net.k1ra.flight_data_recorder_dashboard.features.project.viewmodel.ProjectViewModel
import net.k1ra.flight_data_recorder_dashboard.utils.titleFontSize
import org.jetbrains.compose.resources.stringResource
import ovh.plrapps.mapcompose.ui.MapUI
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*

@Composable
fun ProjectDashboardView(viewModel: ProjectViewModel, data: ProjectData) {
    Card(Modifier.padding(16.dp)) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(Res.string.users_live, data.currentSessions.size), fontSize = titleFontSize)
        }
    }

    Card(Modifier.padding(16.dp)) {
        MapUI(
            modifier = Modifier.fillMaxSize(),
            state = viewModel.mapState
        )
    }
}