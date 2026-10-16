package io.github.eduzyrtin.traccarwear.client

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Avisos de Bluetooth y de arranque. Las conexiones de cualquier dispositivo Bluetooth (auriculares, etc.)
 * llegan aquí, así que lo primero es salir sin hacer nada si no se está rastreando con la pausa activada.
 */
class GateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val appContext = context.applicationContext
        val pending = goAsync()
        (appContext as ClientApplication).scope.launch {
            try {
                handle(appContext, action)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(context: Context, action: String) {
        val settings = SettingsStore.read(context)
        if (!settings.trackingWanted) {
            GateWorker.sync(context, settings)
            return
        }
        when (action) {
            // Al desconectarse el móvil se intenta arrancar enseguida, que es cuando Android lo permite con más
            // probabilidad; si Wear OS tarda en confirmarlo, lo reintenta el trabajo de respaldo.
            // Nota: no se ha verificado en un reloj real que el arranque desde segundo plano sea siempre
            // posible; la exención de batería (ver BatteryOptimization) es lo que lo hace fiable.
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> if (settings.pauseNearPhone) startIfPhoneAway(context)
            // Al volver el móvil se para con un pequeño margen para no cortar por un rebote del Bluetooth
            BluetoothDevice.ACTION_ACL_CONNECTED -> if (settings.pauseNearPhone) GateWorker.enqueue(context, NEAR_DELAY)
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                GateWorker.sync(context, settings)
                GateWorker.enqueue(context, Duration.ZERO)
            }
        }
    }

    private suspend fun startIfPhoneAway(context: Context) {
        repeat(AWAY_CHECKS) {
            if (!PhoneProximity.isNearby(context)) {
                TrackingGate.reconcile(context)
                return
            }
            delay(AWAY_CHECK_INTERVAL)
        }
        GateWorker.enqueue(context, AWAY_RECHECK_DELAY)
    }

    private companion object {
        const val AWAY_CHECKS = 3
        val AWAY_CHECK_INTERVAL = 2.seconds
        val AWAY_RECHECK_DELAY = 60.seconds
        val NEAR_DELAY = 15.seconds
    }
}
