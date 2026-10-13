package io.github.eduzyrtin.traccarwear.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import io.github.eduzyrtin.traccarwear.client.ui.ClientViewModel
import io.github.eduzyrtin.traccarwear.client.ui.MainScreen
import io.github.eduzyrtin.traccarwear.client.ui.SettingsScreen
import io.github.eduzyrtin.traccarwear.client.ui.TextInputScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ClientApp() }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { TrackingGate.reconcile(applicationContext) }
    }
}

private object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val EDIT_SERVER = "edit_server"
    const val EDIT_DEVICE_ID = "edit_device_id"
}

@Composable
private fun ClientApp(viewModel: ClientViewModel = viewModel()) {
    val navController = rememberSwipeDismissableNavController()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    MaterialTheme {
        AppScaffold {
            SwipeDismissableNavHost(navController = navController, startDestination = Routes.MAIN) {
                composable(Routes.MAIN) {
                    MainScreen(viewModel, onOpenSettings = { navController.navigate(Routes.SETTINGS) })
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        viewModel,
                        onEditServer = { navController.navigate(Routes.EDIT_SERVER) },
                        onEditDeviceId = { navController.navigate(Routes.EDIT_DEVICE_ID) },
                    )
                }
                composable(Routes.EDIT_SERVER) {
                    TextInputScreen(
                        title = stringResource(R.string.server_url),
                        initialValue = settings.serverUrl,
                        hint = stringResource(R.string.server_url_hint),
                        keyboardType = KeyboardType.Uri,
                        onSave = { value ->
                            viewModel.updateSettings { it.copy(serverUrl = value.trim()) }
                            navController.popBackStack()
                        },
                    )
                }
                composable(Routes.EDIT_DEVICE_ID) {
                    TextInputScreen(
                        title = stringResource(R.string.device_id),
                        initialValue = settings.deviceId,
                        hint = stringResource(R.string.device_id_hint),
                        keyboardType = KeyboardType.Ascii,
                        onSave = { value ->
                            viewModel.updateSettings { it.copy(deviceId = value.trim()) }
                            navController.popBackStack()
                        },
                    )
                }
            }
        }
    }
}
