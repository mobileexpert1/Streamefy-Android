package com.streamefy.utils

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.view.Surface
import android.view.SurfaceHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.IntBuffer
import java.io.File
import java.io.FileOutputStream

class FrameCaptureHandler(private val context: Context) {
    private var eglDisplay: EGLDisplay? = null
    private var eglSurface: EGLSurface? = null
    private var eglContext: EGLContext? = null
    var frameCaptureRunnable: Runnable? = null

    fun initializeEGL(surface: Surface) {
        // Initialize EGL only once when the surface is created
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw RuntimeException("Failed to initialize EGL display")
        }

        val config = chooseEGLConfig(eglDisplay!!)
        eglContext = createEGLContext(eglDisplay!!, config)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, config, surface, intArrayOf(EGL14.EGL_NONE), 0)

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw RuntimeException("Failed to make EGL context current")
        }
    }

    fun startPeriodicCapture(surface: Surface, width: Int, height: Int) {
        // Use a timer or handler to periodically trigger frame capture
        frameCaptureRunnable = Runnable {
            captureFrame(surface, width, height)
        }
        
        // Start periodic capture every 1000ms (1 second)
        val intervalMillis = 1000L
        val handler = android.os.Handler()
        handler.postDelayed(frameCaptureRunnable!!, intervalMillis)
    }

    private fun captureFrame(surface: Surface, width: Int, height: Int) {
        // Use OpenGL to capture the frame from the Surface
        val frameBuffer = IntArray(1)
        GLES20.glGenFramebuffers(1, frameBuffer, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])

        val pixels = IntArray(width * height)
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, IntBuffer.wrap(pixels))

        // Convert to Bitmap
        val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

        // Save or process the Bitmap
        saveBitmap(bitmap)
    }

    private fun saveBitmap(bitmap: Bitmap) {
        // Save the captured frame as a PNG file
        val file = File(context.cacheDir, "captured_frame.png")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()
    }

    // EGL configuration and context creation methods
    private fun chooseEGLConfig(eglDisplay: EGLDisplay): EGLConfig {
        val configAttribs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE
        )
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, null, 0, 1, numConfigs, 0)

        val configs = arrayOfNulls<EGLConfig>(numConfigs[0])
        EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, numConfigs[0], numConfigs, 0)

        return configs[0] ?: throw RuntimeException("Unable to choose EGLConfig")
    }

    private fun createEGLContext(eglDisplay: EGLDisplay, config: EGLConfig): EGLContext {
        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, // OpenGL ES 2.0
            EGL14.EGL_NONE
        )

        return EGL14.eglCreateContext(eglDisplay, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
    }
}