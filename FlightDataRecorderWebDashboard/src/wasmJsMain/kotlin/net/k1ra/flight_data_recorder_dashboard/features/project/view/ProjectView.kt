package net.k1ra.flight_data_recorder_dashboard.features.project.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.k1ra.flight_data_recorder_dashboard.features.base.model.CustomSnackbarState
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.BackButtonAppBar
import net.k1ra.flight_data_recorder_dashboard.features.base.view.CustomSnackbarHost
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultErrorCentered
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoadingCentered
import net.k1ra.flight_data_recorder_dashboard.features.project.model.BottomBarItem
import net.k1ra.flight_data_recorder_dashboard.features.project.viewmodel.ProjectViewModel
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProjectView(navController: NavController, projectId: String) {
    val viewModel = koinViewModel<ProjectViewModel>(parameters = { parametersOf(projectId) })
    val state = remember { viewModel.state }
    val selectedItem = remember { viewModel.selectedItem }
    val snackbarState = CustomSnackbarState.init()

    val bottomBarItems = listOf(
        BottomBarItem(stringResource(Res.string.dashboard), Icons.Filled.Home),
        BottomBarItem(stringResource(Res.string.log_query), Icons.Filled.Search),
        BottomBarItem(stringResource(Res.string.settings), Icons.Filled.Settings),
    )

    Scaffold(
        snackbarHost = { CustomSnackbarHost(snackbarState) },
        topBar = { BackButtonAppBar(navController, if (state.value is Success) {(state.value as Success).value.name} else {""}) },
        bottomBar = {
            NavigationBar {
                bottomBarItems.forEachIndexed { index, bottomBarItem ->
                    NavigationBarItem(
                        selected = selectedItem.value == index,
                        onClick = { selectedItem.value = index },
                        icon = { Icon(bottomBarItem.icon, bottomBarItem.name) },
                        label = { Text(bottomBarItem.name) }
                    )
                }
            }
        }
    ) { padding ->
        Column {
            when (val s = state.value) {
                is Success -> {
                    Column(
                        Modifier.padding(padding).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        when (selectedItem.value) {
                            1 -> ProjectLogQueryView(viewModel, s.value)
                            2 -> ProjectSettingsView(viewModel, s.value)
                            else -> ProjectDashboardView(viewModel, s.value)
                        }
                    }
                }
                is Failure -> {
                    if (s.reason.code == 403) {
                        viewModel.query.value = viewModel.defaultQuery

                        when (s.reason.message) {
                            "insufficientPermissions" -> DefaultErrorCentered(stringResource(Res.string.insufficientPermissions)) { viewModel.getData() }
                            "syntaxSelect" -> DefaultErrorCentered(stringResource(Res.string.syntaxSelect)) { viewModel.getData() }
                            "syntaxNoFrom" -> DefaultErrorCentered(stringResource(Res.string.syntaxNoFrom)) { viewModel.getData() }
                            "syntaxFromAfterWhere" -> DefaultErrorCentered(stringResource(Res.string.syntaxFromAfterWhere)) { viewModel.getData() }
                            "syntaxWhereMissing" -> DefaultErrorCentered(stringResource(Res.string.syntaxWhereMissing)) { viewModel.getData() }
                            "syntaxTreeBranchZero" -> DefaultErrorCentered(stringResource(Res.string.syntaxTreeBranchZero)) { viewModel.getData() }
                            "syntaxTreeUnexpectedType" -> DefaultErrorCentered(stringResource(Res.string.syntaxTreeUnexpectedType)) { viewModel.getData() }
                            "syntaxInvalidOp" -> DefaultErrorCentered(stringResource(Res.string.syntaxInvalidOp)) { viewModel.getData() }
                            "syntaxBracketsError" -> DefaultErrorCentered(stringResource(Res.string.syntaxBracketsError)) { viewModel.getData() }
                            "syntaxInvalidCompareForString" -> DefaultErrorCentered(stringResource(Res.string.syntaxInvalidCompareForString)) { viewModel.getData() }
                            "syntaxInvalidCompareForBool" -> DefaultErrorCentered(stringResource(Res.string.syntaxInvalidCompareForBool)) { viewModel.getData() }
                            "syntaxInvalidCompareForNumber" -> DefaultErrorCentered(stringResource(Res.string.syntaxInvalidCompareForNumber)) { viewModel.getData() }
                            else -> DefaultErrorCentered(stringResource(Res.string.syntaxGeneric)) { viewModel.getData() }
                        }
                    } else {
                        DefaultErrorCentered(stringResource(Res.string.connect_fail)) { viewModel.getData() }
                    }
                }
                is Loading -> DefaultLoadingCentered(s.customText)
                is Initial -> viewModel.getData()
            }
        }
    }
}