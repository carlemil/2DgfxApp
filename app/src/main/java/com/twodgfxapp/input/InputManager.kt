package com.twodgfxapp.input

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_POINTER_DOWN
import android.view.MotionEvent.ACTION_POINTER_UP
import android.view.MotionEvent.ACTION_UP

/**
 * Merges accelerometer tilt events and GLSurfaceView touch events into a single callback stream.
 *
 * Touch coordinates are normalised to [0, 1] relative to the GLSurfaceView dimensions.
 * Tilt values are normalised to [-1, 1] by dividing raw accelerometer output by
 * [SensorManager.GRAVITY_EARTH] (9.81 m/s²).
 *
 * When the accelerometer is absent the [onEvent] callback simply never receives [InputEvent.Tilt]
 * items — no error, no exception.
 *
 * @param context Context used to obtain the [SensorManager].
 * @param onEvent Callback invoked for each incoming [InputEvent]. May be called from any thread.
 */
class InputManager(
    context:           Context,
    private val onEvent: (InputEvent) -> Unit,
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Surface dimensions used for touch normalisation (updated by GLSurfaceView)
    @Volatile private var surfaceWidth  = 1f
    @Volatile private var surfaceHeight = 1f

    /** Register sensor listener. Call from [EffectScreen] via DisposableEffect.start. */
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    /** Unregister all listeners and release resources. Call from [EffectScreen] via DisposableEffect.onDispose. */
    fun destroy() {
        sensorManager.unregisterListener(this)
    }

    /** Update surface dimensions so touch coordinates can be normalised. */
    fun updateSurfaceSize(width: Int, height: Int) {
        surfaceWidth  = width.toFloat().coerceAtLeast(1f)
        surfaceHeight = height.toFloat().coerceAtLeast(1f)
    }

    /**
     * Forward a [MotionEvent] from the [GLSurfaceView] touch listener.
     * Extracts all active pointers and emits a [InputEvent.Touch] per pointer.
     */
    fun onTouchEvent(event: MotionEvent) {
        val w = surfaceWidth
        val h = surfaceHeight

        when (val maskedAction = event.actionMasked) {
            ACTION_DOWN, ACTION_POINTER_DOWN -> {
                val idx = if (maskedAction == ACTION_DOWN) 0 else event.actionIndex
                emit(event, idx, TouchAction.DOWN, w, h)
            }
            ACTION_MOVE -> {
                for (idx in 0 until event.pointerCount) {
                    emit(event, idx, TouchAction.MOVE, w, h)
                }
            }
            ACTION_UP, ACTION_POINTER_UP -> {
                val idx = if (maskedAction == ACTION_UP) 0 else event.actionIndex
                emit(event, idx, TouchAction.UP, w, h)
            }
            ACTION_CANCEL -> {
                // Treat cancel as UP for all active pointers
                for (idx in 0 until event.pointerCount) {
                    emit(event, idx, TouchAction.UP, w, h)
                }
            }
        }
    }

    // ── SensorEventListener ────────────────────────────────────────────────

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val gravity = SensorManager.GRAVITY_EARTH
            onEvent(
                InputEvent.Tilt(
                    x = (event.values[0] / gravity).coerceIn(-1f, 1f),
                    y = (event.values[1] / gravity).coerceIn(-1f, 1f),
                    z = (event.values[2] / gravity).coerceIn(-1f, 1f),
                )
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun emit(
        event:  MotionEvent,
        index:  Int,
        action: TouchAction,
        w:      Float,
        h:      Float,
    ) {
        onEvent(
            InputEvent.Touch(
                x         = (event.getX(index) / w).coerceIn(0f, 1f),
                y         = (event.getY(index) / h).coerceIn(0f, 1f),
                action    = action,
                pointerId = event.getPointerId(index),
            )
        )
    }
}
