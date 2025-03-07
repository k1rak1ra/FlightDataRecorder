package net.k1ra.flight_data_recorder_dashboard.features.base.view

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.k1ra.flight_data_recorder_dashboard.features.base.model.CustomSnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.SnackbarState

@Composable
fun CustomSnackbarHost(state: CustomSnackbarState) {
    SnackbarHost(hostState = state.hostState) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = when (state.colorState.value) {
                SnackbarState.DEFAULT -> SnackbarDefaults.color
                SnackbarState.ERROR -> Color.Red
            },
            contentColor = when (state.colorState.value) {
                SnackbarState.DEFAULT -> SnackbarDefaults.contentColor
                SnackbarState.ERROR -> Color.White
            }
        )
    }
}