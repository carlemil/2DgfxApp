package com.twodgfxapp.model

/**
 * Immutable descriptor for a single visual effect.
 *
 * @param id           Unique slug used as navigation argument (e.g. "plasma", "copper-bars").
 * @param name         Human-readable display name shown in the menu.
 * @param category     One of the eight [EffectCategory] values.
 * @param touchSupport True if the effect responds to single-touch / drag input.
 * @param multiTouch   True if the effect responds to two or more simultaneous touch points.
 * @param tiltSupport  True if the effect responds to accelerometer tilt events.
 */
data class EffectDescriptor(
    val id:           String,
    val name:         String,
    val category:     EffectCategory,
    val touchSupport: Boolean,
    val multiTouch:   Boolean,
    val tiltSupport:  Boolean,
)
