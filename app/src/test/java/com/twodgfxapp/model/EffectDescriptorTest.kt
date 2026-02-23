package com.twodgfxapp.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EffectDescriptorTest {

    private val plasma = EffectDescriptor(
        id           = "plasma",
        name         = "Plasma",
        category     = EffectCategory.RASTER_PLASMA,
        touchSupport = false,
        multiTouch   = false,
        tiltSupport  = false,
    )

    @Test
    fun `all fields are readable`() {
        assertEquals("plasma",                   plasma.id)
        assertEquals("Plasma",                   plasma.name)
        assertEquals(EffectCategory.RASTER_PLASMA, plasma.category)
        assertFalse(plasma.touchSupport)
        assertFalse(plasma.multiTouch)
        assertFalse(plasma.tiltSupport)
    }

    @Test
    fun `data class equality compares all fields`() {
        val clone = plasma.copy()
        assertEquals(plasma, clone)
    }

    @Test
    fun `data class inequality when any field differs`() {
        assertNotEquals(plasma, plasma.copy(id = "other"))
        assertNotEquals(plasma, plasma.copy(name = "Other"))
        assertNotEquals(plasma, plasma.copy(category = EffectCategory.FRACTALS))
        assertNotEquals(plasma, plasma.copy(touchSupport = true))
        assertNotEquals(plasma, plasma.copy(multiTouch = true))
        assertNotEquals(plasma, plasma.copy(tiltSupport = true))
    }

    @Test
    fun `copy returns independent instance with overridden field`() {
        val withTouch = plasma.copy(touchSupport = true)
        assertTrue(withTouch.touchSupport)
        assertFalse(plasma.touchSupport)    // original unchanged
    }

    @Test
    fun `hashCode is consistent with equals`() {
        val clone = plasma.copy()
        assertEquals(plasma.hashCode(), clone.hashCode())
    }

    @Test
    fun `toString contains key fields`() {
        val str = plasma.toString()
        assertTrue(str.contains("plasma"))
        assertTrue(str.contains("Plasma"))
    }
}
