package com.twodgfxapp.renderer.effects

import android.content.Context
import android.opengl.GLES30
import com.twodgfxapp.R
import com.twodgfxapp.input.InputState
import com.twodgfxapp.renderer.BaseShaderEffect

/** Metaballs — passes up to 5 simultaneous touch points to the shader. */
class MetaballsEffect(context: Context) : BaseShaderEffect(context, R.raw.metaballs) {

    private var uTouchCountLoc = -1
    private var uTouchesLoc    = -1
    private val touchArray     = FloatArray(10)

    override suspend fun onInit(width: Int, height: Int) {
        uTouchCountLoc = GLES30.glGetUniformLocation(program, "u_touch_count")
        uTouchesLoc    = GLES30.glGetUniformLocation(program, "u_touches")
    }

    override fun onRender(dt: Float, input: InputState) {
        val n = minOf(input.touchPoints.size, 5)
        for (i in 0 until n) {
            touchArray[i * 2]     = input.touchPoints[i].x
            touchArray[i * 2 + 1] = input.touchPoints[i].y
        }
        GLES30.glUniform1i(uTouchCountLoc, n)
        if (n > 0) GLES30.glUniform2fv(uTouchesLoc, n, touchArray, 0)
    }
}
