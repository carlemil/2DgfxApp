# Data Model: 2D Effect Showcase

**Feature**: `001-effect-showcase` | **Date**: 2026-02-22

---

## EffectDescriptor

```kotlin
data class EffectDescriptor(
    val id:           String,          // unique slug, e.g. "plasma", "copper-bars"
    val name:         String,          // display name, e.g. "Plasma"
    val category:     EffectCategory,  // one of 8 categories (see below)
    val touchSupport: Boolean,         // responds to single touch / drag
    val multiTouch:   Boolean,         // responds to 2+ simultaneous touch points
    val tiltSupport:  Boolean          // responds to accelerometer tilt
)
```

---

## EffectCategory (enum)

```kotlin
enum class EffectCategory(val displayName: String) {
    RASTER_PLASMA   ("Raster / Plasma"),
    TUNNEL_3D       ("Tunnel & 3D"),
    BLOBS_METABALLS ("Blobs & Metaballs"),
    PARTICLES       ("Particles"),
    DISTORTION      ("Distortion"),
    ROTOZOOM        ("Rotozoom"),
    CLASSICS_2D     ("2D Classics"),
    FRACTALS        ("Fractals")
}
```

---

## EffectCatalogue

```kotlin
object EffectCatalogue {
    val effects: List<EffectDescriptor>   // immutable, ordered, all 29 entries

    fun byCategory(): Map<EffectCategory, List<EffectDescriptor>>
    // Returns entries grouped by category, preserving insertion order within each group
}
```

### All 29 Effects

| # | id | name | category | touch | multiTouch | tilt |
|---|-----|------|----------|-------|-----------|------|
| 1 | `plasma` | Plasma | RASTER_PLASMA | false | false | false |
| 2 | `copper-bars` | Copper Bars | RASTER_PLASMA | false | false | false |
| 3 | `sine-scroller` | Sine Scroller | RASTER_PLASMA | false | false | false |
| 4 | `raster-bars` | Raster Bars | RASTER_PLASMA | false | false | false |
| 5 | `checker-twist` | Checker Twist | RASTER_PLASMA | true | false | false |
| 6 | `tunnel` | Tunnel | TUNNEL_3D | true | false | true |
| 7 | `voxel-landscape` | Voxel Landscape | TUNNEL_3D | true | false | true |
| 8 | `dot-tunnel` | Dot Tunnel | TUNNEL_3D | true | false | false |
| 9 | `sphere` | Sphere | TUNNEL_3D | true | false | true |
| 10 | `metaballs` | Metaballs | BLOBS_METABALLS | true | true | false |
| 11 | `blob-field` | Blob Field | BLOBS_METABALLS | true | false | false |
| 12 | `gooey-blobs` | Gooey Blobs | BLOBS_METABALLS | true | true | false |
| 13 | `particles` | Particles | PARTICLES | true | false | true |
| 14 | `fireworks` | Fireworks | PARTICLES | true | false | false |
| 15 | `starfield` | Starfield | PARTICLES | false | false | true |
| 16 | `snow` | Snow | PARTICLES | false | false | true |
| 17 | `fire` | Fire | PARTICLES | true | false | false |
| 18 | `water-ripple` | Water Ripple | DISTORTION | true | true | false |
| 19 | `lens-zoom` | Lens Zoom | DISTORTION | true | false | false |
| 20 | `heat-haze` | Heat Haze | DISTORTION | false | false | true |
| 21 | `shockwave` | Shockwave | DISTORTION | true | false | false |
| 22 | `rotozoom` | Rotozoom | ROTOZOOM | true | false | false |
| 23 | `spinning-cube` | Spinning Cube | ROTOZOOM | true | false | true |
| 24 | `kaleidoscope` | Kaleidoscope | ROTOZOOM | true | false | false |
| 25 | `bouncing-balls` | Bouncing Balls | CLASSICS_2D | true | false | false |
| 26 | `game-of-life` | Game of Life | CLASSICS_2D | true | false | false |
| 27 | `mandelbrot` | Mandelbrot | FRACTALS | true | true | false |
| 28 | `julia-set` | Julia Set | FRACTALS | true | true | false |
| 29 | `burning-ship` | Burning Ship | FRACTALS | true | true | false |

---

## InputEvent (sealed class)

```kotlin
sealed class InputEvent {

    data class Touch(
        val x:         Float,        // normalised [0, 1] horizontal position
        val y:         Float,        // normalised [0, 1] vertical position
        val action:    TouchAction,  // DOWN | MOVE | UP
        val pointerId: Int           // pointer index for multi-touch tracking
    ) : InputEvent()

    data class Tilt(
        val x: Float,   // normalised accelerometer x [-1, 1]
        val y: Float,   // normalised accelerometer y [-1, 1]
        val z: Float    // normalised accelerometer z [-1, 1]
    ) : InputEvent()
}

enum class TouchAction { DOWN, MOVE, UP }
```

---

## InputState (snapshot passed to renderer each frame)

```kotlin
data class InputState(
    val touchPoints: List<InputEvent.Touch>,   // all currently active touch points
    val tilt:        InputEvent.Tilt?          // null when sensor unavailable
)
```

`InputState` is immutable. A new instance is constructed by `EffectViewModel`
each frame from the most recent `InputEvent` items collected via the `SharedFlow`.

---

## EffectRenderer (interface)

```kotlin
interface EffectRenderer {
    suspend fun init(width: Int, height: Int)
    fun render(dt: Float, input: InputState)
    fun destroy()
}
```

| Method | Thread | Notes |
|--------|--------|-------|
| `init` | GL thread | Called once after surface creation. Throws `EffectInitException` on failure. |
| `render` | GL thread | Called every frame. `dt` = elapsed seconds (target ≈ 0.01667). Must complete within 12ms. Must NOT allocate heap objects. |
| `destroy` | GL thread | Called before surface destruction. Must release all GL resources. |

### State Machine

```
CREATED ──init()──► READY ──render() loop──► (stable)
                                │
                        destroy() │
                                ▼
                           DESTROYED

   Error at init() or render() ──► ERROR  (triggers FR-011 recovery)
```

---

## Error Types

```kotlin
class EffectInitException(message: String, cause: Throwable? = null)
    : Exception(message, cause)
// Thrown by EffectRenderer.init() on shader compilation failure or
// GL resource allocation failure. Caught by GlSurfaceRenderer,
// which emits an error state to EffectViewModel for FR-011 handling.
```
