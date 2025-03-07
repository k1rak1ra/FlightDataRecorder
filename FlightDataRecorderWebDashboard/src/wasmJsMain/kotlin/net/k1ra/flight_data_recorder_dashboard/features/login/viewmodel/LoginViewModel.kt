package net.k1ra.flight_data_recorder_dashboard.features.login.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.k1ra.flight_data_recorder.model.authentication.ClientUserData
import net.k1ra.flight_data_recorder.model.authentication.LoginRequest
import net.k1ra.flight_data_recorder_dashboard.di.userDataInstance
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.utils.USERDATA_KEY
import net.k1ra.hoodies_network_kmm.HoodiesNetworkClient
import net.k1ra.hoodies_network_kmm.result.Result
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.sharedprefkmm.SharedPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginViewModel : ViewModel(), KoinComponent {
    val state = mutableStateOf(Initial as Result<ClientUserData>)

    private val httpClient: HoodiesNetworkClient by inject()
    private val sharedPref: SharedPreferences by inject()

    fun checkExistingSessionData() = viewModelScope.launch {
        val data = sharedPref.get<ClientUserData>(USERDATA_KEY) ?: return@launch
        state.value = Success(data)
    }

    fun doLogin(request: LoginRequest) = viewModelScope.launch {
        state.value = Loading("Logging you in...")

        state.value = httpClient.post<ClientUserData, LoginRequest>("webapi/login", request).also {
            if (it is Success) {
                userDataInstance.value = it.value
                sharedPref.set(USERDATA_KEY, it.value)
            }
        }
    }

}