# Quickstart: 2D Effect Showcase

**Feature**: `001-effect-showcase` | **Date**: 2026-02-22

---

## 1. Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** (bundled with Android Studio)
- **Android SDK** with Build Tools 34+ and Platform 34+
- **AVD or physical device**: Android 8.0+ (API 26+); physical device strongly
  recommended for accelerometer tilt tests and accurate GPU profiling
- **OpenGL ES 3.0** support on the target device/emulator

---

## 2. Build & Run

```bash
# Clone (if needed) and open in Android Studio, or:
./gradlew :app:installDebug

# For release build:
./gradlew :app:assembleRelease
```

The app installs as **2D Gfx App** on the device.

---

## 3. Manual Smoke Test

1. Launch the app — the **Menu screen** appears with effects grouped by category.
2. Scroll through all 8 categories; verify all 29 effects are listed.
3. Tap **"Plasma"** — the screen transitions to a fullscreen animated effect.
4. Verify the plasma animation is running smoothly (visually ≥ 60 fps, no stutter).
5. Press **Back** — the Menu screen reappears.
6. Tap **"Mandelbrot"** — a fullscreen fractal zoom animation begins.
7. Verify the zoom is smooth and the fractal detail increases over time.

---

## 4. Interaction Test (Touch)

1. Navigate to **Particles** from the menu.
2. Touch and drag across the screen.
3. Verify: particles are attracted toward or follow the finger position.
4. Lift finger — verify particles return to autonomous motion.

For multi-touch effects (Metaballs, Mandelbrot):

1. Navigate to **Metaballs**.
2. Place two fingers on screen simultaneously.
3. Verify: two independent blob attractors respond to each finger.

---

## 5. Tilt Test (Accelerometer)

1. Navigate to **Starfield** from the menu.
2. Hold the device upright (portrait), then tilt left/right and forward/back.
3. Verify: stars shift direction matching the tilt — tilting left shifts stars left.
4. Verify: effect still runs if device is held flat (edge case — graceful no-op).

> Note: Tilt is silently disabled on devices without an accelerometer.
> The effect runs in touch-only mode with no error shown.

---

## 6. Quick-Switch Test (US3)

1. Start any effect (e.g., **Tunnel**).
2. Swipe in from the left edge of the screen.
3. Verify: a quick-switch overlay appears listing all 29 effects.
4. Tap a different effect (e.g., **Julia Set**) from the overlay.
5. Verify: the new effect starts immediately without visiting the Menu screen.
6. Confirm the back stack: pressing Back returns to the Menu, not the overlay.

---

## 7. Background Pause Test (FR-010)

1. Start any effect (e.g., **Fireworks**).
2. Press the Home button — the app moves to background.
3. Wait 10 seconds.
4. Return to the app via the Recents list.
5. Verify: the effect resumes from where it paused — no crash, no black screen,
   no frozen frame.

---

## 8. Performance Profiling

### Frame time verification

1. Open **Android Studio → Profiler → CPU** with the app running.
2. Select **System Trace (Systrace)**.
3. Run any complex effect (Mandelbrot, Metaballs, Voxel Landscape).
4. Capture a 10-second trace.
5. Verify: frame time ≤ 16.67ms (60 fps) in the `renderFrame` / `Choreographer`
   doFrame section.

### GPU shader timing

1. Connect a physical device with GPU debugging support.
2. Use **Android GPU Inspector** or **RenderDoc** (where available).
3. Capture one frame for the Mandelbrot effect.
4. Verify: fragment shader execution time ≤ 12ms.

### Memory

- Verify: no `GC` pauses visible in the Memory Profiler during sustained rendering
  (the `render()` method must not allocate heap objects).

---

## 9. Adding a New Effect (Developer Guide)

Follow these steps to add effect #30 (or any additional effect):

### Step 1: Create the Kotlin renderer

```
app/src/main/java/com/twodgfxapp/renderer/effects/<slug>/
└── <NameEffect>.kt
```

`<NameEffect>` must implement `EffectRenderer`:

```kotlin
class MyNewEffect : EffectRenderer {
    private var program = 0

    override suspend fun init(width: Int, height: Int) {
        // Compile and link shaders; store GL program handle
        program = GlUtils.compileProgram("fullscreen.vert", "my_new_effect.frag")
    }

    override fun render(dt: Float, input: InputState) {
        // Bind program, set uniforms (u_time, u_resolution, u_touch, u_tilt), draw quad
    }

    override fun destroy() {
        GLES30.glDeleteProgram(program)
    }
}
```

### Step 2: Add the GLSL fragment shader

```
app/src/main/res/raw/<slug>.frag
```

Minimum shader template:

```glsl
#version 300 es
precision mediump float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    // ... your effect logic here ...
    fragColor = vec4(uv, 0.5 + 0.5 * sin(u_time), 1.0);
}
```

### Step 3: Register in EffectCatalogue

Open `app/src/main/java/com/twodgfxapp/model/EffectCatalogue.kt` and add:

```kotlin
EffectDescriptor(
    id           = "<slug>",
    name         = "My New Effect",
    category     = EffectCategory.<CATEGORY>,
    touchSupport = true,
    multiTouch   = false,
    tiltSupport  = false
)
```

### Step 4: Write the unit test

Add a test in `app/src/test/` verifying the new `EffectDescriptor` is present
in `EffectCatalogue.effects` and appears in the correct category bucket from
`byCategory()`.

### Step 5: Smoke test

Run the app, scroll to the new effect in the menu, tap it, and verify it renders.
