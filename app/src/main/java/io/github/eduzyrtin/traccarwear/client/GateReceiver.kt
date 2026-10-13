package io.github.eduzyrtin.traccarwear.client

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class GateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                // Android solo permite arrancar el servicio de ubicación desde segundo plano justo después
                // de este aviso, así que se intenta aquí mismo en cuanto Wear OS confirma que el móvil no
                // está cerca. Si tarda más, lo vuelve a comprobar el worker (con la exención de batería).
                val pending = goAsync()
                (context.applicationContext as ClientApplication).scope.launch {
                    try {
                        startIfPhoneAway(context.applicationContext)
                    } finally {
                        pending.finish()
                    }
                }
            }
            // Al volver el móvil se para con un pequeño margen para no cortar por un rebote del Bluetooth
            BluetoothDevice.ACTION_ACL_CONNECTED -> GateWorker.enqueue(context, NEAR_DELAY)
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                GateWorker.schedulePeriodic(context)
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
