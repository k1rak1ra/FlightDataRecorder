import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.k1ra.flight_data_recorder.feature.config.FlightDataRecorderConfig
import net.k1ra.flight_data_recorder.feature.logging.Log
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    FlightDataRecorderConfig.apply {
        logServer = "http://10.0.0.22:8091"
        appKey = "TEST-KEY"
    }

    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                Log.d("TestTag", "TestMessage", mapOf("a1" to "a1val", "a2" to "a2val"))
            }) {
                Text("Log")
            }
        }
    }
}