package net.k1ra.flight_data_recorder_dashboard.features.project.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.k1ra.eizo.EizoImage
import net.k1ra.flight_data_recorder.model.authentication.SimpleUserData
import net.k1ra.flight_data_recorder.model.projects.ModifyShareRequest
import net.k1ra.flight_data_recorder.model.projects.ProjectData
import net.k1ra.flight_data_recorder.model.projects.ProjectUpdateRequest
import net.k1ra.flight_data_recorder.model.projects.UserPermissionLevel
import net.k1ra.flight_data_recorder_dashboard.di.userDataInstance
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.datatable.view.DataColumn
import net.k1ra.flight_data_recorder_dashboard.features.datatable.view.DataTable
import net.k1ra.flight_data_recorder_dashboard.features.project.viewmodel.ProjectViewModel
import net.k1ra.flight_data_recorder_dashboard.theme.errorContainerDark
import net.k1ra.flight_data_recorder_dashboard.utils.secondaryFontSize
import net.k1ra.flight_data_recorder_dashboard.utils.titleFontSize
import org.jetbrains.compose.resources.painterResource
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProjectSettingsView(viewModel: ProjectViewModel, data: ProjectData) {
    val scrollState = rememberScrollState()
    val onUserSelected = remember { mutableStateOf({ _: SimpleUserData -> }) }
    val showUserSelect = remember { mutableStateOf(false) }
    val userData = remember { userDataInstance }
    val userSelectRemoveShares = remember { mutableStateOf(false) }

    if (showUserSelect.value)
        ProjectSelectUserDialog(viewModel, showUserSelect, onUserSelected.value, userSelectRemoveShares.value)

    Column(
        Modifier.fillMaxSize().verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(Res.string.project_api_key), fontSize = titleFontSize)
        SelectionContainer {
            Text(data.projectId, fontSize = secondaryFontSize)
        }

        Box(Modifier.height(8.dp))
        HorizontalDivider()
        Box(Modifier.height(8.dp))

        Text(stringResource(Res.string.owner), fontSize = titleFontSize)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            EizoImage(
                url = data.owner.profilePicture ?: "",
                fallbackPainter = painterResource(Res.drawable.person),
                modifier = Modifier.size(24.dp).clip(CircleShape)
            )

            if (data.owner.uid == userDataInstance.value!!.uid)
                Text(stringResource(Res.string.you))
            else
                Text(data.owner.name)
        }

        if (data.permissionLevel == UserPermissionLevel.OWNER) {
            Button(onClick = {
                viewModel.getAllUsersState.value = Initial
                onUserSelected.value = {
                    viewModel.update(ProjectUpdateRequest(
                        updateOwner = it.uid,
                        query = viewModel.query.value
                    ))
                }
                userSelectRemoveShares.value = false
                showUserSelect.value = true
            }) {
                Text(stringResource(Res.string.change_owner))
            }
        }

        if (data.permissionLevel == UserPermissionLevel.WRITE || data.permissionLevel == UserPermissionLevel.OWNER) {
            Box(Modifier.height(8.dp))
            HorizontalDivider()
            Box(Modifier.height(8.dp))
            Text(stringResource(Res.string.project_shared_with), fontSize = titleFontSize)

            val keys = listOf(stringResource(Res.string.picture), stringResource(Res.string.name), stringResource(Res.string.level), "")

            DataTable(
                columns = keys.map {
                    DataColumn(alignment = Alignment.Center) {
                        Text(it, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp, 0.dp))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                data.shares.filter { it.user.uid != userData.value!!.uid }.map {
                    row {
                        cell {
                            EizoImage(
                                url = it.user.profilePicture ?: "",
                                fallbackPainter = painterResource(Res.drawable.person),
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                            )
                        }
                        cell { Text(it.user.name) }
                        cell {
                            val dropdownExpanded = remember { mutableStateOf(false) }

                            TextButton(onClick = {
                                dropdownExpanded.value = true
                            }) {
                                Row {
                                    when (it.level) {
                                        UserPermissionLevel.READONLY -> Text(stringResource(Res.string.readonly))
                                        else -> Text(stringResource(Res.string.write))
                                    }
                                    Icon(Icons.Filled.ArrowDropDown, "dropdown")
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded.value,
                                onDismissRequest = {
                                    dropdownExpanded.value = false
                                },
                            ) {
                                val dropdownItems = arrayListOf(
                                    stringResource(Res.string.readonly),
                                    stringResource(Res.string.write),
                                )

                                dropdownItems.map { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            CoroutineScope(Dispatchers.Default).launch {
                                                viewModel.update(ProjectUpdateRequest(
                                                    editShare = ModifyShareRequest(
                                                        it.user.uid,
                                                        if (item == getString(Res.string.write)) { UserPermissionLevel.WRITE } else { UserPermissionLevel.READONLY }
                                                    ),
                                                    query = viewModel.query.value
                                                ))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        cell {
                            Button(
                                onClick = {
                                    viewModel.update(ProjectUpdateRequest(
                                        deleteShare = it.user.uid,
                                        query = viewModel.query.value
                                    ))
                                },
                                colors = ButtonColors(
                                    errorContainerDark,
                                    contentColorFor(errorContainerDark),
                                    errorContainerDark,
                                    contentColorFor(errorContainerDark)
                                )
                            ) {
                                Text(stringResource(Res.string.cancel_share))
                            }
                        }
                    }
                }
            }

            Button(onClick = {
                viewModel.getAllUsersState.value = Initial
                onUserSelected.value = {
                    viewModel.update(ProjectUpdateRequest(
                        addShare = it.uid,
                        query = viewModel.query.value
                    ))
                }
                userSelectRemoveShares.value = true
                showUserSelect.value = true
            }) {
                Text(stringResource(Res.string.add_share))
            }
        }
    }
}