package pe.uncp.senas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.uncp.senas.ui.AlphabetScreen
import pe.uncp.senas.ui.FloatingNav
import pe.uncp.senas.ui.PermissionScreen
import pe.uncp.senas.ui.Tab
import pe.uncp.senas.ui.TranslateScreen
import pe.uncp.senas.ui.theme.Palette
import pe.uncp.senas.ui.theme.SenasTheme

class MainActivity : ComponentActivity() {
    private val vm: TranslatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Palette.Background.copy(alpha = 0f).toArgb()),
        )
        setContent { SenasTheme { App(vm) } }
    }
}

@Composable
private fun App(vm: TranslatorViewModel) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    var tab by rememberSaveable { mutableStateOf(Tab.Translate) }
    var granted by rememberSaveable { mutableStateOf(hasCamera(context)) }
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        asked = true
    }

    // Si el usuario concede el permiso desde Ajustes, lo detectamos al volver.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = hasCamera(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val state by vm.state.collectAsStateWithLifecycle()
    val voiceOn by vm.voiceOn.collectAsStateWithLifecycle()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentInset = navBottom + 84.dp

    Box(Modifier.fillMaxSize().background(Palette.Background)) {
        Crossfade(tab, label = "tab") { current ->
            when (current) {
                Tab.Translate -> if (granted) {
                    TranslateScreen(vm, state, voiceOn, bottomInset = contentInset)
                } else {
                    val permanentlyDenied = asked && !activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    PermissionScreen(
                        permanentlyDenied = permanentlyDenied,
                        onRequest = {
                            if (permanentlyDenied) {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                                )
                            } else {
                                launcher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        bottomInset = contentInset,
                    )
                }
                Tab.Alphabet -> AlphabetScreen(onSpeak = vm::say, bottomInset = contentInset)
            }
        }
        FloatingNav(
            current = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBottom + 14.dp),
        )
    }
}

private fun hasCamera(context: android.content.Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
