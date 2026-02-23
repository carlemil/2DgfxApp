package com.twodgfxapp.renderer

import com.twodgfxapp.input.InputState

/**
 * Contract for a single GPU-accelerated visual effect.
 *
 * All methods are called on the **GL thread** by [GlSurfaceRenderer].
 * Implementations MUST NOT:
 *  - block the main / UI thread
 *  - allocate heap objects inside [render] (causes GC pressure on the GL thread)
 *  - throw exceptions from [render] or [destroy] (use [EffectInitException] from [init] only)
 */
interface EffectRenderer {

    /**
     * One-time initialisation: compile shaders, allocate GL buffers / textures.
     *
     * Called on the GL thread once the EGL surface is ready and dimensions are known.
     * May suspend briefly (e.g. to read raw resources); bridge via runBlocking in caller.
     *
     * @throws EffectInitException on shader compile / link failure or resource allocation failure.
     */
    suspend fun init(width: Int, height: Int)

    /**
     * Draw one frame to the current framebuffer.
     *
     * @param dt    Elapsed seconds since the previous call (target ≈ 0.01667 s for 60 fps). Always > 0.
     * @param input Current input snapshot — immutable, do NOT modify.
     */
    fun render(dt: Float, input: InputState)

    /** Release all GL resources. Called once before the surface is destroyed. */
    fun destroy()
}

/** Thrown by [EffectRenderer.init] when shader compilation or GL resource setup fails. */
class EffectInitException(message: String, cause: Throwable? = null) : Exception(message, cause)
