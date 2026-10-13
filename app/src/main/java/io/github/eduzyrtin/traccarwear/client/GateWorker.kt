package io.github.eduzyrtin.traccarwear.client

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class GateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        TrackingGate.reconcile(applicationContext)
        return Result.success()
    }

    companion object {
        private const val ONCE = "gate-once"
        private const val PERIODIC = "gate-periodic"

        fun enqueue(context: Context, delay: Duration) {
            val request = OneTimeWorkRequestBuilder<GateWorker>()
                .setInitialDelay(delay.toJavaDuration())
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(ONCE, ExistingWorkPolicy.REPLACE, request)
        }

        // Red de seguridad por si se pierde algún aviso de Bluetooth o falta el permiso
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<GateWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
