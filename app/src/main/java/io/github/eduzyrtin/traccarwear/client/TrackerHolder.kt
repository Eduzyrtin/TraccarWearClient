package io.github.eduzyrtin.traccarwear.client

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.traccar.client.Tracker
import org.traccar.client.sharedTracker

/**
 * Acceso único al Tracker del SDK. Cambiar la configuración crea una instancia nueva
 * (Tracker.updateConfig), así que la interfaz observa [tracker] en vez de guardar la instancia.
 */
object TrackerHolder {
    private val mutex = Mutex()
    private val _tracker = MutableStateFlow<Tracker?>(null)
    val tracker: StateFlow<Tracker?> = _tracker

    suspend fun current(context: Context): Tracker? {
        _tracker.value?.let { return it }
        return mutex.withLock {
            _tracker.value ?: run {
                val settings = SettingsStore.read(context)
                // Primero lo que el SDK tenga guardado; si no hay nada, la configuración de la app
                sharedTracker() ?: settings.takeIf { it.isConfigured }?.let {
                    sharedTracker(it.toConfig(context.getString(R.string.tracking_notification)))
                }
            }.also { _tracker.value = it }
        }
    }

    suspend fun configure(context: Context, settings: ClientSettings) = mutex.withLock {
        if (!settings.isConfigured) return@withLock
        val config = settings.toConfig(context.getString(R.string.tracking_notification))
        val existing = _tracker.value ?: sharedTracker()
        _tracker.value = when {
            existing == null -> sharedTracker(config)
            existing.config == config -> existing
            else -> existing.updateConfig(config)
        }
    }
}
