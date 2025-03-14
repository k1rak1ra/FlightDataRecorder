package net.k1ra.flight_data_recorder_server.viewmodel.projects

import com.maxmind.geoip2.WebServiceClient
import net.k1ra.flight_data_recorder.model.projects.IpGeolocationData
import net.k1ra.flight_data_recorder_server.model.dao.projects.IpGeolocationDao
import net.k1ra.flight_data_recorder_server.model.dao.projects.toIpGeolocationData
import net.k1ra.flight_data_recorder_server.utils.Constants
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.InetAddress

object IpGeolocationViewModel {
    private val client: WebServiceClient = WebServiceClient.Builder(Constants.MAXMIND_ACCOUNT_ID, Constants.MAXMIND_API_KEY).host("geolite.info").build()

    fun save(ipStr: String) : IpGeolocationData = transaction {
        val data = getFromDb(ipStr)
        if (data == null) {
            try {
                val data = doLookup(ipStr)
                val dbRecord = IpGeolocationDao.new {
                    ipAddr = ipStr
                    city = data.city
                    state = data.state
                    country = data.country
                    latitude = data.latitude
                    longitude = data.longitude
                }
                return@transaction dbRecord.toIpGeolocationData()
            } catch (e: Exception) {
                return@transaction IpGeolocationData(
                    ipStr,
                    "ERROR",
                    "ERROR",
                    "ERROR",
                    "0",
                    "0"
                )
            }
        } else {
            return@transaction data
        }
    }

    private fun doLookup(ipStr: String) : IpGeolocationData {
        if (ipStr.startsWith("10.0") || ipStr.startsWith("192.168") || ipStr == "127.0.0.1")
            return IpGeolocationData(
                ipStr,
                "LAN",
                "LAN",
                "LAN",
                "0",
                "0"
            )

        val ipAddress = InetAddress.getByName(ipStr)
        val data = client.city(ipAddress)

        return IpGeolocationData(
            ipStr,
            data.city.name,
            data.leastSpecificSubdivision.name,
            data.country.name,
            data.location.latitude.toString(),
            data.location.longitude.toString()
        )
    }

    private fun getFromDb(ipStr: String): IpGeolocationData? = transaction {
        IpGeolocationDao.find { IpGeolocationDao.IpGeolocationTable.ipAddr eq ipStr }.firstOrNull()?.toIpGeolocationData()
    }

    fun get(ipStr: String) : IpGeolocationData {
        return getFromDb(ipStr) ?: save(ipStr)
    }
}