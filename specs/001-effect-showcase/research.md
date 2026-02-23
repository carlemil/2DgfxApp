# Research: 2D Effect Showcase

**Feature**: `001-effect-showcase` | **Date**: 2026-02-22

This document records the design decisions made during Phase 0 research for the
2D Effect Showcase. Each decision captures the chosen approach, rationale, and
rejected alternatives.

---

## Decision 1: Rendering Surface

**Decision**: GLSurfaceView (OpenGL ES 3.0) wrapped in `AndroidView`

**Rationale**: Only rendering path that can sustain 60 fps for all 29 effects.
GLSL fragment shaders execute all 29 effects in < 5ms GPU time. Equivalent CPU
implementations take 15–500ms per frame:

| Effect | CPU (Compose Canvas) | GPU (GLSL shader) |
|--------|----------------------|-------------------|
| Mandelbrot | 100–500ms | < 2ms |
| Metaballs | ~200ms | < 3ms |
| Plasma | ~15–20ms | < 1ms |
| Starfield, Coppers, etc. | 5–30ms | < 1ms |

At 16.67ms frame budget, ~15 of the 29 effects fail on mid-range devices with
CPU rendering.

All 29 effects are pixel-shader-driven: input is fragment coordinate + time +
input state uniforms; output is colour. A single fullscreen quad vertex shader
(`fullscreen.vert`) is reused across all effects; each effect provides only its
own fragment shader.

**Alternatives considered**:

| Alternative | Reason Rejected |
|-------------|-----------------|
| Compose Canvas | CPU-only; cannot sustain 60 fps for ~15 of 29 effects on mid-range hardware |
| AGSL (Android 13+) | Limits min API to 33; provides less flexibility for 3D-style effects (voxel, tunnel); Compose Canvas backend still CPU-bound for complex shaders |
| Vulkan | Unnecessary complexity for 2D pixel shaders; no feature requires Vulkan-specific capabilities |
| TextureView | Extra compositing overhead compared to SurfaceView; no benefit for full-screen rendering |

---

## Decision 2: Game / Render Loop

