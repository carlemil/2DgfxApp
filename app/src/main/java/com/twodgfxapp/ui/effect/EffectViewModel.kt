package com.twodgfxapp.ui.effect

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.twodgfxapp.input.InputEvent
import com.twodgfxapp.input.InputState
import com.twodgfxapp.input.applyEvent
import com.twodgfxapp.model.EffectCatalogue
import com.twodgfxapp.model.EffectDescriptor
import com.twodgfxapp.navigation.Screen
import com.twodgfxapp.renderer.EffectInitException
import com.twodgfxapp.renderer.EffectRenderer
import com.twodgfxapp.renderer.EffectRendererFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicReference

/** Sealed UI state exposed to [EffectScreen]. */
sealed class EffectUiState {
    object Loading   : EffectUiState()
    object Rendering : EffectUiState()
    data class Error(val message: String) : EffectUiState()
}

/**
 * ViewModel for the effect screen.
 *
 * Owns:
 * - [effectDescriptor] looked up from [EffectCatalogue]
 * - [effectRenderer] created by [makeRenderer] (no GL calls yet — init happens on GL thread)
 * - [_inputState] AtomicReference updated by [InputManager] collection and read by GL thread
 * - [uiState] / [isQuickSwitchVisible] exposed to [EffectScreen]
 *
 * @param makeRenderer Injectable factory for testing; defaults to [EffectRendererFactory.create].
 */
class EffectViewModel(
    application:     Application,
    savedStateHandle: SavedStateHandle,
    internal val makeRenderer: (EffectDescriptor) -> EffectRenderer?,
) : AndroidViewModel(application) {

    /** No-arg-factory constructor called by [SavedStateViewModelFactory] at runtime. */
    constructor(application: Application, savedStateHandle: SavedStateHandle) : this(
        application,
        savedStateHandle,
        { descriptor -> EffectRendererFactory.create(application, descriptor) },
    )


    val effectDescriptor: EffectDescriptor? =
        EffectCatalogue.findById(
            savedStateHandle[Screen.Effect.ARG_EFFECT_ID] ?: ""
        )

    /** The [EffectRenderer] instance (Kotlin object, no GL calls until [GlSurfaceRenderer.onSurfaceChanged]). */
    val effectRenderer: EffectRenderer? =
        effectDescriptor?.let { makeRenderer(it) }

    private val _uiState = MutableStateFlow<EffectUiState>(
        if (effectDescriptor == null) EffectUiState.Error("Unknown effect")
        else EffectUiState.Loading
    )
    val uiState: StateFlow<EffectUiState> = _uiState.asStateFlow()

    private val _isQuickSwitchVisible = MutableStateFlow(false)
    val isQuickSwitchVisible: StateFlow<Boolean> = _isQuickSwitchVisible.asStateFlow()

    /** Current input snapshot — written on coroutine thread, read on GL thread. */
    private val _inputState = AtomicReference(InputState(emptyList(), null))

    // ── Input state management ─────────────────────────────────────────────

    /** Thread-safe read for the GL thread. */
    fun currentInputState(): InputState = _inputState.get()

    /** Called by [com.twodgfxapp.input.InputManager] collector to apply a new event. */
    fun applyInputEvent(event: InputEvent) {
        _inputState.updateAndGet { current -> current.applyEvent(event) }
    }

    // ── Renderer callbacks ─────────────────────────────────────────────────

    /** Called from the GL thread via [GlSurfaceRenderer] when the surface is ready. */
    fun onRendererReady() {
        _uiState.update { EffectUiState.Rendering }
    }

    /** Called from the GL thread via [GlSurfaceRenderer] when init fails. */
    fun onRendererError(e: EffectInitException) {
        _uiState.update { EffectUiState.Error(e.message ?: "Render error") }
    }

    // ── Quick-switch ───────────────────────────────────────────────────────

    fun showQuickSwitch() { _isQuickSwitchVisible.value = true  }
    fun hideQuickSwitch() { _isQuickSwitchVisible.value = false }
}
