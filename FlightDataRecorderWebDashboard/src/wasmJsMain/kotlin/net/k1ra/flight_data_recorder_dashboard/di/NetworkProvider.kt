package net.k1ra.flight_data_recorder_dashboard.di

import net.k1ra.flight_data_recorder.feature.logging.Log
import net.k1ra.flight_data_recorder.model.authentication.ClientUserData
import net.k1ra.flight_data_recorder_dashboard.BaseUrlGetter
import net.k1ra.flight_data_recorder_dashboard.utils.USERDATA_KEY
import net.k1ra.hoodies_network_kmm.HoodiesNetworkClient
import net.k1ra.hoodies_network_kmm.interceptor.Interceptor
import net.k1ra.hoodies_network_kmm.request.CancellableMutableRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import net.k1ra.hoodies_network_kmm.request.RetryableCancellableMutableRequest
import net.k1ra.hoodies_network_kmm.result.HttpClientError
import net.k1ra.hoodies_network_kmm.result.Result
import net.k1ra.sharedprefkmm.SharedPreferences
import org.koin.dsl.module

var forceLogoutHook = {}

val NetworkProvider = module {
    single {
        HoodiesNetworkClient.Builder().apply {
            baseUrl = BaseUrlGetter.getBaseUrl()
            maxRetryLimit = 0
            interceptors = listOf(object: Interceptor() {
                override suspend fun interceptRequest(identifier: String, cancellableMutableRequest: CancellableMutableRequest) {
                    val sharedPref: SharedPreferences by inject()
                    val data = sharedPref.get<ClientUserData>(USERDATA_KEY) ?: return

                    cancellableMutableRequest.request.headers["Authorization"] = "Bearer ${data.uid}:${data.sessionToken}"
                }

                override suspend fun interceptResponse(result: Result<*>, response: NetworkResponse) {
                    Log.d("Network-InterceptResponse", "Request to ${response.request.url} completed in ${response.networkTimeMs}ms")
                }

                override suspend fun interceptError(error: HttpClientError, retryableCancellableMutableRequest: RetryableCancellableMutableRequest, autoRetryAttempts: Int) {
                    Log.d(
                        "Network-InterceptError",
                        "Connection to ${retryableCancellableMutableRequest.request.url} failed with ${error.code} and message ${error.message}"
                    )

                    if (error.code == 401) {
                        val sharedPref: SharedPreferences by inject()

                        userDataInstance.value = null
                        sharedPref.delete(USERDATA_KEY)
                        forceLogoutHook.invoke()
                    }
                }
            })
        }.build()
    }
}