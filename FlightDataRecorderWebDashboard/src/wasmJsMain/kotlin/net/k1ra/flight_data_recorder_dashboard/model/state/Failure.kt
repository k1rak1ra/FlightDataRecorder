package net.k1ra.flight_data_recorder_dashboard.model.state

data class Failure(val code: Int, val message: String?) : State<Nothing>