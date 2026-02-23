package com.twodgfxapp.renderer

import android.content.Context
import com.twodgfxapp.model.EffectDescriptor
import com.twodgfxapp.renderer.effects.BlobFieldEffect
import com.twodgfxapp.renderer.effects.BouncingBallsEffect
import com.twodgfxapp.renderer.effects.BurningShipEffect
import com.twodgfxapp.renderer.effects.CheckerTwistEffect
import com.twodgfxapp.renderer.effects.CopperBarsEffect
import com.twodgfxapp.renderer.effects.DotTunnelEffect
import com.twodgfxapp.renderer.effects.FireEffect
import com.twodgfxapp.renderer.effects.FireworksEffect
import com.twodgfxapp.renderer.effects.GameOfLifeEffect
import com.twodgfxapp.renderer.effects.GooeyBlobsEffect
import com.twodgfxapp.renderer.effects.HeatHazeEffect
import com.twodgfxapp.renderer.effects.JuliaSetEffect
import com.twodgfxapp.renderer.effects.KaleidoscopeEffect
import com.twodgfxapp.renderer.effects.LensZoomEffect
import com.twodgfxapp.renderer.effects.MandelbrotEffect
import com.twodgfxapp.renderer.effects.MetaballsEffect
import com.twodgfxapp.renderer.effects.ParticlesEffect
import com.twodgfxapp.renderer.effects.PlasmaEffect
import com.twodgfxapp.renderer.effects.RasterBarsEffect
import com.twodgfxapp.renderer.effects.RotozoomEffect
import com.twodgfxapp.renderer.effects.ShockwaveEffect
import com.twodgfxapp.renderer.effects.SineScrollerEffect
import com.twodgfxapp.renderer.effects.SnowEffect
import com.twodgfxapp.renderer.effects.SphereEffect
import com.twodgfxapp.renderer.effects.SpinningCubeEffect
import com.twodgfxapp.renderer.effects.StarfieldEffect
import com.twodgfxapp.renderer.effects.TunnelEffect
import com.twodgfxapp.renderer.effects.VoxelLandscapeEffect
import com.twodgfxapp.renderer.effects.WaterRippleEffect

/**
 * Maps an [EffectDescriptor] to its [EffectRenderer] implementation.
 *
 * Returns null for unknown IDs (caller should treat null as an unrecoverable error).
 * Note: the returned object is a plain Kotlin object with no GL state yet.
 * GL initialisation happens later in [GlSurfaceRenderer.onSurfaceChanged].
 */
object EffectRendererFactory {

    fun create(context: Context, descriptor: EffectDescriptor): EffectRenderer? =
        when (descriptor.id) {
            // ── Raster / Plasma ───────────────────────────────────────────
            "plasma"        -> PlasmaEffect(context)
            "copper-bars"   -> CopperBarsEffect(context)
            "sine-scroller" -> SineScrollerEffect(context)
            "raster-bars"   -> RasterBarsEffect(context)
            "checker-twist" -> CheckerTwistEffect(context)

            // ── Tunnel & 3D ───────────────────────────────────────────────
            "tunnel"          -> TunnelEffect(context)
            "voxel-landscape" -> VoxelLandscapeEffect(context)
            "dot-tunnel"      -> DotTunnelEffect(context)
            "sphere"          -> SphereEffect(context)

            // ── Blobs & Metaballs ─────────────────────────────────────────
            "metaballs"   -> MetaballsEffect(context)
            "blob-field"  -> BlobFieldEffect(context)
            "gooey-blobs" -> GooeyBlobsEffect(context)

            // ── Particles ─────────────────────────────────────────────────
            "particles" -> ParticlesEffect(context)
            "fireworks" -> FireworksEffect(context)
            "starfield" -> StarfieldEffect(context)
            "snow"      -> SnowEffect(context)
            "fire"      -> FireEffect(context)

            // ── Distortion ────────────────────────────────────────────────
            "water-ripple" -> WaterRippleEffect(context)
            "lens-zoom"    -> LensZoomEffect(context)
            "heat-haze"    -> HeatHazeEffect(context)
            "shockwave"    -> ShockwaveEffect(context)

            // ── Rotozoom ──────────────────────────────────────────────────
            "rotozoom"      -> RotozoomEffect(context)
            "spinning-cube" -> SpinningCubeEffect(context)
            "kaleidoscope"  -> KaleidoscopeEffect(context)

            // ── 2D Classics ───────────────────────────────────────────────
            "bouncing-balls" -> BouncingBallsEffect(context)
            "game-of-life"   -> GameOfLifeEffect(context)

            // ── Fractals ──────────────────────────────────────────────────
            "mandelbrot"   -> MandelbrotEffect(context)
            "julia-set"    -> JuliaSetEffect(context)
            "burning-ship" -> BurningShipEffect(context)

            else -> null
        }
}
