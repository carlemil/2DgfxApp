package com.twodgfxapp.renderer

import android.opengl.EGL14
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.SystemClock
import android.util.Log
import com.twodgfxapp.input.InputState
import kotlinx.coroutines.runBlocking
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private const val TAG = "GlSurfaceRenderer"

/**
 * [GLSurfaceView.Renderer] implementation that delegates rendering to an [EffectRenderer].
 *
 * Call sequence (on GL thread):
 *   onSurfaceCreated → onSurfaceChanged → onDrawFrame (×N) → [destroy]
 *
 * @param effectRenderer   The effect to render.
 * @param inputStateProvider Lambda that returns the current [InputState] snapshot.
 *                           Called every frame on the GL thread — must be thread-safe.
 * @param onError          Called when [EffectRenderer.init] throws [EffectInitException].
 */
class GlSurfaceRenderer(
    private val effectRenderer:   EffectRenderer,
    private val inputStateProvider: () -> InputState,
    private val onError:          (EffectInitException) -> Unit,
    private val onReady:          () -> Unit = {},
) : GLSurfaceView.Renderer {

    private var surfaceWidth  = 0
    private var surfaceHeight = 0
    private var initialized   = false
    private var lastFrameNs   = 0L

    // A single VAO bound for all draws (ES 3.0 requires a bound VAO)
    private val vao = IntArray(1)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glGenVertexArrays(1, vao, 0)
        GLES30.glBindVertexArray(vao[0])
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        surfaceWidth  = width
        surfaceHeight = height

        if (!initialized) {
            runBlocking {
                try {
                    effectRenderer.init(width, height)
                    initialized = true
                    onReady()
                } catch (e: EffectInitException) {
                    Log.e(TAG, "Effect init failed: ${e.message}", e)
                    onError(e)
                }
            }
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!initialized) return

        val nowNs = SystemClock.elapsedRealtimeNanos()
        val dt    = if (lastFrameNs == 0L) (1f / 60f) else (nowNs - lastFrameNs) / 1_000_000_000f
        lastFrameNs = nowNs

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        effectRenderer.render(dt.coerceIn(0.001f, 0.1f), inputStateProvider())
    }

    /** Must be called on the GL thread when the surface is destroyed to release GL resources. */
    fun destroy() {
        if (initialized) {
            effectRenderer.destroy()
            initialized = false
        }
        if (vao[0] != 0) {
            GLES30.glDeleteVertexArrays(1, vao, 0)
            vao[0] = 0
        }
    }
}
