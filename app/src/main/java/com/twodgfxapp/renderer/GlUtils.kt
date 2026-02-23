package com.twodgfxapp.renderer

import android.content.Context
import android.opengl.GLES30
import androidx.annotation.RawRes

/**
 * Utilities for compiling and linking GLSL shaders from Android raw resources.
 * All methods must be called on the GL thread.
 */
object GlUtils {

    /** Read the text content of an Android raw resource. */
    fun readRawResource(context: Context, @RawRes resId: Int): String =
        context.resources.openRawResource(resId).bufferedReader().use { it.readText() }

    /**
     * Compile a GLSL shader.
     *
     * @param type   [GLES30.GL_VERTEX_SHADER] or [GLES30.GL_FRAGMENT_SHADER].
     * @param source GLSL source string.
     * @return Handle to the compiled shader object.
     * @throws EffectInitException on compile failure.
     */
    fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        if (shader == 0) throw EffectInitException("glCreateShader returned 0")
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)

        val status = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] == GLES30.GL_FALSE) {
            val log = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            throw EffectInitException("Shader compile error:\n$log")
        }
        return shader
    }

    /**
     * Link a GLSL program from already-compiled shader handles.
     * Shader handles are detached but NOT deleted — the caller owns their lifetime.
     *
     * @return Handle to the linked program object.
     * @throws EffectInitException on link failure.
     */
    fun linkProgram(vertShader: Int, fragShader: Int): Int {
        val program = GLES30.glCreateProgram()
        if (program == 0) throw EffectInitException("glCreateProgram returned 0")
        GLES30.glAttachShader(program, vertShader)
        GLES30.glAttachShader(program, fragShader)
        GLES30.glLinkProgram(program)

        val status = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0)
        if (status[0] == GLES30.GL_FALSE) {
            val log = GLES30.glGetProgramInfoLog(program)
            GLES30.glDeleteProgram(program)
            throw EffectInitException("Program link error:\n$log")
        }
        return program
    }

    /**
     * Convenience: read, compile and link a vertex+fragment shader pair from raw resources.
     * Intermediate shader objects are deleted after linking.
     *
     * @return Handle to the linked program object, ready to use with [GLES30.glUseProgram].
     * @throws EffectInitException on any failure.
     */
    fun compileProgram(
        context:    Context,
        @RawRes vertResId: Int,
        @RawRes fragResId: Int,
    ): Int {
        val vertSrc = readRawResource(context, vertResId)
        val fragSrc = readRawResource(context, fragResId)

        val vert = compileShader(GLES30.GL_VERTEX_SHADER,   vertSrc)
        val frag = try {
            compileShader(GLES30.GL_FRAGMENT_SHADER, fragSrc)
        } catch (e: EffectInitException) {
            GLES30.glDeleteShader(vert)
            throw e
        }
        return try {
            linkProgram(vert, frag)
        } finally {
            GLES30.glDeleteShader(vert)
            GLES30.glDeleteShader(frag)
        }
    }
}
