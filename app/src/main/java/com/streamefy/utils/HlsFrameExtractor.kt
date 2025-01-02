package com.streamefy.utils

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import java.nio.ByteBuffer

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class HlsFrameExtractor {

    private var extractor: MediaExtractor? = null
    private var codec: MediaCodec? = null
    private var surface: Surface? = null
    private var outputFrameBuffer: ByteBuffer? = null
    private var inputBuffer: ByteBuffer? = null
    private var format: MediaFormat? = null

    // Initialize MediaExtractor and MediaCodec
    @RequiresApi(Build.VERSION_CODES.M)
    fun initializeMediaCodec(videoUrl: String, playerView: PlayerView) {
        val mediaDataSource = StreamingMediaDataSource(videoUrl)
        var  extractor = MediaMetadataRetriever()
        try {
            // Set the data source to the HLS URL
            extractor.setDataSource(mediaDataSource)
//            // Get the number of tracks
//            val trackCount = extractor?.trackCount
//            for (i in 0 until trackCount!!) {
//                val format: MediaFormat = extractor?.getTrackFormat(i)!!
//                val mimeType = format.getString(MediaFormat.KEY_MIME)
//
//                // Check if the track is audio or video
//                if (mimeType?.startsWith("audio/")!!) {
//                    // Select the audio track
//                    extractor?.selectTrack(i)
//                    Log.d("MediaExtractor", "Selected audio track: $mimeType")
//                } else if (mimeType.startsWith("video/")) {
//                    // Select the video track
//                    extractor?.selectTrack(i)
//                    Log.d("MediaExtractor", "Selected video track: $mimeType")
//                }
//            }
//
//            // Read sample data from the selected track
//            val inputBuffer = ByteArray(1024 * 1024) // 1MB buffer
//            var sampleSize: Int
//            while (true) {
////                sampleSize = extractor?.readSampleData(inputBuffer, 0)!!
////                if (sampleSize < 0) {
////                    break // End of stream
////                }
////                val presentationTimeUs = extractor?.sampleTime
////                Log.d("MediaExtractor", "Read sample of size: $sampleSize at time: $presentationTimeUs")
////                extractor?.advance() // Move to the next sample
//            }
        } catch (e: Exception) {
            Log.e("MediaExtractor", "Error extracting HLS stream data: ${e.message}")
        } finally {
            extractor?.release() // Release the extractor
        }

//        try {
//            // Initialize MediaExtractor
//            extractor = MediaExtractor()
//
//            try {
//                extractor?.setDataSource(videoUrl)  // Set the URL of your HLS stream or video file
//            } catch (e: Exception) {
//                Log.e("HlsFrameExtractor", "Error setting data source: ${e.message}")
//                return
//            }
//
//            // Find the video track
//            var trackIndex = -1
//            for (i in 0 until extractor!!.trackCount) {
//                val format = extractor!!.getTrackFormat(i)
//                val mimeType = format.getString(MediaFormat.KEY_MIME)
//                if (mimeType != null && mimeType.startsWith("video/")) {
//                    trackIndex = i
//                    break
//                }
//            }
//
//            if (trackIndex == -1) {
//                Log.e("HlsFrameExtractor", "No video track found!")
//                return
//            }
//
//            // Configure and start MediaCodec
//            this.format = extractor!!.getTrackFormat(trackIndex)  // Save format here
//            codec = MediaCodec.createDecoderByType(this.format!!.getString(MediaFormat.KEY_MIME)!!)
//
//            // Get Surface from ExoPlayer's PlayerView
//            surface = getSurfaceFromExoPlayer(playerView)
//
//            codec?.configure(this.format, surface, null, 0)
//            codec?.start()
//            Log.e("HlsFrameExtractor", "started")
//            // Start extracting frames
//            extractFrames()
//        } catch (e: Exception) {
//            Log.e("HlsFrameExtractor", "Error initializing MediaCodec: ${e.message}")
//        }
    }




    // Extract frames from the video
    fun extractFrames() {
        var isExtracting = true
        var frameCount = 0

        while (isExtracting) {
            val inputBufferIndex = codec?.dequeueInputBuffer(10000) ?: -1
            Log.e("HlsFrameExtractor", "started inputBufferIndex $inputBufferIndex")

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
                    Log.d("HlsFrameExtractor", "Extracted frame: $frameCount  bitmap $bitmap")
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

    // Get Surface from ExoPlayer's PlayerView
    private fun getSurfaceFromExoPlayer(playerView: PlayerView): Surface? {
        var surface=playerView.videoSurfaceView as SurfaceView
        val surfaceHolder: SurfaceHolder = surface.holder
        return surfaceHolder.surface
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
    // Release resources
    fun release() {
        codec?.stop()
        codec?.release()
        extractor?.release()
    }
}
