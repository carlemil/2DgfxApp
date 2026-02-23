# Tasks: 2D Effect Showcase

**Feature**: `001-effect-showcase` | **Date**: 2026-02-22
**Input**: Design documents from `/specs/001-effect-showcase/`
**Plan**: plan.md | **Spec**: spec.md | **Data Model**: data-model.md | **Contracts**: effect-renderer.md, input-flow.md, navigation.md | **Quickstart**: quickstart.md

**Tests**: Included — plan.md Constitution Check mandates TDD for `EffectDescriptor`, `EffectCatalogue`,
`EffectViewModel`, and `InputManager`. Rendering paths are exempt (no deterministic GPU assertion feasible).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no unresolved dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- All paths are relative to repository root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Android project skeleton and shared build configuration.

- [X] T001 Create Android project skeleton: `app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`, `app/src/main/AndroidManifest.xml` (minSdk 26, targetSdk 34, package `com.twodgfxapp`)
- [X] T002 [P] Configure compile-time Gradle dependencies in `app/build.gradle.kts`: Compose BOM (latest stable), `androidx.navigation:navigation-compose`, `androidx.lifecycle:lifecycle-viewmodel-compose`, `androidx.lifecycle:lifecycle-runtime-compose` (OpenGL ES 3.0 is a platform API — no extra dependency)
- [X] T003 [P] Configure test Gradle dependencies in `app/build.gradle.kts`: JUnit 5 engine + `useJUnitPlatform()`, `app.cash.turbine:turbine`, `androidx.compose.ui:ui-test-junit4`, MockK for fake `SensorManager`
- [X] T004 [P] Create shared fullscreen vertex shader in `app/src/main/res/raw/fullscreen.vert` (emits a two-triangle fullscreen quad covering NDC clip space; no attributes, driven by `gl_VertexID`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core model classes, renderer contract, input model, and navigation scaffolding that ALL user stories depend on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T005 Create `EffectCategory` enum with 8 values (`RASTER_PLASMA`, `TUNNEL_3D`, `BLOBS_METABALLS`, `PARTICLES`, `DISTORTION`, `ROTOZOOM`, `CLASSICS_2D`, `FRACTALS`) and a `displayName: String` property in `app/src/main/java/com/twodgfxapp/model/EffectCategory.kt`
- [X] T006 Create `EffectDescriptor` data class (fields: `id: String`, `name: String`, `category: EffectCategory`, `touchSupport: Boolean`, `multiTouch: Boolean`, `tiltSupport: Boolean`) in `app/src/main/java/com/twodgfxapp/model/EffectDescriptor.kt`
- [X] T007 Create `EffectCatalogue` object with an immutable `effects: List<EffectDescriptor>` of all 29 entries (per data-model.md table) and `fun byCategory(): Map<EffectCategory, List<EffectDescriptor>>` in `app/src/main/java/com/twodgfxapp/model/EffectCatalogue.kt`
- [X] T008 [P] Create `TouchAction` enum (`DOWN`, `MOVE`, `UP`), `InputEvent` sealed class (`Touch(x, y, action, pointerId)`, `Tilt(x, y, z)`), and `InputState` data class (`touchPoints: List<InputEvent.Touch>`, `tilt: InputEvent.Tilt?`) in `app/src/main/java/com/twodgfxapp/input/InputEvent.kt`
- [X] T009 [P] Create `EffectRenderer` interface (`suspend fun init(width, height)`, `fun render(dt, input)`, `fun destroy()`) and `EffectInitException(message, cause?)` in `app/src/main/java/com/twodgfxapp/renderer/EffectRenderer.kt`
- [X] T010 [P] Create `GlUtils.kt` with `compileShader(type, source): Int`, `linkProgram(vert, frag): Int`, and `compileProgram(context, vertRawRes, fragRawRes): Int` helpers that read shaders from `res/raw/`, throw `EffectInitException` on GL error in `app/src/main/java/com/twodgfxapp/renderer/GlUtils.kt`
- [X] T011 Create `Screen` sealed class (`Menu`, `Effect`) and `AppNavGraph` composable with `NavHost` defining `"menu"` and `"effect/{effectId}"` destinations in `app/src/main/java/com/twodgfxapp/navigation/AppNavGraph.kt`
- [X] T012 Create `MainActivity.kt` as single-Activity host: instantiate `rememberNavController()`, call `AppNavGraph`, enable edge-to-edge display in `app/src/main/java/com/twodgfxapp/MainActivity.kt`

**Checkpoint**: Foundation complete — user story implementation can now begin (phases 3–5 can start in parallel if staffed).

---

## Phase 3: User Story 1 — Browse & Launch an Effect (Priority: P1) 🎯 MVP

**Goal**: User opens the app, sees all 29 effects grouped by 8 named categories, taps one, and the effect fills the screen animating at ≥ 60 fps. Back returns to the menu.

**Independent Test**: Launch app → scroll menu showing all 29 effects in 8 categories → tap any effect → effect fills screen within 1 second and animates → press Back → menu reappears.

### Tests for User Story 1 (TDD — write and confirm FAIL before implementing)

- [X] T013 [P] [US1] Write `EffectDescriptorTest`: assert all six fields are readable, data-class equality and `copy()` produce correct results in `app/src/test/java/com/twodgfxapp/model/EffectDescriptorTest.kt`
- [X] T014 [P] [US1] Write `EffectCatalogueTest`: assert `effects` has exactly 29 entries, all `id` values are unique, `byCategory()` returns all 8 `EffectCategory` keys, each effect appears in exactly one category bucket in `app/src/test/java/com/twodgfxapp/model/EffectCatalogueTest.kt`
- [X] T015 [P] [US1] Write `EffectViewModelTest`: assert valid `effectId` transitions UI state to Rendering, unknown `effectId` emits Error state, render loop coroutine is active when lifecycle is STARTED and suspended when STOPPED in `app/src/test/java/com/twodgfxapp/ui/EffectViewModelTest.kt`

### Implementation for User Story 1

- [X] T016 [US1] Create `MenuViewModel.kt` exposing `effectsByCategory: StateFlow<Map<EffectCategory, List<EffectDescriptor>>>` populated from `EffectCatalogue.byCategory()` in `app/src/main/java/com/twodgfxapp/ui/menu/MenuViewModel.kt`
- [X] T017 [US1] Create `MenuScreen.kt` composable with a category-grouped `LazyColumn` using sticky section headers (one per `EffectCategory.displayName`), each item calls `navController.navigate(Screen.Effect.withId(descriptor.id))` on tap; show "No effects available" when list is empty in `app/src/main/java/com/twodgfxapp/ui/menu/MenuScreen.kt`
- [X] T018 [US1] Create `GlSurfaceRenderer.kt` implementing `GLSurfaceView.Renderer`: delegates `onSurfaceCreated`/`onSurfaceChanged` → `EffectRenderer.init()`, `onDrawFrame` → `EffectRenderer.render(dt, inputState)` using elapsed time from `SystemClock.elapsedRealtimeNanos()`; catches `EffectInitException` and posts error event to `EffectViewModel` in `app/src/main/java/com/twodgfxapp/renderer/GlSurfaceRenderer.kt`
- [X] T019 [US1] Create `EffectViewModel.kt` with `repeatOnLifecycle(STARTED)` coroutine game loop on `Dispatchers.Default`, `UiState` sealed class (Loading / Rendering / Error), effect lookup from `EffectCatalogue`, and `AtomicReference<InputState>` initialized to `InputState(emptyList(), null)` in `app/src/main/java/com/twodgfxapp/ui/effect/EffectViewModel.kt`
- [X] T020 [US1] Create `EffectScreen.kt` composable: `AndroidView` wrapping `GLSurfaceView` with `RENDERMODE_CONTINUOUSLY`, `BackHandler` that calls `navController.popBackStack()`, `LaunchedEffect` on Error state that shows `Snackbar` and navigates back to menu in `app/src/main/java/com/twodgfxapp/ui/effect/EffectScreen.kt`
- [X] T021 [P] [US1] Create Raster / Plasma GLSL fragment shaders in `app/src/main/res/raw/`: `plasma.frag` (sine-wave color field), `copper-bars.frag` (sweeping gradient bars), `sine-scroller.frag` (text on sine path), `raster-bars.frag` (color-cycling horizontal bars), `checker-twist.frag` (rotating checkerboard); all declare `u_time`, `u_resolution`, `u_touch`, `u_tilt` uniforms
- [X] T022 [P] [US1] Create Tunnel & 3D GLSL fragment shaders in `app/src/main/res/raw/`: `tunnel.frag` (ray-marched infinite tunnel), `voxel-landscape.frag` (ray-cast height-map terrain), `dot-tunnel.frag` (dot-grid tunnel), `sphere.frag` (ray-traced sphere with lighting)
- [X] T023 [P] [US1] Create Blobs & Metaballs GLSL fragment shaders in `app/src/main/res/raw/`: `metaballs.frag` (smooth-min potential field with multiple centers), `blob-field.frag` (simpler 2D blobs), `gooey-blobs.frag` (gooey merge effect)
- [X] T024 [P] [US1] Create Particles GLSL fragment shaders in `app/src/main/res/raw/`: `particles.frag`, `fireworks.frag`, `starfield.frag`, `snow.frag`, `fire.frag`
- [X] T025 [P] [US1] Create Distortion GLSL fragment shaders in `app/src/main/res/raw/`: `water-ripple.frag` (sine ripple distortion), `lens-zoom.frag` (barrel / fisheye distortion), `heat-haze.frag` (turbulence warp), `shockwave.frag` (expanding ring distortion)
- [X] T026 [P] [US1] Create Rotozoom GLSL fragment shaders in `app/src/main/res/raw/`: `rotozoom.frag` (rotating + scaling tiled texture), `spinning-cube.frag` (ray-cast cube), `kaleidoscope.frag` (6-fold radial mirror)
- [X] T027 [P] [US1] Create 2D Classics GLSL fragment shaders in `app/src/main/res/raw/`: `bouncing-balls.frag` (SDF circles), `game-of-life.frag` (GPU automaton step)
- [X] T028 [P] [US1] Create Fractals GLSL fragment shaders in `app/src/main/res/raw/`: `mandelbrot.frag` (iterative escape-time with smooth colouring), `julia-set.frag` (Julia set with `u_touch`-driven C parameter), `burning-ship.frag` (burning ship fractal)
- [X] T029 [P] [US1] Create Raster / Plasma `EffectRenderer` classes in `app/src/main/java/com/twodgfxapp/renderer/effects/`: `PlasmaEffect`, `CopperBarsEffect`, `SineScrollerEffect`, `RasterBarsEffect`, `CheckerTwistEffect` — each calls `GlUtils.compileProgram()` in `init()`, sets `u_time` / `u_resolution` uniforms and draws fullscreen quad in `render()`, calls `glDeleteProgram` in `destroy()`
- [X] T030 [P] [US1] Create Tunnel & 3D `EffectRenderer` classes in `app/src/main/java/com/twodgfxapp/renderer/effects/`: `TunnelEffect`, `VoxelLandscapeEffect`, `DotTunnelEffect`, `SphereEffect` — `VoxelLandscapeEffect` updates height-map texture CPU-side each frame via `glTexSubImage2D`
- [X] T031 [P] [US1] Create Blobs & Metaballs `EffectRenderer` classes in `app/src/main/java/com/twodgfxapp/renderer/effects/`: `MetaballsEffect`, `BlobFieldEffect`, `GooeyBlobsEffect`
- [X] T032 [P] [US1] Create Particles `EffectRenderer` classes in `app/src/main/java/com/twodgfxapp/renderer/effects/`: `ParticlesEffect`, `FireworksEffect`, `StarfieldEffect`, `SnowEffect`, `FireEffect` — `ParticlesEffect` updates particle positions CPU-side each frame via `glBufferSubData` (pre-allocated `FloatBuffer`, no heap allocation in `render()`)
- [X] T033 [P] [US1] Create Distortion `EffectRenderer` classes in `app/src/main/java/com/twodgfxapp/renderer/effects/`: `WaterRippleEffect`, `LensZoomEffect`, `HeatHazeEffect`, `ShockwaveEffect`
- [X] T034 [P] [US1] Create Rotozoom `EffectRenderer` classes in `app/src/main/java/com/twodgfxapp/renderer/effects/`: `RotozoomEffect`, `SpinningCubeEffect`, `KaleidoscopeEffect`
- [X] T035 [P] [US1] Create 2D Classics `EffectRenderer` classes in `app/src/main/java/com/twodgfxapp/renderer/effects/`: `BouncingBallsEffect`, `GameOfLifeEffect`
- [X] T036 [P] [US1] Create Fractals `EffectRenderer` classes in `app/src/main/java/com/twodgfxapp/renderer/effects/`: `MandelbrotEffect`, `JuliaSetEffect`, `BurningShipEffect`
- [X] T037 [US1] Create `EffectRendererFactory.kt` with `fun create(descriptor: EffectDescriptor): EffectRenderer` that maps all 29 `EffectDescriptor.id` values to their `EffectRenderer` implementation; throw `IllegalArgumentException` for unknown IDs in `app/src/main/java/com/twodgfxapp/renderer/EffectRendererFactory.kt`

**Checkpoint**: User Story 1 is fully functional — menu lists all 29 effects grouped by category, tapping one launches fullscreen GL rendering, Back returns to menu.

---

## Phase 4: User Story 2 — Interact with a Running Effect (Priority: P2)

**Goal**: Touch and tilt input visibly influence the running effect within one rendered frame. Multi-touch tracked per pointer. No-accelerometer edge case handled silently.

**Independent Test**: With any touch-enabled effect running → touch/drag screen → effect responds visibly within one frame → lift finger → graceful return to default state. Tilt device → effect shifts with tilt direction.

### Tests for User Story 2 (TDD — write and confirm FAIL before implementing)

- [X] T038 [P] [US2] Write `InputManagerTest` using Turbine: assert `Touch DOWN` emitted on `ACTION_DOWN` `MotionEvent` with normalised coordinates, `Tilt` emitted on accelerometer callback with normalised values, no events and no exceptions when accelerometer absent (`hasAccelerometer = false`), multi-pointer `MotionEvent` produces one `Touch` item per active pointer in `app/src/test/java/com/twodgfxapp/input/InputManagerTest.kt`

### Implementation for User Story 2

- [X] T039 [US2] Create `InputManager.kt` with `val events: SharedFlow<InputEvent>` (replay=0, extraBufferCapacity=64, `DROP_OLDEST`); registers `SensorManager.TYPE_ACCELEROMETER` at `SENSOR_DELAY_GAME`, normalises output to `[-1, 1]`; exposes `fun onTouchEvent(event: MotionEvent)` that normalises pointer coordinates to `[0, 1]` and emits one `InputEvent.Touch` per pointer; provides `fun destroy()` to unregister all listeners in `app/src/main/java/com/twodgfxapp/input/InputManager.kt`
- [X] T040 [US2] Update `EffectViewModel.kt` to instantiate `InputManager`, collect `events` under `repeatOnLifecycle(STARTED)`, and update `AtomicReference<InputState>` by applying each incoming `InputEvent` via an `applyEvent(event): InputState` extension in `app/src/main/java/com/twodgfxapp/ui/effect/EffectViewModel.kt`
- [X] T041 [US2] Update `GlSurfaceRenderer.kt` to read the latest `InputState` from `EffectViewModel.currentInputState()` at the start of each `onDrawFrame` call and pass it to `EffectRenderer.render(dt, input)` in `app/src/main/java/com/twodgfxapp/renderer/GlSurfaceRenderer.kt`
- [X] T042 [US2] Update `EffectScreen.kt` to set an `OnTouchListener` on the `GLSurfaceView` that forwards `MotionEvent` to `InputManager.onTouchEvent()`; observe lifecycle via `DisposableEffect` so `InputManager.destroy()` is called when the composable leaves composition in `app/src/main/java/com/twodgfxapp/ui/effect/EffectScreen.kt`
- [X] T043 [P] [US2] Update GLSL fragment shaders for all effects with `touchSupport = true` or `tiltSupport = true` to read and respond to `u_touch` (vec2) and `u_tilt` (vec3) uniforms in `app/src/main/res/raw/` (affects: checker-twist, tunnel, voxel-landscape, dot-tunnel, sphere, metaballs, blob-field, gooey-blobs, particles, fireworks, fire, water-ripple, lens-zoom, shockwave, rotozoom, spinning-cube, kaleidoscope, bouncing-balls, game-of-life, mandelbrot, julia-set, burning-ship)
- [X] T044 [P] [US2] Update `render()` methods in all interactive `EffectRenderer` classes to call `glUniform2f(uTouchLoc, input.touchPoints.firstOrNull()?.x ?: 0f, ...)` and `glUniform3f(uTiltLoc, ...)` from the provided `InputState` in `app/src/main/java/com/twodgfxapp/renderer/effects/`

**Checkpoint**: User Stories 1 AND 2 are independently functional — all 29 effects render and the interactive ones respond to touch and tilt within one frame.

---

## Phase 5: User Story 3 — Switch Effects Without Returning to Menu (Priority: P3)

**Goal**: While an effect is running, a left-edge swipe opens a compact overlay picker. Selecting a new effect replaces the current one immediately. Pressing Back from the effect screen always returns to menu.

**Independent Test**: Effect running → swipe from left edge → overlay appears listing all 29 effects → tap a different effect → new effect renders without visiting the menu → Back → menu.

### Implementation for User Story 3

- [X] T045 [US3] Create `QuickSwitchOverlay.kt` composable: semi-transparent `Box` drawn on top of the effect, containing a scrollable `LazyColumn` of all 29 effect names; calls `onEffectSelected(id: String)` on item tap; calls `onDismiss()` on tap outside the list or rightward drag; visibility controlled by `isVisible: Boolean` parameter in `app/src/main/java/com/twodgfxapp/ui/effect/QuickSwitchOverlay.kt`
- [X] T046 [US3] Update `EffectViewModel.kt` to add `isQuickSwitchVisible: StateFlow<Boolean>`, `fun showQuickSwitch()`, `fun hideQuickSwitch()`, and `fun switchEffect(newId: String, navController: NavController)` that calls `navController.navigate(Screen.Effect.withId(newId)) { popUpTo(Screen.Effect.route) { inclusive = true } }` in `app/src/main/java/com/twodgfxapp/ui/effect/EffectViewModel.kt`
- [X] T047 [US3] Update `EffectScreen.kt` to detect a left-edge swipe using `Modifier.pointerInput` with `detectHorizontalDragGestures` (trigger when drag starts within 48.dp of the left edge); show `QuickSwitchOverlay` as a `Box` overlay on top of `AndroidView`; wire `onEffectSelected` to `viewModel.switchEffect()` and `onDismiss` to `viewModel.hideQuickSwitch()` in `app/src/main/java/com/twodgfxapp/ui/effect/EffectScreen.kt`
- [X] T048 [US3] Update `AppNavGraph.kt` to ensure the `"effect/{effectId}"` composable receives the `NavController` and that quick-switch navigation uses `popUpTo(Screen.Effect.route) { inclusive = true }` so the back stack never grows beyond `[menu, effect/{effectId}]` in `app/src/main/java/com/twodgfxapp/navigation/AppNavGraph.kt`

**Checkpoint**: All three user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Edge-case validation, performance verification, and FR-010/FR-011 compliance across all stories.

- [ ] T049 [P] Validate FR-010 background pause: run quickstart.md §7 manual test — start any effect, press Home, wait 10 s, return to app; confirm effect resumes without crash or black screen; verify in code that `repeatOnLifecycle(STARTED)` is the sole lifecycle gate in `app/src/main/java/com/twodgfxapp/ui/effect/EffectViewModel.kt`
- [X] T050 [P] Validate FR-011 error recovery: create `EffectScreenErrorTest` instrumented test that injects a mock `EffectRenderer` whose `init()` throws `EffectInitException`; assert `Snackbar` appears and `NavController` pops back to `"menu"` without crash in `app/src/androidTest/java/com/twodgfxapp/ui/EffectScreenErrorTest.kt`
- [ ] T051 [P] Validate empty-catalogue edge case: add a unit test (or manual verification) that `MenuScreen` shows "No effects available" message when `EffectCatalogue.effects` is empty; confirm the guard is present in `app/src/main/java/com/twodgfxapp/ui/menu/MenuScreen.kt`
- [ ] T052 [P] Validate no-accelerometer edge case: on an emulator without accelerometer, run the app; confirm `InputState.tilt` stays `null`, tilt-only effects (`starfield`, `snow`, `heat-haze`) still animate via the time uniform, and no error or crash occurs
- [ ] T053 Verify 60 fps performance: using Android Studio GPU Profiler, run `MandelbrotEffect`, `MetaballsEffect`, and `VoxelLandscapeEffect` for 10 seconds each; assert fragment shader execution ≤ 12ms and total `Choreographer` doFrame time ≤ 16.67ms per quickstart.md §8
- [ ] T054 [P] Review `render()` heap allocations: use Android Studio Memory Profiler during sustained rendering of `ParticlesEffect` and `MandelbrotEffect`; confirm zero GC events attributable to `render()`; fix any found allocations in the corresponding `EffectRenderer` classes in `app/src/main/java/com/twodgfxapp/renderer/effects/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Requires Phase 1 — **BLOCKS all user stories**
- **US1 (Phase 3)**: Requires Phase 2 — no dependency on US2 or US3
- **US2 (Phase 4)**: Requires Phase 2 AND US1 complete (`EffectViewModel`, `GlSurfaceRenderer`, `EffectScreen` must exist to extend)
- **US3 (Phase 5)**: Requires Phase 2 AND US1 complete; US2 is recommended but not strictly blocking
- **Polish (Phase 6)**: Requires all desired user stories complete

### User Story Dependencies

- **User Story 1 (P1)**: No dependency on other stories — pure greenfield after Foundation
- **User Story 2 (P2)**: Extends `EffectViewModel`, `GlSurfaceRenderer`, and `EffectScreen` created in US1 — US1 must be complete first
- **User Story 3 (P3)**: Adds overlay to `EffectScreen` and extends `EffectViewModel` from US1; independent of US2 (no input dependency)

### Within Each User Story

- TDD tests (T013–T015, T038) MUST be written and confirmed FAILING before implementation tasks begin
- Model / data classes before ViewModels / services
- Shaders [P] and `EffectRenderer` classes [P] per category can be developed concurrently across categories
- `EffectRendererFactory` (T037) must follow all `EffectRenderer` class tasks (T029–T036)
- `EffectScreen` (T020) must follow `EffectViewModel` (T019) and `GlSurfaceRenderer` (T018)
- `AppNavGraph` (T011) must be wired into `MainActivity` (T012) before any screen can be reached at runtime

---

## Parallel Execution Examples

### Phase 2: Foundational tasks (run after T001)

```
T005 → T006 → T007   (sequential: Category → Descriptor → Catalogue)
T008 [P]             (InputEvent / InputState — independent)
T009 [P]             (EffectRenderer interface — independent)
T010 [P]             (GlUtils — independent)
T011 [P]             (AppNavGraph — independent)
```

T005–T007 must complete before T009 (InputState referenced in EffectRenderer.render).

### Phase 3: US1 shaders and renderers are fully parallel across categories

```
Parallel group A — Shaders (T021–T028, all independent):
  T021  plasma.frag, copper-bars.frag, sine-scroller.frag, raster-bars.frag, checker-twist.frag
  T022  tunnel.frag, voxel-landscape.frag, dot-tunnel.frag, sphere.frag
  T023  metaballs.frag, blob-field.frag, gooey-blobs.frag
  T024  particles.frag, fireworks.frag, starfield.frag, snow.frag, fire.frag
  T025  water-ripple.frag, lens-zoom.frag, heat-haze.frag, shockwave.frag
  T026  rotozoom.frag, spinning-cube.frag, kaleidoscope.frag
  T027  bouncing-balls.frag, game-of-life.frag
  T028  mandelbrot.frag, julia-set.frag, burning-ship.frag

Parallel group B — EffectRenderers (T029–T036, all independent):
  T029  PlasmaEffect, CopperBarsEffect, SineScrollerEffect, RasterBarsEffect, CheckerTwistEffect
  T030  TunnelEffect, VoxelLandscapeEffect, DotTunnelEffect, SphereEffect
  T031  MetaballsEffect, BlobFieldEffect, GooeyBlobsEffect
  T032  ParticlesEffect, FireworksEffect, StarfieldEffect, SnowEffect, FireEffect
  T033  WaterRippleEffect, LensZoomEffect, HeatHazeEffect, ShockwaveEffect
  T034  RotozoomEffect, SpinningCubeEffect, KaleidoscopeEffect
  T035  BouncingBallsEffect, GameOfLifeEffect
  T036  MandelbrotEffect, JuliaSetEffect, BurningShipEffect

Groups A and B can run fully concurrently — T037 (factory) waits for all of group B.
```

### Phase 4: US2 sequence

```
T038 (InputManagerTest — must FAIL) → T039 (InputManager) → T040 (ViewModel) → T041 + T042 [parallel]
T043 (update shaders) [P] — runs after T039, parallel with T044
T044 (update renderers) [P] — runs after T039, parallel with T043
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (**CRITICAL — blocks everything**)
3. Complete Phase 3: User Story 1 (T013–T037)
4. **STOP and VALIDATE**: Run quickstart.md §1–§3 smoke test
5. Demo / share if ready

### Incremental Delivery

1. **Setup + Foundation** → project builds, no visible output yet
2. **+ US1** → Menu + 29 animated effects → **MVP!**
3. **+ US2** → Touch and tilt interactivity on all supported effects
4. **+ US3** → Quick-switch overlay for rapid exploration
5. **+ Polish** → Performance verified, edge cases handled, FR-010/FR-011 confirmed

### Parallel Team Strategy

With multiple developers available:

1. Team completes Setup (Phase 1) + Foundational (Phase 2) together
2. Once Foundational is done:
   - **Developer A**: US1 core screens (T016–T020), wires navigation
   - **Developer B**: Category 1–4 shaders + renderers (T021–T024, T029–T032)
   - **Developer C**: Category 5–8 shaders + renderers (T025–T028, T033–T036)
3. Developer A integrates factory (T037) once B and C finish
4. All three pick up US2 together

---

## Notes

- `[P]` tasks operate on different files with no unresolved dependencies — safe to parallelise
- `[Story]` label maps each task to a user story for traceability and independent delivery
- TDD order: write test → confirm it fails → implement → confirm it passes → commit
- Each user story is independently completable and testable before the next begins
- No heap allocations in `render()` — pre-allocate all `FloatBuffer` and uniform location caches in `init()`
- Commit after each task or logical group for a clean, bisectable git history
