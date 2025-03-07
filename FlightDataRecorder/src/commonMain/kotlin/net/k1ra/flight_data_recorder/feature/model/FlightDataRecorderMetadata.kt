package net.k1ra.flight_data_recorder.feature.model

import kotlinx.serialization.json.JsonPrimitive

sealed class FlightDataRecorderMetadata {
    class Int(val int: kotlin.Int) : FlightDataRecorderMetadata()
    class Float(val float: kotlin.Float) : FlightDataRecorderMetadata()
    class Double(val double: kotlin.Double) : FlightDataRecorderMetadata()
    class Boolean(val boolean: kotlin.Boolean) : FlightDataRecorderMetadata()
    class String(val string: kotlin.String) : FlightDataRecorderMetadata()
}

fun FlightDataRecorderMetadata.toJsonPrimitive() : JsonPrimitive {
    return if (this is FlightDataRecorderMetadata.Int) {
        JsonPrimitive(this.int)
    } else if (this is FlightDataRecorderMetadata.Float) {
        JsonPrimitive(this.float)
    } else if (this is FlightDataRecorderMetadata.Double) {
        JsonPrimitive(this.double)
    } else if (this is FlightDataRecorderMetadata.Boolean) {
        JsonPrimitive(this.boolean)
    } else {
        JsonPrimitive((this as FlightDataRecorderMetadata.String).string)
    }
}