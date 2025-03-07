package net.k1ra.flight_data_recorder_dashboard.features.settings.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import net.k1ra.eizo.EizoImage
import net.k1ra.flight_data_recorder.model.authentication.UserRole
import net.k1ra.flight_data_recorder_dashboard.di.forceLogoutHook
import net.k1ra.flight_data_recorder_dashboard.di.userDataInstance
import net.k1ra.flight_data_recorder_dashboard.features.base.model.CustomSnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.BackButtonAppBar
import net.k1ra.flight_data_recorder_dashboard.features.base.view.CustomSnackbarHost
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoadingCentered
import net.k1ra.flight_data_recorder_dashboard.features.settings.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsHomeView(navController: NavController) {
    val snackbarState = CustomSnackbarState.init()
    val viewModel = koinViewModel<SettingsViewModel>()
    val userData = remember { userDataInstance }
    val logoutState = remember { viewModel.logoutState }
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { CustomSnackbarHost(snackbarState) },
        topBar = { BackButtonAppBar(navController, stringResource(Res.string.account)) }
    ) { padding ->
        when (val s = logoutState.value) {
            is Initial -> {
                Column(
                    Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    EizoImage(
                        url = userData.value?.profilePicture ?: "",
                        fallbackPainter = painterResource(Res.drawable.person),
                        modifier = Modifier.size(120.dp).clip(CircleShape)
                    )
                    Box(modifier = Modifier.height(8.dp))

                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.profile)) },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.NavigateNext,
                                contentDescription = "navigate",
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("settings_profile")
                        }
                    )
                    HorizontalDivider()

                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.personal_information)) },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.NavigateNext,
                                contentDescription = "navigate",
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("settings_personal_info")
                        }
                    )
                    HorizontalDivider()

                    if (userData.value!!.native) {
                        ListItem(
                            headlineContent = { Text(stringResource(Res.string.authentication)) },
                            trailingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.NavigateNext,
                                    contentDescription = "navigate",
                                )
                            },
                            modifier = Modifier.clickable {
                                navController.navigate("settings_password")
                            }
                        )
                        HorizontalDivider()
                    }

                    if (userData.value!!.role == UserRole.ADMIN) {
                        ListItem(
                            headlineContent = { Text(stringResource(Res.string.manage_users)) },
                            trailingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.NavigateNext,
                                    contentDescription = "navigate",
                                )
                            },
                            modifier = Modifier.clickable {
                                navController.navigate("settings_user_management")
                            }
                        )
                        HorizontalDivider()
                    }

                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.log_out)) },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.NavigateNext,
                                contentDescription = "navigate",
                            )
                        },
                        modifier = Modifier.clickable {
                            viewModel.logoutUser()
                        }
                    )
                    HorizontalDivider()
                }
            }
            is Loading -> DefaultLoadingCentered(s.customText)
            else -> forceLogoutHook.invoke()
        }
    }
}
