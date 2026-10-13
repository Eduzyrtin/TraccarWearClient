package io.github.eduzyrtin.traccarwear.client.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.eduzyrtin.traccarwear.client.ClientSettings
import io.github.eduzyrtin.traccarwear.client.R
import io.github.eduzyrtin.traccarwear.client.SettingsStore
import io.github.eduzyrtin.traccarwear.client.TrackerHolder
import io.github.eduzyrtin.traccarwear.client.TrackingGate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.traccar.client.requestPosition
import org.traccar.client.State as TrackerState

class ClientViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application get() = getApplication()

    val settings: StateFlow<ClientSettings> = SettingsStore.flow(application)
        .stateIn(viewModelScope, SharingStarted.Eagerly, ClientSettings())

    @OptIn(ExperimentalCoroutinesApi::class)
    val trackerState: StateFlow<TrackerState?> = TrackerHolder.tracker
        .flatMapLatest { it?.state ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { TrackerHolder.current(application) }
    }

    fun setTracking(wanted: Boolean) {
        viewModelScope.launch {
            SettingsStore.update(app) { it.copy(trackingWanted = wanted) }
            TrackingGate.reconcile(app)
        }
    }

    fun updateSettings(transform: (ClientSettings) -> ClientSettings) {
        viewModelScope.launch {
            val updated = SettingsStore.update(app, transform)
            TrackerHolder.configure(app, updated)
            TrackingGate.reconcile(app)
        }
    }

    fun sendPositionNow() {
        viewModelScope.launch {
            val sent = try {
                TrackerHolder.current(app)?.requestPosition(app) == true
            } catch (_: IllegalStateException) {
                false // permiso de ubicación denegado
            }
            Toast.makeText(app, if (sent) R.string.position_sent else R.string.position_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
