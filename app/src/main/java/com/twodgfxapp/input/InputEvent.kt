package com.twodgfxapp.input

/** A discrete input signal from the user — either a touch pointer event or a tilt (accelerometer) reading. */
sealed class InputEvent {

    /**
     * A touch pointer event.
     *
     * @param x         Normalised horizontal position in [0, 1] (0 = left edge).
     * @param y         Normalised vertical position in [0, 1] (0 = top edge).
     * @param action    The pointer action: [TouchAction.DOWN], [TouchAction.MOVE], or [TouchAction.UP].
     * @param pointerId Stable Android pointer ID (not the pointer index).
     */
    data class Touch(
        val x:         Float,
        val y:         Float,
        val action:    TouchAction,
        val pointerId: Int,
    ) : InputEvent()

    /**
     * An accelerometer tilt sample.
     * All values are normalised by dividing raw output by `SensorManager.GRAVITY_EARTH` (9.81 m/s²).
     *
     * @param x Lateral tilt in [-1, 1].
     * @param y Forward/backward tilt in [-1, 1].
     * @param z Vertical component in [-1, 1].
     */
    data class Tilt(
        val x: Float,
        val y: Float,
        val z: Float,
    ) : InputEvent()
}

enum class TouchAction { DOWN, MOVE, UP }

/**
 * Immutable snapshot of all active input at a given point in time.
 * Constructed every frame by [com.twodgfxapp.ui.effect.EffectViewModel]
 * and consumed by [com.twodgfxapp.renderer.EffectRenderer.render].
 */
data class InputState(
    val touchPoints: List<InputEvent.Touch>,   // all currently active (DOWN/MOVE) touch points
    val tilt:        InputEvent.Tilt?,         // null when accelerometer is unavailable
)

/** Returns a new [InputState] with [event] applied. */
fun InputState.applyEvent(event: InputEvent): InputState = when (event) {
    is InputEvent.Touch -> when (event.action) {
        TouchAction.DOWN, TouchAction.MOVE -> {
            val updated = touchPoints.filter { it.pointerId != event.pointerId } + event
            copy(touchPoints = updated)
        }
        TouchAction.UP -> copy(touchPoints = touchPoints.filter { it.pointerId != event.pointerId })
    }
    is InputEvent.Tilt -> copy(tilt = event)
}
