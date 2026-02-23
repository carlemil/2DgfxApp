# Contract: InputManager Flow

**Feature**: `001-effect-showcase` | **Date**: 2026-02-22

---

## Overview

`InputManager` exposes a single hot `SharedFlow<InputEvent>` that merges two
input streams: accelerometer tilt events and touch events from the
`GLSurfaceView`.

```kotlin
class InputManager(
    private val context: Context,
    private val sensorManager: SensorManager
) {
    val events: SharedFlow<InputEvent>   // hot, replay = 0
}
```

---

## Flow Properties

| Property | Value |
|----------|-------|
| Type | `SharedFlow<InputEvent>` |
| Replay | 0 (no buffering; late subscribers miss past events) |
| Extra buffer capacity | 64 events (absorbs burst on rapid touch moves) |
| On buffer overflow | `DROP_OLDEST` (drops stale events rather than suspending emitter) |
| Emission thread | `Dispatchers.Default` |
| Backpressure | Handled by overflow policy; callers must not suspend in collection |

---

## Touch Event Contract

- Emits `InputEvent.Touch` for every `MotionEvent` received by the
  `GLSurfaceView`'s touch listener.
- Pointer actions covered: `ACTION_DOWN`, `ACTION_POINTER_DOWN`, `ACTION_MOVE`,
  `ACTION_UP`, `ACTION_POINTER_UP`, `ACTION_CANCEL`.
- `ACTION_CANCEL` is treated as `TouchAction.UP` for all active pointers.
- `x` and `y` values are normalised to `[0, 1]` relative to the surface dimensions.
- `pointerId` is the stable Android pointer ID (not the pointer index).
- Events for all pointers in a multi-touch event are emitted as separate
  `InputEvent.Touch` items in a single atomic batch (within the same frame).

---

## Tilt Event Contract

- Emits `InputEvent.Tilt` from `SensorManager.TYPE_ACCELEROMETER` callbacks.
- Registration rate: `SENSOR_DELAY_GAME` (~20ms interval, ≤ 50 Hz).
- Values normalised to `[-1, 1]` by dividing raw accelerometer output by
  `SensorManager.GRAVITY_EARTH` (9.81 m/s²).
- When the accelerometer sensor is **not available** on the device:
  - No `Tilt` events are ever emitted.
  - No error event is emitted.
  - No exception is thrown.
  - Collectors simply never receive `Tilt` items; `InputState.tilt` remains `null`.

---

## Lifecycle Contract

| Phase | Behaviour |
|-------|-----------|
| `onStart` | Sensor listener registered; touch listener active |
| `onStop` | Sensor listener unregistered; touch listener still attached to view but events discarded (flow has no collectors) |
| Surface destroyed | Touch listener detached from `GLSurfaceView` |
| `InputManager.destroy()` | All listeners unregistered; `SharedFlow` completed |

`InputManager` is designed to be collected under `repeatOnLifecycle(STARTED)`
so that the coroutine collection cancels automatically on `onStop`.

---

## Consumption Contract

`EffectViewModel` collects `InputManager.events` and builds an `InputState`
snapshot:

```kotlin
private val _inputState = AtomicReference(InputState(emptyList(), null))

lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        inputManager.events.collect { event ->
            _inputState.updateAndGet { current -> current.applyEvent(event) }
        }
    }
}
```

The `AtomicReference` allows the GL thread to read the latest `InputState`
without blocking:

```kotlin
// Called on GL thread inside render():
val input = _inputState.get()
effectRenderer.render(dt, input)
```

---

## Testing

`InputManager` is unit-testable with Turbine:

```kotlin
@Test
fun `emits Touch DOWN when motion event received`() = runTest {
    val manager = InputManager(fakeSensorManager(hasAccelerometer = true))
    manager.events.test {
        manager.onTouchEvent(motionEvent(ACTION_DOWN, x = 0.5f, y = 0.5f))
        val event = awaitItem()
        assertThat(event).isInstanceOf(InputEvent.Touch::class.java)
        assertThat((event as InputEvent.Touch).action).isEqualTo(TouchAction.DOWN)
    }
}

@Test
fun `emits no Tilt events when sensor unavailable`() = runTest {
    val manager = InputManager(fakeSensorManager(hasAccelerometer = false))
    manager.events.test {
        expectNoEvents()
    }
}
```
