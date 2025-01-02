package com.streamefy.utils

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class HlsFrameExtractorCodec {

    private var extractor: MediaExtractor? = null
    private var codec: MediaCodec? = null
    private var surface: Surface? = null
    private var outputFrameBuffer: ByteBuffer? = null
    private var inputBuffer: ByteBuffer? = null
    private var format: MediaFormat? = null  // Store format here

    // Initialize MediaExtractor and MediaCodec
    fun initializeMediaCodec(videoUrl: String, surfaceView: SurfaceView) {
        try {
            // Initialize MediaExtractor
            extractor = MediaExtractor()
            extractor?.setDataSource(videoUrl)  // Set the URL of your HLS stream or video file

            // Find the video track
            var trackIndex = -1
            for (i in 0 until extractor!!.trackCount) {
                val format = extractor!!.getTrackFormat(i)
                val mimeType = format.getString(MediaFormat.KEY_MIME)
                if (mimeType != null && mimeType.startsWith("video/")) {
                    trackIndex = i
                    break
                }
            }

            if (trackIndex == -1) {
                Log.e("HlsFrameExtractor", "No video track found!")
                return
            }

            // Configure and start MediaCodec
            this.format = extractor!!.getTrackFormat(trackIndex)  // Save format here
            codec = MediaCodec.createDecoderByType(this.format!!.getString(MediaFormat.KEY_MIME)!!)

            // Get Surface from SurfaceView
            surface = surfaceView.holder.surface  // Correct usage

            codec?.configure(this.format, surface, null, 0)
            codec?.start()

            // Start extracting frames
            extractFrames()
        } catch (e: Exception) {
            Log.e("HlsFrameExtractor", "Error initializing MediaCodec: ${e.message}")
        }
    }

    // Extract frames from the video
    fun extractFrames() {
        var isExtracting = true
        var frameCount = 0

        while (isExtracting) {
            val inputBufferIndex = codec?.dequeueInputBuffer(10000) ?: -1
            if (inputBufferIndex >= 0) {
                // Feed data to the codec
                inputBuffer = codec?.getInputBuffer(inputBufferIndex)
                val sampleSize = extractor?.readSampleData(inputBuffer!!, 0) ?: -1
                if (sampleSize < 0) {
                    Log.d("HlsFrameExtractor", "End of stream reached.")
                    isExtracting = false
                } else {
                    codec?.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor!!.sampleTime, 0)
                    extractor?.advance()
                }
            }

            val bufferInfo = MediaCodec.BufferInfo()
            val outputBufferIndex = codec?.dequeueOutputBuffer(bufferInfo, 10000) ?: -1
            if (outputBufferIndex >= 0) {
                val outputFrame = codec?.getOutputBuffer(outputBufferIndex)

                // If you need the frame as a Bitmap
                if (outputFrame != null && bufferInfo.size > 0 && format != null) {
                    val bitmap = convertFrameToBitmap(outputFrame, bufferInfo.size, format!!)
                    // Do something with the Bitmap (e.g., display or save)
                    Log.d("HlsFrameExtractor", "Extracted frame: $frameCount")
                    frameCount++
                }

                codec?.releaseOutputBuffer(outputBufferIndex, true)
            }
        }

        codec?.stop()
        codec?.release()
        extractor?.release()
    }

    // Convert raw video frame (ByteBuffer) to Bitmap
    private fun convertFrameToBitmap(frame: ByteBuffer, size: Int, format: MediaFormat): Bitmap {
        // Extract the width and height from the format
        val width = format.getInteger(MediaFormat.KEY_WIDTH)
        val height = format.getInteger(MediaFormat.KEY_HEIGHT)

        // Create a byte array to hold YUV data
        val yuvData = ByteArray(size)
        frame.get(yuvData)

        // Convert YUV to RGB (assuming YUV420)
        val rgba = yuv420ToRgb(yuvData, width, height)

        // Create Bitmap from RGBA
        return Bitmap.createBitmap(rgba, 0, width, width, height, Bitmap.Config.ARGB_8888)
    }

    // Convert YUV420 to RGB
    private fun yuv420ToRgb(yuv: ByteArray, width: Int, height: Int): IntArray {
        val rgba = IntArray(width * height)

        // YUV420 conversion to RGB logic here
        // This is a simplified conversion, you can use libraries or more efficient algorithms.
        var yIndex = 0
        var uvIndex = width * height

        for (i in 0 until height) {
            for (j in 0 until width) {
                val y = yuv[yIndex++].toInt() and 0xFF
                val u = yuv[uvIndex++].toInt() and 0xFF
                val v = yuv[uvIndex++].toInt() and 0xFF

                // Basic YUV to RGB conversion
                val r = (y + 1.402 * (v - 128)).toInt().coerceIn(0, 255)
                val g = (y - 0.344136 * (u - 128) - 0.714136 * (v - 128)).toInt().coerceIn(0, 255)
                val b = (y + 1.772 * (u - 128)).toInt().coerceIn(0, 255)

                rgba[i * width + j] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        return rgba
    }
}
