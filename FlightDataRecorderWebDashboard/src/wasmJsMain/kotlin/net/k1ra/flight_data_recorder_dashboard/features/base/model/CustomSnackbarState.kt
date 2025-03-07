package net.k1ra.flight_data_recorder_dashboard.features.base.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class CustomSnackbarState(
    val scope: CoroutineScope,
    val hostState: SnackbarHostState,
    val colorState: MutableState<SnackbarState>
) {
    fun showSnackbar(resource: StringResource, color: SnackbarState) = scope.launch {
        colorState.value = color
        hostState.showSnackbar(getString(resource))
    }

    companion object {
        @Composable
        fun init() : CustomSnackbarState {
            val scope = rememberCoroutineScope()
            val hostState = remember { SnackbarHostState() }
            val colorState = remember { mutableStateOf(SnackbarState.DEFAULT) }

            return CustomSnackbarState(scope, hostState, colorState)
        }
    }
}