package com.twodgfxapp.renderer

import android.content.Context
import android.opengl.GLES30
import androidx.annotation.RawRes
import com.twodgfxapp.R
import com.twodgfxapp.input.InputState

/**
 * Base class for all fullscreen GLSL shader effects.
 *
 * Subclasses only need to:
 * 1. Pass the fragment shader raw resource ID to the constructor.
 * 2. Override [onInit] to cache any extra uniform locations or allocate effect-specific buffers.
 * 3. Override [onRender] for any effect-specific pre-draw work (e.g., updating particle data).
 *
 * Standard uniforms set every frame:
 *   - `u_time`       (float)  — total elapsed seconds
 *   - `u_resolution` (vec2)   — surface width × height in pixels
 *   - `u_touch`      (vec2)   — first active touch point in [0,1] or (0.5, 0.5) if none
 *   - `u_tilt`       (vec3)   — normalised accelerometer x/y/z in [-1,1] or (0,0,0) if absent
 *
 * Draw: `glDrawArrays(GL_TRIANGLES, 0, 3)` — matches the fullscreen.vert vertex shader.
 *
 * ⚠ render() MUST NOT allocate heap objects (no collections, no lambdas with closures, no strings).
 */
abstract class BaseShaderEffect(
    private val context:     Context,
    @RawRes private val fragResId: Int,
) : EffectRenderer {

    protected var program      = 0
    private   var uTimeLoc     = -1
    private   var uResLoc      = -1
    private   var uTouchLoc    = -1
    private   var uTiltLoc     = -1

    protected var surfaceWidth  = 0
    protected var surfaceHeight = 0

    // Accumulated time — updated each render() call
    private var elapsedTime = 0f

    // ── EffectRenderer API ─────────────────────────────────────────────────

    final override suspend fun init(width: Int, height: Int) {
        surfaceWidth  = width
        surfaceHeight = height

        program    = GlUtils.compileProgram(context, R.raw.fullscreen, fragResId)
        uTimeLoc   = GLES30.glGetUniformLocation(program, "u_time")
        uResLoc    = GLES30.glGetUniformLocation(program, "u_resolution")
        uTouchLoc  = GLES30.glGetUniformLocation(program, "u_touch")
        uTiltLoc   = GLES30.glGetUniformLocation(program, "u_tilt")

        onInit(width, height)
    }

    final override fun render(dt: Float, input: InputState) {
        elapsedTime += dt

        GLES30.glUseProgram(program)

        GLES30.glUniform1f(uTimeLoc,  elapsedTime)
        GLES30.glUniform2f(uResLoc,   surfaceWidth.toFloat(), surfaceHeight.toFloat())

        val touch = input.touchPoints.firstOrNull()
        GLES30.glUniform2f(uTouchLoc, touch?.x ?: 0.5f, touch?.y ?: 0.5f)

        val tilt = input.tilt
        GLES30.glUniform3f(uTiltLoc,  tilt?.x ?: 0f, tilt?.y ?: 0f, tilt?.z ?: 0f)

        onRender(dt, input)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
    }

    final override fun destroy() {
        onDestroy()
        if (program != 0) {
            GLES30.glDeleteProgram(program)
            program = 0
        }
    }

    // ── Overrideable hooks ─────────────────────────────────────────────────

    /** Called once after the GL program is linked. Cache extra uniform locations here. */
    protected open suspend fun onInit(width: Int, height: Int) {}

    /** Called every frame, after standard uniforms are set but before `glDrawArrays`. */
    protected open fun onRender(dt: Float, input: InputState) {}

    /** Called before [program] is deleted. Release any extra GL resources here. */
    protected open fun onDestroy() {}
}
