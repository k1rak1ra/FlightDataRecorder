package net.k1ra.flight_data_recorder.model.projects

data class QueryItem<T>(
    val key: String,
    val op: DatabaseQueryComparison,
    val value: T,
    val type: QueryDataType
)