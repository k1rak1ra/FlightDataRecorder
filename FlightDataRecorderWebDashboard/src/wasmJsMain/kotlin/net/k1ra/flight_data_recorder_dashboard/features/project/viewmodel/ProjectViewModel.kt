package net.k1ra.flight_data_recorder_dashboard.features.project.viewmodel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.mapMarker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import net.k1ra.flight_data_recorder.model.authentication.SimpleUserData
import net.k1ra.flight_data_recorder.model.projects.IpGeolocationData
import net.k1ra.flight_data_recorder.model.projects.LogSearchQuery
import net.k1ra.flight_data_recorder.model.projects.ProjectData
import net.k1ra.flight_data_recorder.model.projects.ProjectUpdateRequest
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.hoodies_network_kmm.HoodiesNetworkClient
import net.k1ra.hoodies_network_kmm.result.Result
import net.k1ra.hoodies_network_kmm.result.Success
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ovh.plrapps.mapcompose.api.addCallout
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.removeAllMarkers
import ovh.plrapps.mapcompose.api.snapScrollTo
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan
import kotlin.time.Duration.Companion.seconds

class ProjectViewModel(val projectId: String) : ViewModel(), KoinComponent {
    val state = mutableStateOf(Initial as Result<ProjectData>)
    val getAllUsersState = mutableStateOf(Initial as Result<List<SimpleUserData>>)
    var defaultQuery = ""
    val query = mutableStateOf("")
    val selectedItem = mutableStateOf(0)

    private val httpClient: HoodiesNetworkClient by inject()

    private val hashMapOfMapPointData = mutableMapOf<String, @Composable () -> Unit>()

    @OptIn(ExperimentalResourceApi::class)
    private val tileStreamProvider = TileStreamProvider { row, col, zoomLvl ->
        try {
            val buffer = Buffer()
            Res.readBytes("files/tiles/$zoomLvl/$col/$row.png").let {
                buffer.write(it)
                buffer
            }

            buffer
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val mapState = MapState(1, 4096, 4096).apply {
        addLayer(tileStreamProvider)

        viewModelScope.launch {
            snapScrollTo(0.5, 0.45)
        }
    }

    init {
        mapState.onMarkerClick { id, x, y ->
            hashMapOfMapPointData[id]?.let { mapState.addCallout(id, x = x, y = y, c = it) }
        }

        forceAutoRefreshHook = { getData() }

        //Auto refresh thread
        viewModelScope.launch {
            while(true) {
                delay(30.seconds)
                getData(false)
            }
        }
    }

    fun getData(showLoading: Boolean = true) = viewModelScope.launch {
        if (showLoading)
            state.value = Loading("Loading...")

        state.value = httpClient.post<ProjectData, LogSearchQuery>("webapi/project/$projectId", LogSearchQuery(query.value)).also {
            if (it is Success)
                handleMap(it.value)
        }
    }

    private fun handleMap(data: ProjectData) {
        if (query.value == "")
            query.value = "SELECT * FROM ${data.name}"
        defaultQuery = "SELECT * FROM ${data.name}"

        hashMapOfMapPointData.clear()
        mapState.removeAllMarkers()

        val hashMapOfDuplicateLocations = mutableMapOf<Pair<String, String>, ArrayList<IpGeolocationData>>()

        data.currentSessions.forEach {
            if (hashMapOfDuplicateLocations[Pair(it.longitude, it.latitude)] == null)
                hashMapOfDuplicateLocations[Pair(it.longitude, it.latitude)] = arrayListOf(it)
            else
                hashMapOfDuplicateLocations[Pair(it.longitude, it.latitude)]!!.add(it)
        }

        hashMapOfDuplicateLocations.onEachIndexed { index, entry ->
            val x = (entry.key.first.toDouble() + 180.0) / 360.0

            val latRad = entry.key.second.toDouble() * PI / 180
            val mercN = ln(tan((PI / 4) + (latRad / 2)))
            val y = 0.5 - (mercN / (2 * PI))

            val size = when (entry.value.size) {
                1 -> 7
                2 -> 10
                3 -> 13
                4 -> 17
                5 -> 20
                6 -> 21
                7 -> 22
                8 -> 23
                9 -> 24
                10 -> 25
                11 -> 26
                12 -> 27
                13 -> 28
                14 -> 29
                else -> 30
            }

            mapState.addMarker("id$index", x = x, y = y) {
                Icon(
                    painter = painterResource(Res.drawable.mapMarker),
                    contentDescription = null,
                    modifier = Modifier.size(size.dp),
                    tint = Color.Black,
                )
            }

            hashMapOfMapPointData["id$index"] = {
                Card {
                    Column(
                        Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${entry.value.size} sessions")
                        Text("${entry.value.first().city}, ${entry.value.first().state}")
                        Text(entry.value.first().country)
                    }
                }
            }
        }
    }

    fun getAllUsers() = viewModelScope.launch {
        getAllUsersState.value = Loading("Getting users...")

        getAllUsersState.value = httpClient.get("webapi/all_users")
    }

    fun update(request: ProjectUpdateRequest) = viewModelScope.launch {
        state.value = Loading("Making the changes...")

        state.value = httpClient.patch<ProjectData, ProjectUpdateRequest>("webapi/project/$projectId", request).also {
            if (it is Success)
                handleMap(it.value)
        }
    }

    companion object {
        var forceAutoRefreshHook = {}
    }
}
