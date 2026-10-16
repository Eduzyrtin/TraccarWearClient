package io.github.eduzyrtin.traccarwear.client

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ClientApplication : Application() {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            SettingsStore.ensureDeviceId(this@ClientApplication)
            // Solo en debug (valores de test-defaults.properties); en release están vacíos.
            // Se aplican únicamente si todavía no hay servidor configurado.
            if (BuildConfig.TEST_SERVER.isNotEmpty()) {
                SettingsStore.update(this@ClientApplication) {
                    if (it.serverUrl.isBlank()) {
                        it.copy(
                            serverUrl = BuildConfig.TEST_SERVER,
                            deviceId = BuildConfig.TEST_DEVICE_ID.ifEmpty { it.deviceId },
                        )
                    } else {
                        it
                    }
                }
            }
            // Solo hace trabajo real si se estaba rastreando (ver TrackingGate); en reposo no carga el SDK
            TrackingGate.reconcile(this@ClientApplication)
        }
    }
}
