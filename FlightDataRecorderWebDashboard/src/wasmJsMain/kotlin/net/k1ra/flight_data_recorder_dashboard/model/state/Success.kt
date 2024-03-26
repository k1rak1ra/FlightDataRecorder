package net.k1ra.flight_data_recorder_dashboard.model.state

data class Success<out T>(val value: T) : State<T>()