package net.k1ra.flight_data_recorder.feature.batching

import androidx.compose.ui.text.intl.Locale
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.k1ra.flight_data_recorder.feature.config.FlightDataRecorderConfig
import net.k1ra.flight_data_recorder.feature.cryptography.CipherMode
import net.k1ra.flight_data_recorder.feature.cryptography.Cryptography
import net.k1ra.flight_data_recorder.feature.database.LogDao
import net.k1ra.flight_data_recorder.feature.database.LogEntity
import net.k1ra.flight_data_recorder.feature.database.buildLogDatabase
import net.k1ra.flight_data_recorder.feature.deviceinfo.DeviceInfoGetter
import net.k1ra.flight_data_recorder.feature.logging.LogLevels
import net.k1ra.flight_data_recorder.feature.model.FlightDataRecorderMetadata
import net.k1ra.flight_data_recorder.feature.model.toJsonPrimitive
import net.k1ra.hoodies_network_kmm.HoodiesNetworkClient
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.sharedprefkmm.SharedPreferences
import net.k1ra.sharedprefkmm.util.IODispatcher
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

internal class BatchLoggingManager(val appKey: String) {
    private val collection = "$appKey-FDRLogs"
    private val prefs = SharedPreferences(collection)
    private val crypto = Cryptography(prefs)
    private var db: LogDao? = null
    private val dbMutex = Mutex()
    val writeMutex = Mutex()
    private var deviceUniqueId: String? = null
    private val httpClient = HoodiesNetworkClient.Builder().apply {
        defaultHeaders = mapOf("Authorization" to "Bearer $appKey")
        retryOnConnectionFailure = true
        maxRetryLimit = 10
        retryDelayDuration = 10.seconds
    }.build()

    private suspend fun getDb(): LogDao {
        db?.let { return it }
        return dbMutex.withLock {
            db ?: buildLogDatabase(collection).getDao().also { db = it }
        }
    }

    //Start log line count watchdog
    private val watchdogJob = CoroutineScope(IODispatcher).launch {
        while (true) {
            try {
                if (getDb().countAll() > FlightDataRecorderConfig.batchLimit) {
                    //Fetch stored logs and get ID of last line
                    val allLogs = getDb().getAll()
                    val maxId = allLogs.last().id

                    //Decrypt all logs and convert to JsonArray
                    val jsonArr = JsonArray(allLogs.map { Json.parseToJsonElement(crypto.runAes(it.data, it.iv, CipherMode.DECRYPT).decodeToString()) })

                    var logServer = FlightDataRecorderConfig.logServer ?: ""
                    if (logServer.last() == '/')
                        logServer = logServer.substring(0, logServer.length - 1)

                    //Upload to server and delete from DB if uploaded successfully
                    if (httpClient.post<Unit, ByteArray>("$logServer/client/batchupload", jsonArr.toString().encodeToByteArray()) is Success)
                        writeMutex.withLock { getDb().deleteBeforeId(maxId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                writeMutex.withLock { getDb().deleteBeforeId(Long.MAX_VALUE) }
            }

            //Wait a while before repeating the loop
            delay(30.seconds)
        }
    }

    fun consumeLog(tag: String, message: String, level: LogLevels, additionalMetadata: Map<String, FlightDataRecorderMetadata>) = CoroutineScope(IODispatcher).launch {
        //Convert the log line and all additional metadata to JSON
        val logJson = convertLogToJsonString(tag, message, level, additionalMetadata)

        //Encrypt the log line
        var iv = crypto.generateIv()

        while(getDb().getByIv(iv) > 0)
            iv = crypto.generateIv()

        val logEncrypted = crypto.runAes(logJson.encodeToByteArray(), iv, CipherMode.ENCRYPT)

        //Insert the log line into the DB
        writeMutex.withLock { getDb().insert(LogEntity(data = logEncrypted, iv = iv)) }

        //Start watchdog thread if inactive
        if (!watchdogJob.isActive)
            watchdogJob.start()
    }

    private suspend fun convertLogToJsonString(tag: String, message: String, level: LogLevels, additionalMetadata: Map<String, FlightDataRecorderMetadata>) : String {
        val elements = mutableMapOf<String, JsonElement>()

        elements.putAll(FlightDataRecorderConfig.additionalMetadata.mapValues { it.value.toJsonPrimitive() })
        elements.putAll(additionalMetadata.mapValues { it.value.toJsonPrimitive() })

        elements["Tag"] = JsonPrimitive(tag)
        elements["Message"] = JsonPrimitive(message)
        elements["LogLevel"] = JsonPrimitive(level.name)
        elements["DateTime"] = JsonPrimitive(Clock.System.now().toEpochMilliseconds())
        elements["DeviceID"] = JsonPrimitive(getUniqueDeviceId())
        elements["Language"] = JsonPrimitive(Locale.current.language)
        elements["Region"] = JsonPrimitive(Locale.current.region)
        elements["OSType"] = JsonPrimitive(DeviceInfoGetter.getOsType())
        elements["OSVersion"] = JsonPrimitive(DeviceInfoGetter.getOsVersion())
        elements["NumCpuCores"] = JsonPrimitive(DeviceInfoGetter.getNumCpuCores())
        elements["SystemMemoryMb"] = JsonPrimitive(DeviceInfoGetter.getAvailableMemoryMb())
        elements["SystemProductName"] = JsonPrimitive(DeviceInfoGetter.getSystemProductName())

        return JsonObject(elements).toString()
    }

    private suspend fun getUniqueDeviceId() : String {
        if (deviceUniqueId != null)
            return deviceUniqueId as String

        deviceUniqueId = prefs.get<String>("deviceUniqueId")

        if (deviceUniqueId == null) {
            deviceUniqueId = uuid4().toString()
            prefs.set("deviceUniqueId", deviceUniqueId)
        }

        return deviceUniqueId as String
    }
}