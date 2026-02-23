package com.twodgfxapp.ui.effect

import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.twodgfxapp.input.InputManager
import com.twodgfxapp.navigation.Screen
import com.twodgfxapp.renderer.GlSurfaceRenderer
import kotlinx.coroutines.launch

@Composable
fun EffectScreen(
    navController: NavController,
    viewModel:     EffectViewModel = viewModel(),
) {
    val uiState             by viewModel.uiState.collectAsStateWithLifecycle()
    val isQuickSwitchVisible by viewModel.isQuickSwitchVisible.collectAsStateWithLifecycle()
    val snackbarHostState   = remember { SnackbarHostState() }
    val context             = LocalContext.current
    val scope               = rememberCoroutineScope()

    // ── Error state: show snackbar then navigate back ─────────────────────
    LaunchedEffect(uiState) {
        if (uiState is EffectUiState.Error) {
            val msg = (uiState as EffectUiState.Error).message
            snackbarHostState.showSnackbar(msg)
            navController.popBackStack(Screen.Menu.route, inclusive = false)
        }
    }

    // ── Back handler: return to menu ──────────────────────────────────────
    BackHandler {
        navController.popBackStack(Screen.Menu.route, inclusive = false)
    }

    // ── InputManager lifecycle ────────────────────────────────────────────
    val inputManager = remember {
        InputManager(context) { event -> viewModel.applyInputEvent(event) }
    }
    DisposableEffect(Unit) {
        inputManager.start()
        onDispose { inputManager.destroy() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                val edgeThresholdPx = 48.dp.toPx()
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        if (offset.x < edgeThresholdPx) {
                            viewModel.showQuickSwitch()
                        }
                    },
                    onHorizontalDrag = { _, _ -> },
                )
            },
    ) {

        // ── GLSurfaceView ─────────────────────────────────────────────────
        val renderer = viewModel.effectRenderer
        if (renderer != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory  = { ctx ->
                    val glRenderer = GlSurfaceRenderer(
                        effectRenderer    = renderer,
                        inputStateProvider = viewModel::currentInputState,
                        onError           = viewModel::onRendererError,
                        onReady           = viewModel::onRendererReady,
                    )
                    GLSurfaceView(ctx).apply {
                        setEGLContextClientVersion(3)
                        setRenderer(glRenderer)
                        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                        setOnTouchListener { _, event ->
                            inputManager.onTouchEvent(event)
                            true
                        }
                        // Store renderer ref for cleanup
                        tag = glRenderer
                    }
                },
                onRelease = { surfaceView ->
                    // Clean up GL resources on the GL thread before view is released
                    val glRenderer = surfaceView.tag as? GlSurfaceRenderer
                    surfaceView.queueEvent { glRenderer?.destroy() }
                },
            )
        }

        // ── Quick-switch overlay ──────────────────────────────────────────
        if (isQuickSwitchVisible) {
            QuickSwitchOverlay(
                onEffectSelected = { effectId ->
                    viewModel.hideQuickSwitch()
                    navController.navigate(Screen.Effect.withId(effectId)) {
                        popUpTo(Screen.Effect.route) { inclusive = true }
                    }
                },
                onDismiss = { viewModel.hideQuickSwitch() },
            )
        }

        // ── Snackbar host ─────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter),
        )
    }
}
