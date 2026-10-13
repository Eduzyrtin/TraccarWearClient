package io.github.eduzyrtin.traccarwear.client

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
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
 * Sin la exención de optimización de batería, Android puede impedir que el GPS arranque
 * en segundo plano cuando el reloj se aleja del teléfono. Se pide una sola vez, si el reloj
 * tiene la pantalla de ajustes correspondiente.
 */
object BatteryOptimization {
    private const val PREFS = "battery_optimization"
    private const val PROMPTED = "prompted"

    fun promptOnce(context: Context) {
        val power = context.getSystemService<PowerManager>() ?: return
        if (power.isIgnoringBatteryOptimizations(context.packageName)) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(PROMPTED, false)) return
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        prefs.edit().putBoolean(PROMPTED, true).apply()
    }
}
