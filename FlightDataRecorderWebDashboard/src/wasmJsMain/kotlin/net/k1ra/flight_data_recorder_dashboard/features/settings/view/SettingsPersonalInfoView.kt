package net.k1ra.flight_data_recorder_dashboard.features.settings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.k1ra.flight_data_recorder_dashboard.features.settings.viewmodel.SettingsViewModel
import net.k1ra.flight_data_recorder.model.settings.PersonalInformationUpdateRequest
import net.k1ra.flight_data_recorder.model.settings.UserSettingsUpdateRequest
import net.k1ra.flight_data_recorder_dashboard.di.userDataInstance
import net.k1ra.flight_data_recorder_dashboard.features.base.model.CustomSnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.SnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.BackButtonAppBar
import net.k1ra.flight_data_recorder_dashboard.features.base.view.CustomSnackbarHost
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultTextField
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsPersonalInfoView(navController: NavController) {
    val snackbarState = CustomSnackbarState.init()
    val viewModel = koinViewModel<SettingsViewModel>()
    val userData = remember { userDataInstance }
    val state = remember { viewModel.updateUserDataState }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val nameText = remember { mutableStateOf(userData.value!!.name) }
    val emailText = remember { mutableStateOf(userData.value!!.email) }

    Scaffold(
        snackbarHost = { CustomSnackbarHost(snackbarState) },
        topBar = { BackButtonAppBar(navController, stringResource(Res.string.your_personal_information)) }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.height(8.dp))

            DefaultTextField(nameText, stringResource(Res.string.name), keyboardController)
            DefaultTextField(emailText, stringResource(Res.string.email), keyboardController)

            when(val p = state.value) {
                is Initial -> {
                    Button(onClick = {
                        keyboardController?.hide()
                        viewModel.updateUserDataState.value = Loading(null)

                        viewModel.updateUserData(
                            UserSettingsUpdateRequest(
                            personalInformationUpdateRequest = PersonalInformationUpdateRequest(
                                nameText.value,
                                emailText.value
                            )
                        )
                        )
                    }) {
                        Text(stringResource(Res.string.save))
                    }
                }
                is Loading -> { DefaultLoading(p.customText) }
                is Failure -> {
                    if (p.reason.code == 403) {
                        if (p.reason.message == "blank")
                            snackbarState.showSnackbar(Res.string.blank, SnackbarState.ERROR)
                        else
                            snackbarState.showSnackbar(Res.string.emailNotUnique, SnackbarState.ERROR)
                    } else {
                        snackbarState.showSnackbar(Res.string.connect_fail, SnackbarState.ERROR)
                    }

                    viewModel.updateUserDataState.value = Initial
                }
                is Success -> {
                    viewModel.updateUserDataState.value = Initial
                    snackbarState.showSnackbar(Res.string.updated_successfully, SnackbarState.DEFAULT)
                }
            }
        }
    }
}
