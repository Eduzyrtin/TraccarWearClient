package io.github.eduzyrtin.traccarwear.client

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.edit
import androidx.core.content.getSystemService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.traccar.client.hasPermission

/**
 * Decide si el SDK debe estar transmitiendo: el usuario lo quiere activo y el teléfono no está cerca
 * (si la pausa automática está activada). Es idempotente; se llama ante cualquier cambio.
 */
object TrackingGate {
    private const val TAG = "TrackingGate"
    private val mutex = Mutex()

    suspend fun reconcile(context: Context) = mutex.withLock {
        val settings = SettingsStore.read(context)
        // La pausa por cercanía solo existe mientras se rastrea: si no, se cancela el trabajo periódico
        GateWorker.sync(context, settings)
        // Sin rastreo y sin SDK cargado no hay nada que parar: no se arranca el SDK (base de datos, red) solo para comprobarlo
        if (!settings.trackingWanted && TrackerHolder.tracker.value == null) return@withLock
        val tracker = TrackerHolder.current(context) ?: return@withLock
        val phoneNearby = settings.pauseNearPhone && settings.trackingWanted && PhoneProximity.isNearby(context)
        val shouldRun = settings.trackingWanted && !phoneNearby
        if (settings.pausedByPhone != phoneNearby) {
            SettingsStore.update(context) { it.copy(pausedByPhone = phoneNearby) }
        }
        val running = tracker.state.value.enabled
        when {
            shouldRun && !running ->
                if (hasLocationPermissions(context)) tracker.start()
                else Log.w(TAG, "Faltan permisos de ubicación: abre la app para concederlos")
            !shouldRun && running -> tracker.stop()
        }
        Log.i(TAG, "wanted=${settings.trackingWanted} nearby=$phoneNearby running=${tracker.state.value.enabled}")
    }

    private fun hasLocationPermissions(context: Context) =
        hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
            hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
}

/**
 * Android restringe arrancar un servicio en primer plano (el del GPS) desde segundo plano, que es lo que
 * ocurre al alejarse del teléfono. Estar fuera de la optimización de batería es una de las exenciones
 * que lo permiten, así que se pide una sola vez. Cuesta algo de batería en reposo (Doze menos estricto)
 * y es el compromiso necesario para que la pausa por cercanía arranque sola.
 */
object BatteryOptimization {
    private const val PREFS = "battery_optimization"
    private const val PROMPTED = "prompted"
    private const val TAG = "BatteryOptimization"

    fun promptOnce(context: Context) {
        val power = context.getSystemService<PowerManager>() ?: return
        if (power.isIgnoringBatteryOptimizations(context.packageName)) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(PROMPTED, false)) return
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Este reloj no tiene la pantalla de optimización de batería", e)
        }
        prefs.edit { putBoolean(PROMPTED, true) }
    }
}
