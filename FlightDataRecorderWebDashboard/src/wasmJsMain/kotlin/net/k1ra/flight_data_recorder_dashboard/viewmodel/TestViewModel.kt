package net.k1ra.flight_data_recorder_dashboard.viewmodel

import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.loading_dashboard
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.logging_in
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import net.k1ra.flight_data_recorder.model.authentication.LoginRequest
import net.k1ra.flight_data_recorder.model.authentication.UserData
import net.k1ra.flight_data_recorder_dashboard.BaseUrlGetter
import net.k1ra.flight_data_recorder_dashboard.helpers.asStateFlowClass
import net.k1ra.flight_data_recorder_dashboard.model.state.Failure
import net.k1ra.flight_data_recorder_dashboard.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.model.state.State
import net.k1ra.flight_data_recorder_dashboard.model.state.Success
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
class TestViewModel : ViewModel() {
    //TODO DI ME WITH KOIN
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
   private val baseUrl = BaseUrlGetter.getBaseUrl()

    private val _loginState = MutableStateFlow<State<UserData>>(Initial)
    val loginState = _loginState.asStateFlowClass()

    private val _dashboardState = MutableStateFlow<State<String>>(Initial)
    val dashboardState = _dashboardState.asStateFlowClass()

    fun login(request: LoginRequest) = viewModelScope.launch {
        _loginState.value = Loading(getString(Res.string.logging_in))

        val resp = client.post("${baseUrl}webapi/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (resp.status.value == 200)
            _loginState.value = Success(resp.body())
        else
            _loginState.value = Failure(resp.status.value, resp.bodyAsText())
    }

    fun testDashboard() = viewModelScope.launch {
        val state = _loginState.value //TODO GLOBAL DI FOR USER DATA
        if (state is Success) {
            _dashboardState.value = Loading(getString(Res.string.loading_dashboard))

            val resp = client.get("${baseUrl}webapi/dashboard") {
                headers {
                    //TODO I CAN PROBABLY BE DI'D GLOBALLY
                    append("Authorization", "Bearer ${state.value.uid}:${state.value.token}")
                }
            }

            if (resp.status.value == 200)
                _dashboardState.value = Success(resp.bodyAsText())
            else {
                //TODO if response to any non-login request is a 401
                // there should be a global kick-out that will delete all stored user data and send the user straight back to the login

                _dashboardState.value = Failure(resp.status.value, resp.bodyAsText())
            }
        } else {
            _dashboardState.value = Failure(401, "User data missing")
        }
    }
}