package net.k1ra.flight_data_recorder_dashboard.helpers

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StateFlowClass<T>(private val delegate: StateFlow<T>) : StateFlow<T> by delegate {
    @OptIn(DelicateCoroutinesApi::class)
    fun subscribe(block: (T) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        delegate.collect { value -> block(value) }
    }
}

fun <T> StateFlow<T>.asStateFlowClass(): StateFlowClass<T> = StateFlowClass(this)