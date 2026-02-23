package com.twodgfxapp.ui

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.twodgfxapp.input.InputState
import com.twodgfxapp.navigation.Screen
import com.twodgfxapp.renderer.EffectInitException
import com.twodgfxapp.renderer.EffectRenderer
import com.twodgfxapp.ui.effect.EffectUiState
import com.twodgfxapp.ui.effect.EffectViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EffectViewModelTest {

    private val mockApp: Application = mockk(relaxed = true)

    private fun makeViewModel(
        effectId:       String,
        fakeRenderer:   EffectRenderer? = mockk(relaxed = true),
    ): EffectViewModel {
        val savedState = SavedStateHandle(
            mapOf(Screen.Effect.ARG_EFFECT_ID to effectId)
        )
        return EffectViewModel(
            application    = mockApp,
            savedStateHandle = savedState,
            makeRenderer   = { fakeRenderer },
        )
    }

    @Test
    fun `valid effectId results in non-null effectDescriptor`() = runTest {
        val vm = makeViewModel("plasma")
        assertNotNull(vm.effectDescriptor)
        assertEquals("plasma", vm.effectDescriptor?.id)
    }

    @Test
    fun `unknown effectId results in Error UiState`() = runTest {
        val vm = makeViewModel("does-not-exist")
        val state = vm.uiState.first()
        assertTrue(state is EffectUiState.Error, "Expected Error state, got $state")
    }

    @Test
    fun `valid effectId starts with Loading UiState`() = runTest {
        val vm = makeViewModel("plasma")
        // The ViewModel starts in Loading; rendering begins only after GL surface is ready
        val state = vm.uiState.first()
        assertTrue(
            state is EffectUiState.Loading || state is EffectUiState.Rendering,
            "Expected Loading or Rendering, got $state",
        )
    }

    @Test
    fun `onRendererError transitions to Error state`() = runTest {
        val vm = makeViewModel("plasma")
        vm.onRendererError(EffectInitException("shader error"))
        val state = vm.uiState.first()
        assertTrue(state is EffectUiState.Error, "Expected Error state, got $state")
    }

    @Test
    fun `currentInputState returns initial empty InputState`() {
        val vm = makeViewModel("plasma")
        val input: InputState = vm.currentInputState()
        assertTrue(input.touchPoints.isEmpty())
        assertNull(input.tilt)
    }

    @Test
    fun `effectDescriptor is null for unknown effectId`() {
        val vm = makeViewModel("no-such-effect")
        assertNull(vm.effectDescriptor)
    }

    @Test
    fun `quickSwitch visibility starts as false`() = runTest {
        val vm = makeViewModel("plasma")
        assertEquals(false, vm.isQuickSwitchVisible.first())
    }

    @Test
    fun `showQuickSwitch sets visibility to true`() = runTest {
        val vm = makeViewModel("plasma")
        vm.showQuickSwitch()
        assertEquals(true, vm.isQuickSwitchVisible.first())
    }

    @Test
    fun `hideQuickSwitch sets visibility to false after show`() = runTest {
        val vm = makeViewModel("plasma")
        vm.showQuickSwitch()
        vm.hideQuickSwitch()
        assertEquals(false, vm.isQuickSwitchVisible.first())
    }
}
