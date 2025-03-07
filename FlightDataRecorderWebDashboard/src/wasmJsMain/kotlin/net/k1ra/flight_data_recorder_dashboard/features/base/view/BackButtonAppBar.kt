package net.k1ra.flight_data_recorder_dashboard.features.base.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackButtonAppBar(navController: NavController, title: String) {
    val scope = rememberCoroutineScope()

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "back")
            }
        }
    )
}