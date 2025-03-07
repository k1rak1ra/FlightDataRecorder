package net.k1ra.flight_data_recorder_dashboard.features.base.view

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun DefaultError(text: String, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )

    Button(onClick = {
        onClick.invoke()
    }){
        Text(stringResource(Res.string.retry))
    }
}