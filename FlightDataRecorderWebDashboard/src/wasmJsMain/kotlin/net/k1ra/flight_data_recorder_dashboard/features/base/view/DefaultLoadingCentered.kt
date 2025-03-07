package net.k1ra.flight_data_recorder_dashboard.features.base.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoading

@Composable
fun DefaultLoadingCentered(text: String?) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
    ) {
        DefaultLoading(text)
    }
}