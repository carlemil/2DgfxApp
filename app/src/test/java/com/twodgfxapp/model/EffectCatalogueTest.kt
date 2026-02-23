package com.twodgfxapp.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EffectCatalogueTest {

    @Test
    fun `effects list contains exactly 29 entries`() {
        assertEquals(29, EffectCatalogue.effects.size)
    }

    @Test
    fun `all effect IDs are unique`() {
        val ids = EffectCatalogue.effects.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Duplicate effect IDs detected: $ids")
    }

    @Test
    fun `byCategory returns all 8 categories`() {
        val grouped = EffectCatalogue.byCategory()
        val expectedCategories = EffectCategory.entries.toSet()
        assertEquals(expectedCategories, grouped.keys)
    }

    @Test
    fun `each effect appears in exactly one category bucket`() {
        val grouped = EffectCatalogue.byCategory()
        val allInBuckets = grouped.values.flatten()
        assertEquals(EffectCatalogue.effects.size, allInBuckets.size)
        EffectCatalogue.effects.forEach { effect ->
            assertTrue(
                allInBuckets.any { it.id == effect.id },
                "Effect ${effect.id} missing from byCategory() output",
            )
        }
    }

    @Test
    fun `byCategory bucket contains effects matching that category`() {
        val grouped = EffectCatalogue.byCategory()
        grouped.forEach { (category, effects) ->
            effects.forEach { effect ->
                assertEquals(category, effect.category, "Effect ${effect.id} in wrong bucket")
            }
        }
    }

    @Test
    fun `byCategory preserves insertion order within each group`() {
        val grouped = EffectCatalogue.byCategory()
        val plasmaGroup = grouped[EffectCategory.RASTER_PLASMA] ?: error("RASTER_PLASMA missing")
        // First effect registered in the Raster/Plasma category should be "plasma"
        assertEquals("plasma", plasmaGroup.first().id)
    }

    @Test
    fun `findById returns correct descriptor for known ID`() {
        val descriptor = EffectCatalogue.findById("plasma")
        assertNotNull(descriptor)
        assertEquals("plasma", descriptor!!.id)
        assertEquals("Plasma", descriptor.name)
    }

    @Test
    fun `findById returns null for unknown ID`() {
        assertNull(EffectCatalogue.findById("does-not-exist"))
    }

    @Test
    fun `all effects have non-blank ID and name`() {
        EffectCatalogue.effects.forEach { effect ->
            assertTrue(effect.id.isNotBlank(),   "Blank ID for effect: $effect")
            assertTrue(effect.name.isNotBlank(), "Blank name for effect: $effect")
        }
    }
}
