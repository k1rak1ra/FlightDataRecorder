package net.k1ra.flight_data_recorder_dashboard.di

import net.k1ra.flight_data_recorder_dashboard.features.dashboard.viewmodel.DashboardViewModel
import net.k1ra.flight_data_recorder_dashboard.features.login.viewmodel.LoginViewModel
import net.k1ra.flight_data_recorder_dashboard.features.project.viewmodel.ProjectViewModel
import net.k1ra.flight_data_recorder_dashboard.features.settings.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ViewModelProvider = module {
    viewModel { LoginViewModel() }
    viewModel { DashboardViewModel() }
    viewModel { parameters -> ProjectViewModel(projectId = parameters.get()) }
    viewModel { SettingsViewModel() }
}