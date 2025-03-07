package net.k1ra.flight_data_recorder_dashboard.features.settings.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.k1ra.flight_data_recorder.model.adminsettings.DetailedUserData
import net.k1ra.flight_data_recorder.model.adminsettings.UserCreationRequest
import net.k1ra.flight_data_recorder.model.adminsettings.UserPasswordUpdateRequest
import net.k1ra.flight_data_recorder.model.adminsettings.UserUpdateRequest
import net.k1ra.flight_data_recorder.model.settings.UserSettingsUpdateRequest
import net.k1ra.flight_data_recorder_dashboard.di.userDataInstance
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.dashboard.viewmodel.DashboardViewModel
import net.k1ra.flight_data_recorder_dashboard.utils.USERDATA_KEY
import net.k1ra.hoodies_network_kmm.HoodiesNetworkClient
import net.k1ra.hoodies_network_kmm.result.Result
import net.k1ra.sharedprefkmm.SharedPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsViewModel : ViewModel(), KoinComponent {
    val logoutState = mutableStateOf(Initial as Result<Unit>)
    val updateUserDataState = mutableStateOf(Initial as Result<Unit>)
    val fetchUsersState = mutableStateOf(Initial as Result<List<DetailedUserData>>)
    val createUserState = mutableStateOf(Initial as Result<Unit>)
    val updateUserState = mutableStateOf(Initial as Result<Unit>)
    val updateUserPasswordState = mutableStateOf(Initial as Result<Unit>)

    private val httpClient: HoodiesNetworkClient by inject()
    private val sharedPref: SharedPreferences by inject()

    fun logoutUser() = viewModelScope.launch {
        logoutState.value = Loading("Logging you out...")

        logoutState.value = httpClient.get<Unit>("webapi/logout").also {
            userDataInstance.value = null
            sharedPref.delete(USERDATA_KEY)
        }
    }

    fun updateUserData(request: UserSettingsUpdateRequest) = viewModelScope.launch {
        updateUserDataState.value = Loading(null)

        updateUserDataState.value = httpClient.post("webapi/user_settings", request)

        DashboardViewModel.forceAutoRefreshHook.invoke()
    }

    fun getUsers() = viewModelScope.launch {
        fetchUsersState.value = Loading("Loading users...")

        fetchUsersState.value = httpClient.get("webapi/manage_users")
    }

    fun createUser(request: UserCreationRequest) = viewModelScope.launch {
        createUserState.value = Loading(null)

        createUserState.value = httpClient.post("webapi/manage_users", request)
        getUsers()
    }

    fun updateUser(request: UserUpdateRequest, uid: String) = viewModelScope.launch {
        updateUserState.value = Loading(null)

        updateUserState.value = httpClient.post("webapi/manage_users/$uid", request)
        getUsers()
    }

    fun updateUserPassword(request: UserPasswordUpdateRequest, uid: String) = viewModelScope.launch {
        updateUserPasswordState.value = Loading(null)

        updateUserPasswordState.value = httpClient.patch("webapi/manage_users/$uid", request)
    }
}