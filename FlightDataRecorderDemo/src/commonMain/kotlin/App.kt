import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.k1ra.flight_data_recorder.feature.config.FlightDataRecorderConfig
import net.k1ra.flight_data_recorder.feature.logging.Log
import net.k1ra.flight_data_recorder.feature.model.FlightDataRecorderMetadata

@Composable
fun App() {
    FlightDataRecorderConfig.apply {
        logServer = "http://10.0.0.22:8091"
        appKey = "TEST-KEY"
    }

    MaterialTheme {
        Scaffold { padding ->
            Column(Modifier.fillMaxWidth().padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    Log.d(
                        "TestTag",
                        "TestMessage",
                        mapOf(
                            "a1" to FlightDataRecorderMetadata.String("a1val"),
                            "a2" to FlightDataRecorderMetadata.String("a2val")
                        )
                    )
                }) {
                    Text("Log")
                }
            }
        }
    }
}