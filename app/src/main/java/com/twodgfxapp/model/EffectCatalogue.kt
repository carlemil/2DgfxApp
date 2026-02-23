package com.twodgfxapp.model

/**
 * Immutable, compile-time catalogue of all 29 bundled effects.
 * The list order determines the order shown in the menu.
 */
object EffectCatalogue {

    val effects: List<EffectDescriptor> = listOf(
        // ── Raster / Plasma ──────────────────────────────────────────────────
        EffectDescriptor("plasma",        "Plasma",        EffectCategory.RASTER_PLASMA, false, false, false),
        EffectDescriptor("copper-bars",   "Copper Bars",   EffectCategory.RASTER_PLASMA, false, false, false),
        EffectDescriptor("sine-scroller", "Sine Scroller", EffectCategory.RASTER_PLASMA, false, false, false),
        EffectDescriptor("raster-bars",   "Raster Bars",   EffectCategory.RASTER_PLASMA, false, false, false),
        EffectDescriptor("checker-twist", "Checker Twist", EffectCategory.RASTER_PLASMA, true,  false, false),

        // ── Tunnel & 3D ───────────────────────────────────────────────────────
        EffectDescriptor("tunnel",          "Tunnel",          EffectCategory.TUNNEL_3D, true,  false, true),
        EffectDescriptor("voxel-landscape", "Voxel Landscape", EffectCategory.TUNNEL_3D, true,  false, true),
        EffectDescriptor("dot-tunnel",      "Dot Tunnel",      EffectCategory.TUNNEL_3D, true,  false, false),
        EffectDescriptor("sphere",          "Sphere",          EffectCategory.TUNNEL_3D, true,  false, true),

        // ── Blobs & Metaballs ────────────────────────────────────────────────
        EffectDescriptor("metaballs",  "Metaballs",  EffectCategory.BLOBS_METABALLS, true, true,  false),
        EffectDescriptor("blob-field", "Blob Field", EffectCategory.BLOBS_METABALLS, true, false, false),
        EffectDescriptor("gooey-blobs","Gooey Blobs",EffectCategory.BLOBS_METABALLS, true, true,  false),

        // ── Particles ────────────────────────────────────────────────────────
        EffectDescriptor("particles", "Particles", EffectCategory.PARTICLES, true,  false, true),
        EffectDescriptor("fireworks", "Fireworks", EffectCategory.PARTICLES, true,  false, false),
        EffectDescriptor("starfield", "Starfield", EffectCategory.PARTICLES, false, false, true),
        EffectDescriptor("snow",      "Snow",      EffectCategory.PARTICLES, false, false, true),
        EffectDescriptor("fire",      "Fire",      EffectCategory.PARTICLES, true,  false, false),

        // ── Distortion ───────────────────────────────────────────────────────
        EffectDescriptor("water-ripple", "Water Ripple", EffectCategory.DISTORTION, true, true,  false),
        EffectDescriptor("lens-zoom",    "Lens Zoom",    EffectCategory.DISTORTION, true, false, false),
        EffectDescriptor("heat-haze",    "Heat Haze",    EffectCategory.DISTORTION, false,false, true),
        EffectDescriptor("shockwave",    "Shockwave",    EffectCategory.DISTORTION, true, false, false),

        // ── Rotozoom ─────────────────────────────────────────────────────────
        EffectDescriptor("rotozoom",      "Rotozoom",      EffectCategory.ROTOZOOM, true, false, false),
        EffectDescriptor("spinning-cube", "Spinning Cube", EffectCategory.ROTOZOOM, true, false, true),
        EffectDescriptor("kaleidoscope",  "Kaleidoscope",  EffectCategory.ROTOZOOM, true, false, false),

        // ── 2D Classics ──────────────────────────────────────────────────────
        EffectDescriptor("bouncing-balls", "Bouncing Balls", EffectCategory.CLASSICS_2D, true, false, false),
        EffectDescriptor("game-of-life",   "Game of Life",   EffectCategory.CLASSICS_2D, true, false, false),

        // ── Fractals ─────────────────────────────────────────────────────────
        EffectDescriptor("mandelbrot",   "Mandelbrot",   EffectCategory.FRACTALS, true, true, false),
        EffectDescriptor("julia-set",    "Julia Set",    EffectCategory.FRACTALS, true, true, false),
        EffectDescriptor("burning-ship", "Burning Ship", EffectCategory.FRACTALS, true, true, false),
    )

    /** Returns the effects grouped by category, preserving insertion order within each group. */
    fun byCategory(): Map<EffectCategory, List<EffectDescriptor>> =
        effects.groupBy { it.category }

    /** Looks up an effect by its [id] slug. Returns null if not found. */
    fun findById(id: String): EffectDescriptor? = effects.firstOrNull { it.id == id }
}
