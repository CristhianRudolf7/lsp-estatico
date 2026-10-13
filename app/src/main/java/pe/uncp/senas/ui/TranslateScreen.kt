package pe.uncp.senas.ui

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import pe.uncp.senas.Guess
import pe.uncp.senas.R
import pe.uncp.senas.TranslationState
import pe.uncp.senas.TranslatorViewModel
import pe.uncp.senas.ui.theme.Palette
import java.util.concurrent.Executors

@Composable
fun TranslateScreen(
    vm: TranslatorViewModel,
    state: TranslationState,
    voiceOn: Boolean,
    bottomInset: androidx.compose.ui.unit.Dp,
) {
    var front by rememberSaveable { mutableStateOf(true) }

    Box(Modifier.fillMaxSize().background(Palette.Background)) {
        CameraPreview(front = front, onFrame = vm::analyze, modifier = Modifier.fillMaxSize())
        HandOverlay(state.hand, mirrored = front, modifier = Modifier.fillMaxSize())

        // Velos superior e inferior para que el texto se lea sobre cualquier fondo.
        Box(
            Modifier.fillMaxWidth().height(180.dp).align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Palette.Background.copy(alpha = 0.85f), Color.Transparent)))
        )
        Box(
            Modifier.fillMaxWidth().height(360.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Palette.Background.copy(alpha = 0.92f))))
        )

        TopBar(
            voiceOn = voiceOn,
            onToggleVoice = vm::toggleVoice,
            onFlip = { front = !front },
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
        )

        AnimatedVisibility(
            visible = state.ready && state.hand == null,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) { NoHandHint() }

        ResultCard(
            state = state,
            onSpeak = { label -> vm.sayLabel(label) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = bottomInset + 12.dp),
        )
    }
}

@Composable
private fun TopBar(voiceOn: Boolean, onToggleVoice: () -> Unit, onFlip: () -> Unit, modifier: Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Glass(shape = CircleShape, alpha = 0.6f) {
            Row(Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(Palette.Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_sign_language), null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("Señas", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
        Spacer(Modifier.weight(1f))
        GlassIconButton(
            icon = if (voiceOn) R.drawable.ic_volume_up else R.drawable.ic_volume_off,
            contentDescription = if (voiceOn) "Silenciar voz" else "Activar voz",
            onClick = onToggleVoice,
            active = voiceOn,
        )
        Spacer(Modifier.width(10.dp))
        GlassIconButton(R.drawable.ic_cameraswitch, "Cambiar cámara", onFlip)
    }
}

@Composable
private fun NoHandHint() {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val s by pulse.animateFloat(0.92f, 1.06f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "s")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(88.dp).scale(s).clip(CircleShape)
                .background(Palette.Violet.copy(alpha = 0.18f))
                .border(1.5.dp, Palette.Violet.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_front_hand), null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(14.dp))
        Glass(shape = CircleShape, alpha = 0.6f) {
            Text(
                "Muestra tu mano a la cámara",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun ResultCard(state: TranslationState, onSpeak: (String) -> Unit, modifier: Modifier) {
    val guess = state.guess
    val locked = state.stable != null && state.stable == guess?.label
    Glass(modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), alpha = 0.78f) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LetterTile(label = guess?.label, locked = locked, onClick = { guess?.let { onSpeak(it.label) } })
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                when {
                    state.error != null -> {
                        StatusPill("Error", Palette.Amber)
                        Spacer(Modifier.height(8.dp))
                        Text(state.error, style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
                    }
                    !state.ready -> {
                        StatusPill("Preparando", Palette.Muted)
                        Spacer(Modifier.height(8.dp))
                        Text("Cargando el modelo en tu teléfono…", style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
                    }
                    guess == null -> {
                        StatusPill(if (state.hand == null) "Esperando mano" else "Analizando", Palette.Muted)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Haz una letra del alfabeto y mantenla quieta un momento.",
                            style = MaterialTheme.typography.bodyMedium, color = Palette.Muted,
                        )
                    }
                    else -> GuessDetails(guess, locked, state.alternatives)
                }
            }
        }
    }
}

@Composable
private fun GuessDetails(guess: Guess, locked: Boolean, alternatives: List<Guess>) {
    if (locked) StatusPill("Reconocida", Palette.Teal) else StatusPill("Mantén la seña", Palette.Violet)
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            if (guess.label == " ") "Espacio" else "Letra ${guess.label}",
            style = MaterialTheme.typography.titleLarge, color = Color.White,
        )
        Spacer(Modifier.weight(1f))
        Text("${(guess.confidence * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = Palette.Muted)
    }
    Spacer(Modifier.height(8.dp))
    GradientBar(guess.confidence, Modifier.fillMaxWidth())
    if (alternatives.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("También:", style = MaterialTheme.typography.labelMedium, color = Palette.Muted)
            alternatives.forEach { alt ->
                Text(
                    "${if (alt.label == " ") "␣" else alt.label} ${(alt.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Palette.Text,
                    modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun LetterTile(label: String?, locked: Boolean, onClick: () -> Unit) {
    val border = if (locked) Palette.Accent else Brush.linearGradient(listOf(Palette.Outline, Palette.Outline))
    Box(
        Modifier
            .size(112.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Palette.Surface)
            .border(2.dp, border, RoundedCornerShape(26.dp))
            .clickable(enabled = label != null, role = Role.Button, onClickLabel = "Escuchar", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (locked) {
            Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Palette.Violet.copy(alpha = 0.35f), Color.Transparent))))
        }
        AnimatedContent(
            targetState = label,
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith
                    (slideOutVertically { -it / 2 } + fadeOut())
            },
            label = "letter",
        ) { l ->
            when (l) {
                null -> Text("?", style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp), color = Palette.Outline)
                " " -> Text("␣", style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp), color = Color.White)
                else -> Text(
                    l,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp, lineHeight = 72.sp),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(if (locked) 1f else 0.75f),
                )
            }
        }
        if (label != null) {
            Icon(
                painterResource(R.drawable.ic_volume_up), null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(16.dp),
            )
        }
    }
}

@Composable
private fun CameraPreview(front: Boolean, onFrame: (androidx.camera.core.ImageProxy) -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(front, lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        providerFuture.addListener({
            val p = providerFuture.get().also { provider = it }
            // 4:3 en vista previa y análisis para que el esqueleto coincida con la imagen.
            val selector43 = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()
            val preview = Preview.Builder().setResolutionSelector(selector43).build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                        .setResolutionStrategy(
                            ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                        )
                        .build()
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also { it.setAnalyzer(executor, onFrame) }
            val camera = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            val chosen = if (p.hasCamera(camera)) camera else CameraSelector.DEFAULT_BACK_CAMERA
            p.unbindAll()
            p.bindToLifecycle(lifecycleOwner, chosen, preview, analysis)
        }, ContextCompat.getMainExecutor(context))
        onDispose { provider?.unbindAll() }
    }
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }

    AndroidView(factory = { previewView }, modifier = modifier)
}
