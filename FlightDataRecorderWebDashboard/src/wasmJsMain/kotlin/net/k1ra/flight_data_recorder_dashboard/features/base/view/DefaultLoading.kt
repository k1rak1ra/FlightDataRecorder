package net.k1ra.flight_data_recorder_dashboard.features.base.view

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DefaultLoading(text: String?) {
    LinearProgressIndicator()

    text?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}