package net.k1ra.flight_data_recorder_dashboard.features.login.view

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.k1ra.flight_data_recorder.model.authentication.LoginRequest
import net.k1ra.flight_data_recorder_dashboard.features.base.model.CustomSnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.SnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.CustomSnackbarHost
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoadingCentered
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultTextField
import net.k1ra.flight_data_recorder_dashboard.features.login.viewmodel.LoginViewModel
import net.k1ra.flight_data_recorder_dashboard.utils.titleFontSize
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
fun LoginView(navController: NavController) {
    val viewModel = koinViewModel<LoginViewModel>()
    val state = remember { viewModel.state }
    val snackbarState = CustomSnackbarState.init()
    val keyboardController = LocalSoftwareKeyboardController.current
    val usernameText = remember { mutableStateOf("") }
    val passwordText = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { CustomSnackbarHost(snackbarState) }
    ) { padding ->
        Column {
            when (val s = state.value) {
                is Success -> {
                    navController.navigate("dashboard") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
                is Failure -> {
                    if (s.reason.code == 401)
                        snackbarState.showSnackbar(Res.string.incorrect_credentials, SnackbarState.ERROR)
                    else if (s.reason.code == 403)
                        snackbarState.showSnackbar(Res.string.user_exists, SnackbarState.ERROR)
                    else
                        snackbarState.showSnackbar(Res.string.connect_fail, SnackbarState.ERROR)

                    viewModel.state.value = Initial
                }
                is Loading -> DefaultLoadingCentered(s.customText)
                is Initial -> {
                    Column(
                        Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        viewModel.checkExistingSessionData()

                        Box(modifier = Modifier.height(100.dp))
                        Text(stringResource(Res.string.app_name), fontSize = titleFontSize)
                        Box(modifier = Modifier.height(100.dp))
                        DefaultTextField(usernameText, stringResource(Res.string.username_or_email), keyboardController)
                        DefaultTextField(
                            passwordText,
                            stringResource(Res.string.password),
                            keyboardController,
                            passwordMode = true
                        )
                        Button(onClick = {
                            viewModel.doLogin(LoginRequest(usernameText.value, passwordText.value))
                        }) {
                            Text(stringResource(Res.string.login))
                        }

                    }
                }
            }
        }
    }
}