**Decision**: Coroutine-based fixed-timestep loop:

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch(Dispatchers.Default) {
            fixedTimestepLoop()   // updates state, queues GL requests
        }
    }
}
```

**Frame budget breakdown**:

| Component | Budget |
|-----------|--------|
| GPU render (GLSL shader) | ≤ 12ms |
| State update (coroutine, `Dispatchers.Default`) | ≤ 2ms |
| Overhead / synchronisation | ≤ 2ms |
| **Total** | **≤ 16.67ms** |

**Rationale**: `repeatOnLifecycle(STARTED)` automatically pauses the loop when
the app moves to background (satisfying FR-010) and cancels cleanly on lifecycle
destroy — no manual lifecycle management required. A fixed timestep ensures
consistent physics/simulation regardless of frame variance. `Dispatchers.Default`
keeps CPU-side state update off the main thread.

The GL render tick itself uses `GLSurfaceView.Renderer.onDrawFrame` (the standard
OpenGL ES callback), invoked by the GL thread. The coroutine loop feeds updated
`InputState` into the renderer before each draw call via an `AtomicReference`.

**Alternatives considered**:

| Alternative | Reason Rejected |
|-------------|-----------------|
| `Handler.postDelayed` | Not lifecycle-aware; manual cancellation required; doesn't integrate with Coroutines |
| Raw `Thread` | Violates Principle III (Coroutines); no structured concurrency |
| `onDrawFrame`-only loop | Cannot drive coroutine-based input/state pipeline |

---

## Decision 3: Navigation

**Decision**: Jetpack Navigation Compose; two routes:
- `menu` — category-grouped effect list
- `effect/{effectId}` — fullscreen effect screen

**Rationale**: Native Compose navigation with lifecycle-aware back stack. The
`effect/{effectId}` route contains a fullscreen `GLSurfaceView` wrapped in
`AndroidView`. Quick-switch (US3) is implemented as a Compose overlay drawn
on top of the effect screen — it is not a separate route, avoiding an extra
screen transition and back-stack entry.

**Alternatives considered**:

| Alternative | Reason Rejected |
|-------------|-----------------|
| Manual back-stack management | Reimplements Navigation Compose without benefit; not idiomatic |
| Fragment-based navigation | Not Compose-first; adds unnecessary Fragment lifecycle management |
| Bottom sheet for quick-switch route | Creates extra back-stack entry; overlay is simpler and avoids navigation overhead |

---

## Decision 4: Input Handling

**Decision**: `SensorManager.TYPE_ACCELEROMETER` for tilt + `MotionEvent` touch
listener on the `GLSurfaceView`; both streams merged into a `SharedFlow<InputEvent>`
collected by `EffectViewModel`.

```kotlin
sealed class InputEvent {
    data class Touch(val x: Float, val y: Float,
                     val action: TouchAction, val pointerId: Int) : InputEvent()
    data class Tilt(val x: Float, val y: Float, val z: Float) : InputEvent()
}
```

**Sensor rate**: `SENSOR_DELAY_GAME` (~20ms / 50 Hz) — sufficient for visually
responsive tilt within one render frame.

**Rationale**: Merging both streams into a single `SharedFlow` lets the same
state-update pipeline handle touch and tilt without callback nesting or separate
threading concerns. Sensor availability is checked at startup; tilt events are
silently omitted (no emission, no error) when the accelerometer is absent — the
effect runs touch-only.

**Alternatives considered**:

| Alternative | Reason Rejected |
|-------------|-----------------|
| Separate callbacks for touch vs tilt | Callback nesting; harder to test |
| `StateFlow` instead of `SharedFlow` | `StateFlow` replays last value; `SharedFlow(replay=0)` is correct for event streams where missing a stale event is fine |
| `SENSOR_DELAY_UI` | Too slow (~66ms); tilt lag perceptible within a frame |
| `SENSOR_DELAY_FASTEST` | Unnecessary CPU overhead; 50 Hz is sufficient |

---

## Decision 5: Shader Strategy

**Decision**: Per-effect GLSL fragment shader stored as `res/raw/<slug>.frag`;
one shared full-screen quad vertex shader (`res/raw/fullscreen.vert`).

**Rationale**: All 29 effects are pixel-shader-driven. A single vertex shader
emits a fullscreen quad (two triangles covering clip space); each effect's
fragment shader receives:
- `u_time` — elapsed seconds (float)
- `u_resolution` — viewport dimensions (vec2)
- `u_touch` — normalised touch position (vec2)
- `u_tilt` — normalised accelerometer values (vec3)

Reusing one vertex shader keeps the pipeline uniform and minimises boilerplate.
Shaders are compiled once during `EffectRenderer.init()` and cached as program
objects; no runtime recompilation.

**CPU-hybrid effects** (Particles, Voxel landscape): Particle positions / voxel
height-map are updated on `Dispatchers.Default` each frame; updated vertex or
texture data is uploaded to GPU via `glBufferSubData` / `glTexSubImage2D` before
the draw call.

**Alternatives considered**:

| Alternative | Reason Rejected |
|-------------|-----------------|
| Hardcoded shader strings in Kotlin | Difficult to edit; no syntax highlighting; no hot-reload potential |
| Separate compiled binary shaders | Increases build complexity; GLSL source is small and compiles fast on device |
| Third-party rendering library (e.g., libGDX) | Adds a large dependency for functionality provided by the platform API |

---

## Library Evaluation (Principle V — Simplicity)

No third-party rendering libraries adopted. OpenGL ES 3.0 is a platform API
(zero extra Gradle dependency). Jetpack Navigation Compose and Lifecycle are
already in the canonical Android tech stack.

| Library Considered | Decision | Reason |
|--------------------|----------|--------|
| libGDX | Rejected | Overkill; provides a full game engine when only GL shader execution is needed |
| SceneKit / Filament | Rejected | 3D engines; adds unnecessary complexity for 2D effects |
| AGSL / RuntimeShader | Rejected | API 33+ only; breaks min-SDK 26 target |
| Coil / Glide | N/A | No image loading required |
