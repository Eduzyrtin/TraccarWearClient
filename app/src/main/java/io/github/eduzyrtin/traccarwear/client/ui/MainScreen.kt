package io.github.eduzyrtin.traccarwear.client.ui

import android.Manifest
import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.eduzyrtin.traccarwear.client.BatteryOptimization
import io.github.eduzyrtin.traccarwear.client.ClientSettings
import io.github.eduzyrtin.traccarwear.client.R
import kotlinx.coroutines.launch
import org.traccar.client.ensurePermissions
import org.traccar.client.hasPermission
import org.traccar.client.State as TrackerState

@Composable
fun MainScreen(viewModel: ClientViewModel, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val trackerState by viewModel.trackerState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Permisos de ubicación (los pide el SDK), exención de batería y, por último, activar
    fun startTracking() = scope.launch {
        if (ensurePermissions(context)) {
            BatteryOptimization.promptOnce(context)
            viewModel.setTracking(true)
        }
    }

    // Sin este permiso no llegan los avisos de Bluetooth; la pausa sigue funcionando, pero con más retraso
    val bluetoothPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { startTracking() }

    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item {
                ListHeader { Text(stringResource(R.string.app_name)) }
            }
            item {
                Text(
                    text = statusText(settings, trackerState),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            trackerState?.lastAcceptedLocation?.let { position ->
                item {
                    Text(
                        text = stringResource(
                            R.string.last_position,
                            DateUtils.getRelativeTimeSpanString(
                                position.time,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS,
                            ),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        when {
                            settings.trackingWanted -> viewModel.setTracking(false)
                            needsBluetoothPermission(context, settings) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                                bluetoothPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                            else -> startTracking()
                        }
                    },
                    enabled = settings.isConfigured,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(if (settings.trackingWanted) R.string.stop else R.string.start)) },
                )
            }
            item {
                Button(
                    onClick = viewModel::sendPositionNow,
                    enabled = settings.isConfigured,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.send_now)) },
                )
            }
            item {
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.settings)) },
                )
            }
        }
    }
}

@Composable
private fun statusText(settings: ClientSettings, state: TrackerState?): String = stringResource(
    when {
        !settings.isConfigured -> R.string.status_not_configured
        state?.enabled == true -> R.string.status_tracking
        settings.trackingWanted && settings.pausedByPhone -> R.string.status_paused_phone
        settings.trackingWanted -> R.string.status_waiting
        else -> R.string.status_stopped
    },
)

// Hasta Android 11 basta BLUETOOTH, que se concede al instalar; BLUETOOTH_CONNECT solo existe desde Android 12
private fun needsBluetoothPermission(context: Context, settings: ClientSettings) =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        settings.pauseNearPhone &&
        !hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
