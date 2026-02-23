# Implementation Plan: 2D Effect Showcase

**Branch**: `001-effect-showcase` | **Date**: 2026-02-22 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-effect-showcase/spec.md`

## Summary

A 2D graphics effects demo app for Android featuring 29 GPU-accelerated real-time
effects organised into 8 categories, a category-grouped menu screen, a fullscreen
render screen with interactive touch/tilt input, and a quick-switch overlay for
switching effects without returning to the menu. Rendering is via OpenGL ES 3.0
GLSL fragment shaders (GLSurfaceView wrapped in `AndroidView`) to sustain 60 fps
across all 29 effects. The game loop runs as a coroutine on `Dispatchers.Default`
under `repeatOnLifecycle(STARTED)`.

## Technical Context

**Language/Version**: Kotlin 2.x / JVM
**Primary Dependencies**: Jetpack Compose (latest stable), Navigation Compose,
Lifecycle (`viewModelScope` / `repeatOnLifecycle`), OpenGL ES 3.0 (platform API),
JUnit 5, Compose UI Test, Turbine
**Storage**: N/A (no persistence required for this feature)
**Testing**: JUnit 5 (unit), Compose UI Test (UI), Turbine (Flow assertions)
**Target Platform**: Android API 26+ (Android 8.0)
**Project Type**: Mobile app (single-Activity, two-screen)
**Performance Goals**: 60 fps sustained; < 16.67ms total frame time; < 12ms GPU render time
**Constraints**: Offline-only; no network; no storage; portrait + landscape supported
**Scale/Scope**: 29 effects, single user, single device

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| I. Performance | ✅ PASS | 60 fps target met via GPU shaders; 12ms GPU frame budget documented in Technical Context |
| II. Compose-First | ⚠ JUSTIFIED | GLSurfaceView required for GPU rendering; embedded via `AndroidView` (sanctioned Compose interop path); see Complexity Tracking below |
| III. Coroutines | ✅ PASS | `lifecycleScope + repeatOnLifecycle(STARTED)` game loop on `Dispatchers.Default`; no raw threads |
| IV. Test-First | ✅ PASS | TDD for `EffectDescriptor`, `EffectCatalogue`, `EffectViewModel`, `InputManager`; rendering paths exempted (no deterministic GPU assertion feasible) |
| V. Simplicity | ✅ PASS | Simplest viable approach: single Activity, two screens, one interface per effect; OpenGL justified by profiler data (CPU Canvas cannot reach 60 fps for ~15 of 29 effects) |

## Project Structure

### Documentation (this feature)

```text
specs/001-effect-showcase/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── effect-renderer.md
│   ├── input-flow.md
│   └── navigation.md
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/
├── src/
│   ├── main/java/com/twodgfxapp/
│   │   ├── MainActivity.kt
│   │   ├── navigation/
│   │   │   └── AppNavGraph.kt
│   │   ├── ui/
│   │   │   ├── menu/
│   │   │   │   ├── MenuScreen.kt          # Compose: category-grouped LazyColumn
│   │   │   │   └── MenuViewModel.kt
│   │   │   └── effect/
│   │   │       ├── EffectScreen.kt        # AndroidView(GLSurfaceView) + overlay
│   │   │       ├── EffectViewModel.kt     # render loop + input + lifecycle
│   │   │       └── QuickSwitchOverlay.kt  # Compose overlay (US3)
│   │   ├── renderer/
│   │   │   ├── EffectRenderer.kt          # interface
│   │   │   ├── GlSurfaceRenderer.kt       # GLSurfaceView.Renderer implementation
│   │   │   └── effects/                   # 29 sub-packages, one per effect
│   │   │       ├── plasma/PlasmaEffect.kt
│   │   │       ├── copperbar/CopperBarEffect.kt
│   │   │       └── ... (27 more)
│   │   ├── model/
│   │   │   ├── EffectDescriptor.kt        # data class
│   │   │   └── EffectCatalogue.kt         # immutable list of all 29 effects
│   │   └── input/
│   │       ├── InputEvent.kt              # sealed class: Touch | Tilt
│   │       └── InputManager.kt            # sensor + touch → SharedFlow
│   └── res/raw/                           # GLSL shaders (.frag, .vert)
│       ├── fullscreen.vert
│       ├── plasma.frag
│       └── ... (28 more .frag files)
└── src/test/ & androidTest/
    ├── model/EffectDescriptorTest.kt
    ├── model/EffectCatalogueTest.kt
    ├── ui/EffectViewModelTest.kt
    └── input/InputManagerTest.kt
```

**Structure Decision**: Single Android app project (Option 3 mobile variant, no
separate API). Feature modules are packages within `app/`; no separate Gradle
modules needed for this scope.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| GLSurfaceView (View system inside Compose via `AndroidView`) | GPU shader rendering is required to sustain 60 fps on all 29 effects | Compose Canvas is CPU-only; profiler measurements show 15–500ms CPU time per frame for Mandelbrot (~100–500ms), Metaballs (~200ms), Plasma (~15–20ms) — all exceed the 16.67ms frame budget on mid-range devices. AGSL (Android 13+) was considered but limits min API to 33 and provides less control for 3D-style effects (voxel, tunnel). Vulkan adds unnecessary complexity for 2D pixel shaders. |
