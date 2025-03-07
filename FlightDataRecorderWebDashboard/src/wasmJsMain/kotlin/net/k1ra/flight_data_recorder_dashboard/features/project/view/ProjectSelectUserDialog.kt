package net.k1ra.flight_data_recorder_dashboard.features.project.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.k1ra.eizo.EizoImage
import net.k1ra.flight_data_recorder.model.authentication.SimpleUserData
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultErrorCentered
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoadingCentered
import net.k1ra.flight_data_recorder_dashboard.features.project.viewmodel.ProjectViewModel
import net.k1ra.flight_data_recorder_dashboard.utils.secondaryFontSize
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import org.jetbrains.compose.resources.painterResource
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSelectUserDialog(
    viewModel: ProjectViewModel,
    showDialog: MutableState<Boolean>,
    onSelected: (SimpleUserData) -> Unit,
    removeShares: Boolean
) {
    val state = remember { viewModel.getAllUsersState }

    BasicAlertDialog(onDismissRequest = {
        showDialog.value = false
    }) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (val s = state.value) {
                    is Initial -> viewModel.getAllUsers()
                    is Loading -> DefaultLoadingCentered(s.customText)
                    is Failure -> DefaultErrorCentered(stringResource(Res.string.connect_fail)) { viewModel.getAllUsers() }
                    is Success -> {
                        val list = if (removeShares && viewModel.state.value is Success)
                            s.value
                                .filter { !(viewModel.state.value as Success).value.shares.map { it.user.uid }.contains(it.uid) }
                                .filter { (viewModel.state.value as Success).value.owner.uid != it.uid }
                        else
                            s.value

                        if (list.isEmpty()) {
                            Text(stringResource(Res.string.no_eligible_users))
                        } else {
                            list.map {
                                Card(Modifier.padding(8.dp).clickable {
                                    showDialog.value = false
                                    onSelected.invoke(it)
                                }) {
                                    Column(
                                        Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Start)
                                        ) {
                                            EizoImage(
                                                url = it.profilePicture ?: "",
                                                fallbackPainter = painterResource(Res.drawable.person),
                                                modifier = Modifier.size(64.dp).clip(CircleShape)
                                            )

                                            Text(it.name, fontSize = secondaryFontSize)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}