package net.k1ra.flight_data_recorder_dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.generic_error
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.make_dashboard
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.make_login
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.koin.koinViewModel
import moe.tlaster.precompose.viewmodel.viewModel
import net.k1ra.flight_data_recorder.model.authentication.LoginRequest
import net.k1ra.flight_data_recorder.model.authentication.UserData
import net.k1ra.flight_data_recorder_dashboard.model.state.Failure
import net.k1ra.flight_data_recorder_dashboard.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.model.state.Success
import net.k1ra.flight_data_recorder_dashboard.theme.DarkColors
import net.k1ra.flight_data_recorder_dashboard.viewmodel.TestViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    val viewModel = koinViewModel(vmClass = TestViewModel::class)

    val baseUrl = BaseUrlGetter.getBaseUrl()
    var user by remember { mutableStateOf(null as UserData?) }
    var respStr by remember { mutableStateOf(null as String?) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.loginState.subscribe {
        scope.launch {
            when (it) {
                is Failure -> snackbarHostState.showSnackbar(getString(Res.string.generic_error, it.code, it.message?:""))
                Initial -> { /* Do nothing */ }
                is Loading -> { /* Show progress indicator with custom text */ }
                is Success -> user = it.value
            }
        }
    }

    viewModel.dashboardState.subscribe {
        scope.launch {
            when (it) {
                is Failure -> snackbarHostState.showSnackbar(getString(Res.string.generic_error, it.code, it.message?:""))
                Initial -> { /* Do nothing */ }
                is Loading -> { /* Show progress indicator with custom text */ }
                is Success -> respStr = it.value
            }
        }
    }

    MaterialTheme(
        colors = DarkColors
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { _ ->
            Column(
                Modifier.padding(all = 16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("BaseUrl is $baseUrl")

                EizoImageWeb(
                    url = "$baseUrl/invalid-path",
                    modifier = Modifier.size(100.dp).clip(CircleShape)
                )

                Button(onClick = { viewModel.login(LoginRequest("root", "Change Me!")) }) {
                    Text(stringResource(Res.string.make_login))
                }

                //If user isn't null, show this
                user?.let {
                    Card(
                        modifier = Modifier.padding(all = 16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            Modifier.padding(all = 16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EizoImageWeb(
                                url = it.picture,
                                modifier = Modifier.size(100.dp).clip(CircleShape)
                            )

                            Text(
                                it.name,
                                style = MaterialTheme.typography.h5,
                                textAlign = TextAlign.Center
                            )

                            Button(onClick = { viewModel.testDashboard() }) {
                                Text(stringResource(Res.string.make_dashboard))
                            }

                            //If we got a response to the dashboard request, show this
                            respStr?.let {
                                Text(it.trim())
                            }
                        }
                    }
                }
            }
        }
    }
}