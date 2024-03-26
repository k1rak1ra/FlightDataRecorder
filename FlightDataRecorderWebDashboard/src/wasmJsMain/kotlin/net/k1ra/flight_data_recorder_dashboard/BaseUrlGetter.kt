package net.k1ra.flight_data_recorder_dashboard

import kotlinx.browser.window

object BaseUrlGetter {
    fun getBaseUrl() : String {
        return window.location.href.replace("index.html","")
    }
}