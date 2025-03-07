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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.k1ra.flight_data_recorder_dashboard.features.settings.viewmodel.SettingsViewModel
import net.k1ra.flight_data_recorder.model.settings.PasswordUpdateRequest
import net.k1ra.flight_data_recorder.model.settings.UserSettingsUpdateRequest
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
import org.koin.compose.viewmodel.koinViewModel
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsPasswordView(navController: NavController) {
    val snackbarState = CustomSnackbarState.init()
    val viewModel = koinViewModel<SettingsViewModel>()
    val state = remember { viewModel.updateUserDataState }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val oldPasswordText = remember { mutableStateOf("") }
    val passwordText = remember { mutableStateOf("") }
    val passwordConfirmText = remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { CustomSnackbarHost(snackbarState) },
        topBar = { BackButtonAppBar(navController, stringResource(Res.string.change_password)) }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.height(8.dp))

            DefaultTextField(
                oldPasswordText,
                stringResource(Res.string.current_password),
                keyboardController,
                passwordMode = true
            )
            DefaultTextField(
                passwordText,
                stringResource(Res.string.new_password),
                keyboardController,
                passwordMode = true
            )
            DefaultTextField(
                passwordConfirmText,
                stringResource(Res.string.confirm_new_password),
                keyboardController,
                passwordMode = true
            )

            when(val p = state.value) {
                is Initial -> {
                    Button(onClick = {
                        keyboardController?.hide()
                        viewModel.updateUserDataState.value = Loading(null)

                        if (passwordText.value == passwordConfirmText.value) {
                            viewModel.updateUserData(
                                UserSettingsUpdateRequest(
                                    passwordUpdateRequest = PasswordUpdateRequest(
                                        oldPasswordText.value,
                                        passwordText.value
                                    )
                                )
                            )
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarState.showSnackbar(Res.string.password_do_not_match, SnackbarState.ERROR)
                            }
                        }
                    }) {
                        Text(stringResource(Res.string.save))
                    }
                }
                is Loading -> { DefaultLoading(p.customText) }
                is Failure -> {
                    if (p.reason.code == 403) {
                        if (p.reason.message == "oldPassword")
                            snackbarState.showSnackbar(Res.string.old_password_incorrect, SnackbarState.ERROR)
                        else
                            snackbarState.showSnackbar(Res.string.password_too_short, SnackbarState.ERROR)
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
