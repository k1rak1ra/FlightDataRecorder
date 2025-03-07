package net.k1ra.flight_data_recorder_dashboard.di

import androidx.compose.runtime.mutableStateOf
import net.k1ra.flight_data_recorder.model.authentication.ClientUserData
import net.k1ra.flight_data_recorder_dashboard.utils.SHAREDPREF_COLLECTION_NAME
import net.k1ra.sharedprefkmm.SharedPreferences
import org.koin.dsl.module

val userDataInstance = mutableStateOf(null as ClientUserData?)

val UserDataProvider = module {
    single { SharedPreferences(SHAREDPREF_COLLECTION_NAME) }
}