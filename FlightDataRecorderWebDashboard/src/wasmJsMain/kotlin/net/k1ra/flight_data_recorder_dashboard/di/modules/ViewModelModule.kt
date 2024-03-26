package net.k1ra.flight_data_recorder_dashboard.di.modules

import net.k1ra.flight_data_recorder_dashboard.viewmodel.TestViewModel
import org.koin.dsl.module

val ViewModelModule = module {
    factory { TestViewModel() }
}