package io.github.eduzyrtin.traccarwear.client

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.traccar.client.Accuracy
import org.traccar.client.Config
import org.traccar.client.LocationConfig
import org.traccar.client.NotificationConfig
import kotlin.random.Random

data class ClientSettings(
    val serverUrl: String = "",
    val deviceId: String = "",
    val accuracy: Accuracy = Accuracy.MEDIUM,
    val intervalSeconds: Int = 300,
    val distanceMeters: Int = 75,
    // El SDK pausa el GPS cuando detecta que no te mueves (como la app oficial). Es lo que más batería ahorra.
    val stopDetection: Boolean = true,
    val pauseNearPhone: Boolean = true,
    // Lo que quiere el usuario. El tracking real puede estar parado aunque esto sea true (móvil cerca).
    val trackingWanted: Boolean = false,
    // Estado, no ajuste: la última vez que se comprobó, el tracking estaba en pausa por tener el móvil cerca.
    val pausedByPhone: Boolean = false,
) {
    val isConfigured: Boolean get() = serverUrl.isNotBlank() && deviceId.isNotBlank()

    fun toConfig(notificationText: String) = Config(
        serverUrl = serverUrl.trim(),
        deviceId = deviceId.trim(),
        location = LocationConfig(
            accuracy = accuracy,
            distanceMeters = distanceMeters,
            intervalSeconds = intervalSeconds,
            stopDetection = stopDetection,
        ),
        notification = NotificationConfig(text = notificationText),
    )
}

private val Context.dataStore by preferencesDataStore("client_settings")

object SettingsStore {
    private val SERVER_URL = stringPreferencesKey("server_url")
    private val DEVICE_ID = stringPreferencesKey("device_id")
    private val ACCURACY = stringPreferencesKey("accuracy")
    private val INTERVAL = intPreferencesKey("interval_seconds")
    private val DISTANCE = intPreferencesKey("distance_meters")
    private val STOP_DETECTION = booleanPreferencesKey("stop_detection")
    private val PAUSE_NEAR_PHONE = booleanPreferencesKey("pause_near_phone")
    private val TRACKING_WANTED = booleanPreferencesKey("tracking_wanted")
    private val PAUSED_BY_PHONE = booleanPreferencesKey("paused_by_phone")

    fun flow(context: Context): Flow<ClientSettings> = context.dataStore.data.map { it.toSettings() }

    suspend fun read(context: Context): ClientSettings = flow(context).first()

    suspend fun update(context: Context, transform: (ClientSettings) -> ClientSettings): ClientSettings {
        lateinit var result: ClientSettings
        context.dataStore.edit { prefs ->
            result = transform(prefs.toSettings())
            prefs[SERVER_URL] = result.serverUrl
            prefs[DEVICE_ID] = result.deviceId
            prefs[ACCURACY] = result.accuracy.name
            prefs[INTERVAL] = result.intervalSeconds
            prefs[DISTANCE] = result.distanceMeters
            prefs[STOP_DETECTION] = result.stopDetection
            prefs[PAUSE_NEAR_PHONE] = result.pauseNearPhone
            prefs[TRACKING_WANTED] = result.trackingWanted
            prefs[PAUSED_BY_PHONE] = result.pausedByPhone
        }
        return result
    }

    // Igual que la app oficial: identificador aleatorio de 8 cifras la primera vez
    suspend fun ensureDeviceId(context: Context) {
        context.dataStore.edit { prefs ->
            if (prefs[DEVICE_ID].isNullOrBlank()) {
                prefs[DEVICE_ID] = (Random.nextInt(90_000_000) + 10_000_000).toString()
            }
        }
    }

    private fun Preferences.toSettings() = ClientSettings(
        serverUrl = this[SERVER_URL] ?: "",
        deviceId = this[DEVICE_ID] ?: "",
        accuracy = this[ACCURACY]?.let { name -> Accuracy.entries.find { it.name == name } } ?: Accuracy.MEDIUM,
        intervalSeconds = this[INTERVAL] ?: 300,
        distanceMeters = this[DISTANCE] ?: 75,
        stopDetection = this[STOP_DETECTION] ?: true,
        pauseNearPhone = this[PAUSE_NEAR_PHONE] ?: true,
        trackingWanted = this[TRACKING_WANTED] ?: false,
        pausedByPhone = this[PAUSED_BY_PHONE] ?: false,
    )
}
