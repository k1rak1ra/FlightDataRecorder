package net.k1ra.flight_data_recorder_dashboard.features.dashboard.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import net.k1ra.eizo.EizoImage
import net.k1ra.flight_data_recorder_dashboard.di.userDataInstance
import net.k1ra.flight_data_recorder_dashboard.features.base.model.CustomSnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.SnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.CustomSnackbarHost
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DashboardTopRow
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultErrorCentered
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoadingCentered
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultTextField
import net.k1ra.flight_data_recorder_dashboard.features.dashboard.viewmodel.DashboardViewModel
import net.k1ra.flight_data_recorder_dashboard.utils.secondaryFontSize
import net.k1ra.flight_data_recorder_dashboard.utils.titleFontSize
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardView(navController: NavController) {
    val viewModel = koinViewModel<DashboardViewModel>()
    val state = remember { viewModel.state }
    val createProjectState = remember { viewModel.createProjectState }
    val snackbarState = CustomSnackbarState.init()
    val scrollState = rememberScrollState()
    val showAddProjectDialog = remember { mutableStateOf(false) }
    val newProjectNameText = remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        snackbarHost = { CustomSnackbarHost(snackbarState) },
        floatingActionButton = { FloatingActionButton(onClick = {
            newProjectNameText.value = ""
            showAddProjectDialog.value = true
        }){
            Icon(Icons.Filled.Add, "Add")
        } }
    ) { padding ->
        Column {
            when (val s = state.value) {
                is Success -> {
                    if (showAddProjectDialog.value) {
                        BasicAlertDialog(onDismissRequest = {
                            showAddProjectDialog.value = false
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
                                    Text(stringResource(Res.string.new_project), fontSize = titleFontSize)

                                    DefaultTextField(newProjectNameText, stringResource(Res.string.project_name), keyboardController)

                                    when(val p = createProjectState.value) {
                                        is Initial -> {
                                            Button(onClick = {
                                                viewModel.createProject(newProjectNameText.value)
                                            }) {
                                                Text(stringResource(Res.string.create))
                                            }
                                        }
                                        is Loading -> DefaultLoading(p.customText)
                                        is Failure -> {
                                            if (p.reason.code == 403)
                                                snackbarState.showSnackbar(Res.string.project_already_exists, SnackbarState.ERROR)
                                            else
                                                snackbarState.showSnackbar(Res.string.connect_fail, SnackbarState.ERROR)

                                            createProjectState.value = Initial
                                        }
                                        is Success -> {
                                            showAddProjectDialog.value = false
                                            snackbarState.showSnackbar(Res.string.project_created, SnackbarState.DEFAULT)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Top),
                    ) {
                        DashboardTopRow(navController, stringResource(Res.string.your_projects))

                        val itemsPerRow = 5

                        for (i in 0 .. s.value.projects.size/itemsPerRow) {
                            val listEnd = if (itemsPerRow*(i+1) < s.value.projects.size)
                                itemsPerRow*(i+1)
                            else
                                s.value.projects.size

                            val subList = s.value.projects.subList(itemsPerRow*i, listEnd)

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                subList.map {
                                    Card(Modifier.padding(8.dp).weight(1f).clickable {
                                        navController.navigate("project/${it.projectId}")
                                    }) {
                                        Column(
                                            Modifier.fillMaxWidth().padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(48.dp)
                                        ) {
                                            Text(it.name, fontSize = secondaryFontSize)

                                            Row(
                                                Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start)
                                            ) {
                                                EizoImage(
                                                    url = it.owner.profilePicture ?: "",
                                                    fallbackPainter = painterResource(Res.drawable.person),
                                                    modifier = Modifier.size(24.dp).clip(CircleShape)
                                                )

                                                if (it.owner.uid == userDataInstance.value!!.uid)
                                                    Text(stringResource(Res.string.owned_by_you))
                                                else
                                                    Text(stringResource(Res.string.owned_by, it.owner.name))
                                            }
                                        }
                                    }
                                }

                                for (j in 0 until (itemsPerRow - subList.size))
                                    Box(Modifier.padding(8.dp).weight(1f))
                            }
                        }

                    }
                }
                is Failure -> DefaultErrorCentered(stringResource(Res.string.connect_fail)) { viewModel.getData() }
                is Loading -> DefaultLoadingCentered(s.customText)
                is Initial -> viewModel.getData()
            }
        }
    }
}