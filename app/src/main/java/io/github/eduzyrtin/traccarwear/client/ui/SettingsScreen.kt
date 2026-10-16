package io.github.eduzyrtin.traccarwear.client.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import io.github.eduzyrtin.traccarwear.client.R
import org.traccar.client.Accuracy

private val INTERVALS = listOf(30, 60, 120, 300, 600, 1800)
private val DISTANCES = listOf(0, 25, 50, 75, 100, 250, 500)

@Composable
fun SettingsScreen(viewModel: ClientViewModel, onEditServer: () -> Unit, onEditDeviceId: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item {
                ListHeader { Text(stringResource(R.string.settings)) }
            }
            item {
                SettingButton(
                    label = stringResource(R.string.server_url),
                    value = settings.serverUrl.ifBlank { stringResource(R.string.not_set) },
                    onClick = onEditServer,
                )
            }
            item {
                SettingButton(stringResource(R.string.device_id), settings.deviceId, onEditDeviceId)
            }
            item {
                SwitchButton(
                    checked = settings.pauseNearPhone,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(pauseNearPhone = checked) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pause_near_phone)) },
                    secondaryLabel = { Text(stringResource(R.string.pause_near_phone_summary)) },
                )
            }
            item {
                SwitchButton(
                    checked = settings.stopDetection,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(stopDetection = checked) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.stop_detection)) },
                    secondaryLabel = { Text(stringResource(R.string.stop_detection_summary)) },
                )
            }
            item {
                SettingButton(stringResource(R.string.accuracy), stringResource(settings.accuracy.label())) {
                    viewModel.updateSettings { it.copy(accuracy = Accuracy.entries.next(it.accuracy)) }
                }
            }
            item {
                SettingButton(stringResource(R.string.distance), distanceLabel(settings.distanceMeters)) {
                    viewModel.updateSettings { it.copy(distanceMeters = DISTANCES.next(it.distanceMeters)) }
                }
            }
            item {
                // El SDK ignora el intervalo cuando hay distancia mínima (igual que la app oficial)
                SettingButton(
                    label = stringResource(R.string.interval),
                    value = intervalLabel(settings.intervalSeconds) +
                        if (settings.distanceMeters > 0) " · " + stringResource(R.string.interval_unused) else "",
                ) {
                    viewModel.updateSettings { it.copy(intervalSeconds = INTERVALS.next(it.intervalSeconds)) }
                }
            }
        }
    }
}

@Composable
private fun SettingButton(label: String, value: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(),
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        secondaryLabel = { Text(value, maxLines = 3) },
    )
}

private fun <T> List<T>.next(current: T): T = this[(indexOf(current) + 1) % size]

@StringRes
private fun Accuracy.label(): Int = when (this) {
    Accuracy.HIGHEST -> R.string.accuracy_highest
    Accuracy.HIGH -> R.string.accuracy_high
    Accuracy.MEDIUM -> R.string.accuracy_medium
    Accuracy.LOW -> R.string.accuracy_low
}

@Composable
private fun distanceLabel(meters: Int): String =
    if (meters == 0) stringResource(R.string.off) else stringResource(R.string.meters, meters)

@Composable
private fun intervalLabel(seconds: Int): String =
    if (seconds < 60) stringResource(R.string.seconds, seconds) else stringResource(R.string.minutes, seconds / 60)
