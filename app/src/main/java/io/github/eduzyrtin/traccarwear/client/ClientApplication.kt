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
        GateWorker.schedulePeriodic(this)
        scope.launch {
            SettingsStore.ensureDeviceId(this@ClientApplication)
            TrackingGate.reconcile(this@ClientApplication)
        }
    }
}
