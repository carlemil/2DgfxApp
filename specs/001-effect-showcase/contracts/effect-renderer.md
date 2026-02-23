# Contract: EffectRenderer Interface

**Feature**: `001-effect-showcase` | **Date**: 2026-02-22

---

## Interface Definition

```kotlin
interface EffectRenderer {
    suspend fun init(width: Int, height: Int)
    fun render(dt: Float, input: InputState)
    fun destroy()
}
```

---

## Method Contracts

### `init(width: Int, height: Int)`

| Attribute | Value |
|-----------|-------|
| Thread | GL thread (called by `GLSurfaceView.Renderer.onSurfaceCreated` / `onSurfaceChanged`) |
| Called | Once, after the GL surface is created and dimensions are known |
| Suspension | May suspend; runs inside a coroutine context bridged from the GL thread |
| Success | Compiles GLSL shaders, allocates GL buffers/textures, stores program handle |
| Failure | Throws `EffectInitException` with descriptive message |

**Preconditions**:
- A valid EGL context is current on the calling thread.
- `width > 0` and `height > 0`.

**Postconditions**:
- On success: renderer is in `READY` state; subsequent `render()` calls are valid.
- On `EffectInitException`: renderer is in `ERROR` state; `GlSurfaceRenderer`
  catches the exception and emits an error event to `EffectViewModel`, which
  triggers the FR-011 error overlay and navigates back to the menu.

**Must NOT**:
- Block the UI/main thread.
- Allocate large heap objects that survive past `destroy()` (prevent memory leaks).

---

### `render(dt: Float, input: InputState)`

| Attribute | Value |
|-----------|-------|
| Thread | GL thread (called by `GLSurfaceView.Renderer.onDrawFrame`) |
| Called | Every frame while the surface is active |
| `dt` | Elapsed wall-clock seconds since the previous `render()` call; target ≈ 0.01667 s (60 fps). Always > 0. |

**Preconditions**:
- `init()` completed successfully (renderer is in `READY` state).
- A valid EGL context is current on the calling thread.

**Postconditions**:
- One complete frame has been drawn to the current framebuffer.
- All uniform values (`u_time`, `u_resolution`, `u_touch`, `u_tilt`) reflect the
  current `input` snapshot.

**Performance requirements**:
- Must complete within **12ms** to preserve the 16.67ms frame budget.
- **Must NOT allocate heap objects** — no `new`, no collection creation, no
  lambda captures with closures, no string formatting. GC pauses on the GL
  thread cause dropped frames.

**Must NOT**:
- Call `GLSurfaceView.requestRender()` (render mode is `RENDERMODE_CONTINUOUSLY`).
- Modify `InputState` (it is immutable).
- Throw exceptions; uncaught exceptions on the GL thread crash the app.

---

### `destroy()`

| Attribute | Value |
|-----------|-------|
| Thread | GL thread (called by `GlSurfaceRenderer` before surface destruction) |
| Called | Once, when the effect is being unloaded or the surface is destroyed |

**Postconditions**:
- All GL resources are released: `glDeleteProgram`, `glDeleteBuffers`,
  `glDeleteTextures`, `glDeleteVertexArrays`.
- Renderer is in `DESTROYED` state; calling `render()` afterwards is undefined
  behaviour (callers must not do this).

**Must NOT**:
- Throw exceptions.
- Leave any GL handles dangling (prevents resource leaks across effect switches).

---

## State Machine

```
         init(w, h)              render() × N           destroy()
CREATED ──────────► READY ─────────────────────────► DESTROYED
                      │
           EffectInitException
                      │
                      ▼
                    ERROR  ──► (FR-011: error overlay, navigate to menu)
```

Callers (`GlSurfaceRenderer`) are responsible for:
1. Not calling `render()` before `init()` completes successfully.
2. Calling `destroy()` exactly once before discarding the renderer instance.
3. Catching `EffectInitException` from `init()` and routing it to `EffectViewModel`.

---

## Testing

`EffectRenderer` implementations that use only CPU-side logic (no GL calls) can
be unit-tested with JUnit 5. Implementations that make GL calls require
instrumented tests running on an actual or emulated GPU (androidTest).

Rendering output correctness cannot be deterministically asserted in unit tests
(GPU-side output is hardware-dependent); testing focuses on:
- `init()` completes without throwing for a valid GL context mock.
- `render()` does not throw with representative `InputState` values.
- `destroy()` releases resources (verified via GL error checking in instrumented tests).
