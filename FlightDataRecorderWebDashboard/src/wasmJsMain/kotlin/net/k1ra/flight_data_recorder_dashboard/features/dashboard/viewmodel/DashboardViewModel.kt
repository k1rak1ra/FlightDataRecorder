package net.k1ra.flight_data_recorder_dashboard.features.dashboard.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.k1ra.flight_data_recorder.model.dashboard.DashboardData
import net.k1ra.flight_data_recorder.model.projects.ProjectCreationRequest
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
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel : ViewModel(), KoinComponent {
    val state = mutableStateOf(Initial as Result<DashboardData>)
    val createProjectState = mutableStateOf(Initial as Result<Unit>)

    private val httpClient: HoodiesNetworkClient by inject()
    private val sharedPref: SharedPreferences by inject()

    init {
        forceAutoRefreshHook = { getData() }

        //Auto refresh thread
        viewModelScope.launch {
            while(true) {
                delay(10.seconds)
                getData(false)
            }
        }
    }

    fun getData(showLoading: Boolean = true) = viewModelScope.launch {
        if (showLoading)
            state.value = Loading("Loading...")

        state.value = httpClient.get<DashboardData>("webapi/dashboard").also {
            if (it is Success) {
                userDataInstance.value = it.value.userData
                sharedPref.set(USERDATA_KEY, it.value.userData)
            }
        }
    }

    fun createProject(name: String) = viewModelScope.launch {
        createProjectState.value = Loading(null)

        createProjectState.value = httpClient.post("webapi/project", ProjectCreationRequest(name))
        getData()
    }

    companion object {
        var forceAutoRefreshHook = {}
    }
}
