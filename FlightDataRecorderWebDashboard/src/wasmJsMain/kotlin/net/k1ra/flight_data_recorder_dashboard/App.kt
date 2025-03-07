package net.k1ra.flight_data_recorder_dashboard

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.flight_data_recorder.feature.logging.Log
import net.k1ra.flight_data_recorder_dashboard.di.NetworkProvider
import net.k1ra.flight_data_recorder_dashboard.di.UserDataProvider
import net.k1ra.flight_data_recorder_dashboard.di.ViewModelProvider
import net.k1ra.flight_data_recorder_dashboard.di.forceLogoutHook
import net.k1ra.flight_data_recorder_dashboard.features.dashboard.view.DashboardView
import net.k1ra.flight_data_recorder_dashboard.features.login.view.LoginView
import net.k1ra.flight_data_recorder_dashboard.features.project.view.ProjectView
import net.k1ra.flight_data_recorder_dashboard.features.settings.view.SettingsHomeView
import net.k1ra.flight_data_recorder_dashboard.features.settings.view.SettingsPasswordView
import net.k1ra.flight_data_recorder_dashboard.features.settings.view.SettingsPersonalInfoView
import net.k1ra.flight_data_recorder_dashboard.features.settings.view.SettingsProfileView
import net.k1ra.flight_data_recorder_dashboard.features.settings.view.SettingsUserManagementView
import net.k1ra.flight_data_recorder_dashboard.theme.AppTypography
import net.k1ra.flight_data_recorder_dashboard.theme.darkScheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication({
        //Koin configuration
        modules(
            ViewModelProvider,
            UserDataProvider,
            NetworkProvider
        )
    }) {
        //Main app Composable
        MaterialTheme(
            colorScheme = darkScheme,
            typography = AppTypography,
        ) {
            val navController = rememberNavController()

            forceLogoutHook = {
                CoroutineScope(Dispatchers.Main).launch {

                    if (navController.currentDestination?.route != "login") {
                        navController.popBackStack(route = "dashboard", inclusive = true)
                        navController.navigate("login")
                    }
                }
            }

            navController.addOnDestinationChangedListener { _, dest, _ ->
                Log.d("NavController", "Navigated to ${dest.route}")
            }

            NavHost(
                navController = navController,
                startDestination = "login"
            ) {
                composable("login") {
                    LoginView(navController)
                }

                composable("dashboard") {
                    DashboardView(navController)
                }

                composable("settings_home") {
                    SettingsHomeView(navController)
                }

                composable("settings_profile") {
                    SettingsProfileView(navController)
                }

                composable("settings_personal_info") {
                    SettingsPersonalInfoView(navController)
                }

                composable("settings_password") {
                    SettingsPasswordView(navController)
                }

                composable("settings_user_management") {
                    SettingsUserManagementView(navController)
                }

                composable("project/{projectId}") { navBackStackEntry ->
                    ProjectView(navController, navBackStackEntry.arguments?.getString("projectId")!!)
                }
            }
        }
    }
}