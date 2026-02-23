package com.twodgfxapp.input

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.view.MotionEvent
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InputManagerTest {

    private lateinit var sensorManager:  SensorManager
    private lateinit var mockContext:    Context
    private val emittedEvents = mutableListOf<InputEvent>()

    @BeforeEach
    fun setUp() {
        emittedEvents.clear()
        sensorManager = mockk(relaxed = true)
        mockContext   = mockk(relaxed = true)
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns sensorManager
    }

    private fun makeManager(hasAccelerometer: Boolean): InputManager {
        val sensor: Sensor? = if (hasAccelerometer) mockk(relaxed = true) else null
        every { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) } returns sensor
        return InputManager(mockContext) { event -> emittedEvents.add(event) }
    }

    // ── Touch events ───────────────────────────────────────────────────────

    @Test
    fun `ACTION_DOWN emits Touch DOWN with normalised coordinates`() {
        val manager = makeManager(hasAccelerometer = false)
        manager.updateSurfaceSize(1080, 1920)

        val motionEvent = motionEvent(MotionEvent.ACTION_DOWN, x = 540f, y = 960f, pointerId = 0)
        manager.onTouchEvent(motionEvent)

        assertEquals(1, emittedEvents.size)
        val touch = emittedEvents[0] as InputEvent.Touch
        assertEquals(TouchAction.DOWN, touch.action)
        assertEquals(0.5f, touch.x, 0.01f)
        assertEquals(0.5f, touch.y, 0.01f)
        assertEquals(0, touch.pointerId)
    }

    @Test
    fun `ACTION_UP emits Touch UP`() {
        val manager = makeManager(hasAccelerometer = false)
        manager.updateSurfaceSize(100, 200)

        manager.onTouchEvent(motionEvent(MotionEvent.ACTION_DOWN, x = 50f, y = 100f, pointerId = 0))
        emittedEvents.clear()

        manager.onTouchEvent(motionEvent(MotionEvent.ACTION_UP, x = 50f, y = 100f, pointerId = 0))
        assertEquals(1, emittedEvents.size)
        assertEquals(TouchAction.UP, (emittedEvents[0] as InputEvent.Touch).action)
    }

    @Test
    fun `ACTION_MOVE emits Touch MOVE for each pointer`() {
        val manager = makeManager(hasAccelerometer = false)
        manager.updateSurfaceSize(100, 100)

        val moveEvent = motionEvent(MotionEvent.ACTION_MOVE, x = 25f, y = 75f, pointerId = 0)
        manager.onTouchEvent(moveEvent)

        assertTrue(emittedEvents.any { it is InputEvent.Touch && it.action == TouchAction.MOVE })
    }

    @Test
    fun `ACTION_CANCEL emits Touch UP for each pointer`() {
        val manager = makeManager(hasAccelerometer = false)
        manager.updateSurfaceSize(100, 100)

        val cancelEvent = motionEvent(MotionEvent.ACTION_CANCEL, x = 0f, y = 0f, pointerId = 0)
        manager.onTouchEvent(cancelEvent)

        assertTrue(emittedEvents.any { it is InputEvent.Touch && it.action == TouchAction.UP })
    }

    @Test
    fun `coordinates are clamped to 0-1 range`() {
        val manager = makeManager(hasAccelerometer = false)
        manager.updateSurfaceSize(100, 100)

        // Overshoot the surface edges
        manager.onTouchEvent(motionEvent(MotionEvent.ACTION_DOWN, x = 200f, y = -50f, pointerId = 0))
        val touch = emittedEvents[0] as InputEvent.Touch
        assertEquals(1.0f, touch.x)
        assertEquals(0.0f, touch.y)
    }

    // ── Tilt events ────────────────────────────────────────────────────────

    @Test
    fun `onSensorChanged emits Tilt with normalised values`() {
        val mockSensor = mockk<Sensor>(relaxed = true)
        every { mockSensor.type } returns Sensor.TYPE_ACCELEROMETER
        every { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) } returns mockSensor

        val manager = InputManager(mockContext) { event -> emittedEvents.add(event) }

        val sensorEvent = fakeSensorEvent(mockSensor, floatArrayOf(9.81f, 0f, 0f))
        manager.onSensorChanged(sensorEvent)

        assertEquals(1, emittedEvents.size)
        val tilt = emittedEvents[0] as InputEvent.Tilt
        assertEquals(1.0f, tilt.x, 0.05f)
        assertEquals(0.0f, tilt.y, 0.05f)
    }

    // ── No accelerometer ───────────────────────────────────────────────────

    @Test
    fun `no events emitted when accelerometer absent and no touch`() {
        val manager = makeManager(hasAccelerometer = false)
        manager.start()  // should not crash
        assertTrue(emittedEvents.isEmpty())
    }

    // ── applyEvent integration ─────────────────────────────────────────────

    @Test
    fun `applyEvent DOWN then UP results in empty touchPoints`() {
        var state = InputState(emptyList(), null)
        state = state.applyEvent(InputEvent.Touch(0.5f, 0.5f, TouchAction.DOWN, 0))
        assertEquals(1, state.touchPoints.size)
        state = state.applyEvent(InputEvent.Touch(0.5f, 0.5f, TouchAction.UP, 0))
        assertTrue(state.touchPoints.isEmpty())
    }

    @Test
    fun `applyEvent Tilt updates tilt value`() {
        var state = InputState(emptyList(), null)
        state = state.applyEvent(InputEvent.Tilt(0.1f, 0.2f, 0.9f))
        assertEquals(0.1f, state.tilt!!.x, 0.001f)
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun motionEvent(
        action:    Int,
        x:         Float,
        y:         Float,
        pointerId: Int,
    ): MotionEvent {
        val event = mockk<MotionEvent>(relaxed = true)
        every { event.actionMasked } returns action
        every { event.actionIndex }  returns 0
        every { event.pointerCount } returns 1
        every { event.getX(0) }      returns x
        every { event.getY(0) }      returns y
        every { event.getPointerId(0) } returns pointerId
        return event
    }

    private fun fakeSensorEvent(sensor: Sensor, values: FloatArray): SensorEvent {
        // SensorEvent's constructor is package-private and absent from the SDK stub.
        // Allocate an instance without calling any constructor via sun.misc.Unsafe
        // (accessed through reflection), then write the final `values` field directly.
        val unsafeClass  = Class.forName("sun.misc.Unsafe")
        val unsafe       = unsafeClass.getDeclaredField("theUnsafe")
            .apply { isAccessible = true }.get(null)
        val allocate     = unsafeClass.getMethod("allocateInstance", Class::class.java)
        val event        = allocate.invoke(unsafe, SensorEvent::class.java) as SensorEvent
        val valuesField  = SensorEvent::class.java.getDeclaredField("values")
        val fieldOffset  = unsafeClass.getMethod("objectFieldOffset", java.lang.reflect.Field::class.java)
            .invoke(unsafe, valuesField) as Long
        unsafeClass.getMethod("putObject", Any::class.java, Long::class.javaPrimitiveType, Any::class.java)
            .invoke(unsafe, event, fieldOffset, values)
        event.sensor = sensor
        return event
    }
}
