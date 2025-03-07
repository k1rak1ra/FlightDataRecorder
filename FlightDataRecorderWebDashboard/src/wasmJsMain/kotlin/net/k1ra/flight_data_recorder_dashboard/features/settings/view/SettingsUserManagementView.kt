package net.k1ra.flight_data_recorder_dashboard.features.settings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import net.k1ra.eizo.EizoImage
import net.k1ra.flight_data_recorder.model.adminsettings.DetailedUserData
import net.k1ra.flight_data_recorder_dashboard.features.base.model.CustomSnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.BackButtonAppBar
import net.k1ra.flight_data_recorder_dashboard.features.base.view.CustomSnackbarHost
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultErrorCentered
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoadingCentered
import net.k1ra.flight_data_recorder_dashboard.features.datatable.view.DataColumn
import net.k1ra.flight_data_recorder_dashboard.features.datatable.view.DataTable
import net.k1ra.flight_data_recorder_dashboard.features.settings.viewmodel.SettingsViewModel
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsUserManagementView(navController: NavController) {
    val snackbarState = CustomSnackbarState.init()
    val viewModel = koinViewModel<SettingsViewModel>()
    val fetchUsersState = remember { viewModel.fetchUsersState }
    val showAddUserDialog = remember { mutableStateOf(false) }
    val showEditUserDialog = remember { mutableStateOf(false) }
    val selectedUser = remember { mutableStateOf(null as DetailedUserData?) }
    val scrollState = rememberScrollState()

    selectedUser.value?.let {
        if (showEditUserDialog.value)
            SettingsEditUserDialog(viewModel, showEditUserDialog, snackbarState, it)
    }

    if (showAddUserDialog.value)
        SettingsAddUserDialog(viewModel, showAddUserDialog, snackbarState)

    Scaffold(
        snackbarHost = { CustomSnackbarHost(snackbarState) },
        topBar = { BackButtonAppBar(navController, stringResource(Res.string.manage_users)) },
        floatingActionButton = { FloatingActionButton(onClick = {
            viewModel.createUserState.value = Initial
            showAddUserDialog.value = true
        }){
            Icon(Icons.Filled.Add, "Add")
        } }
    ) { padding ->
        when (val s = fetchUsersState.value) {
            is Initial -> viewModel.getUsers()
            is Loading -> DefaultLoadingCentered(s.customText)
            is Failure -> DefaultErrorCentered(stringResource(Res.string.connect_fail)) { viewModel.getUsers() }
            is Success -> {
                Column(
                    Modifier.fillMaxSize().verticalScroll(scrollState).padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val keys = listOf(
                        stringResource(Res.string.picture),
                        stringResource(Res.string.name),
                        stringResource(Res.string.username),
                        stringResource(Res.string.email),
                        stringResource(Res.string.role),
                        stringResource(Res.string.uid),
                        stringResource(Res.string.native)
                    )

                    DataTable(
                        columns = keys.map {
                            DataColumn(alignment = Alignment.Center) {
                                Text(it, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp, 0.dp))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        s.value.map {
                            row {
                                onClick = {
                                    viewModel.updateUserState.value = Initial
                                    viewModel.updateUserPasswordState.value = Initial
                                    selectedUser.value = it
                                    showEditUserDialog.value = true
                                }
                                cell {
                                    EizoImage(
                                        url = it.profilePicture ?: "",
                                        fallbackPainter = painterResource(Res.drawable.person),
                                        modifier = Modifier.size(48.dp).clip(CircleShape)
                                    )
                                }
                                cell { Text(it.name) }
                                cell { Text(it.username) }
                                cell { Text(it.email) }
                                cell { Text(it.role.name) }
                                cell { Text(it.uid) }
                                cell { Text(if (it.native) { stringResource(Res.string.yes) } else { stringResource(Res.string.no) }) }
                            }
                        }
                    }
                }
            }
        }
    }
}