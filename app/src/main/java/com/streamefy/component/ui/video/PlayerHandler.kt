package com.streamefy.component.ui.video

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import java.net.URLEncoder
import java.util.Base64
import java.util.Collections
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class PlayerHandler(
    private val context: Context,
    private val playerView: PlayerView
) {

    var player: ExoPlayer? = null
    private var isMuted: Boolean = false

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(context).build()

        playerView.player = player
    }

    fun setMediaUri(uri: String) {

        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
            setDefaultRequestProperties(mapOf("AccessKey" to "24c40ba2-d6bb-440f-991324192bf2-e4ad-4440"))
        }

        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
//        player?.setMediaSource(mediaSource)
//        player?.prepare()

        player = ExoPlayer.Builder(context).build().apply {
            setMediaSource(mediaSource)
            prepare()
          //  playWhenReady = true
        }
        playerView.player = player

        //new
//        val baseUri = Uri.parse(uri)
//        val playlistUrl = baseUri.buildUpon().path(baseUri.lastPathSegment).build().toString()
//
//        // Create a default HttpDataSource.Factory with custom user agent
//        // Create a default HttpDataSource.Factory with custom user agent
//        val dataSourceFactory: HttpDataSource.Factory = DefaultHttpDataSource.Factory()
//            .setDefaultRequestProperties(
//                Collections.singletonMap(
//                    "User-Agent",
//                    "YourUserAgentString"
//                )
//            )
//
//        // Create the HLS media source
//
//        // Create the HLS media source
//        val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory)
//            .createMediaSource(MediaItem.fromUri(playlistUrl))
//
//        player?.setMediaSource(mediaSource);
//        player?.prepare();
//        player?.play();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSignedUrl(baseUrl: String, secretKey: String, expireTime: Long): String {
        val encodedUrl = URLEncoder.encode(baseUrl, "UTF-8")
        val expirationTime = expireTime.toString()
        val stringToSign = "$encodedUrl|$expirationTime"

        val mac = Mac.getInstance("HmacSHA1")
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA1")
        mac.init(secretKeySpec)
        val signature = Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.toByteArray()))

        return "$baseUrl?expires=$expirationTime&signature=$signature"
    }

    val baseUrl = "https://your-bunny-cdn-url/your-file.mp4"
    val secretKey = "your-secret-key"
    val expireTime = System.currentTimeMillis() / 1000 + 3600 // URL valid for 1 hour


    fun setQuality(resolution: String) {
        val trackSelector = player?.trackSelector as DefaultTrackSelector
        val dimensions = when (resolution) {
            "360p" -> Pair(640, 360)
            "480p" -> Pair(854, 480)
            "720p" -> Pair(1280, 720)
            "1080p" -> Pair(1920, 1080)
            "1440p" -> Pair(2560, 1440)
            "4K" -> Pair(3840, 2160)
            else -> return
        }

        val (width, height) = dimensions
        val trackSelectionParameters = trackSelector.buildUponParameters()
            .setMaxVideoSize(width, height)
            .build()

        trackSelector.setParameters(trackSelectionParameters)
    }

    fun play() {
        player?.playWhenReady = true
    }

    fun pause() {
        player?.playWhenReady = false
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun seekBackward(seconds: Long) {
        player?.let { exoPlayer ->
            val currentPosition = exoPlayer.currentPosition
            val newPosition = (currentPosition - seconds * 1000)//.coerceAtLeast(0)
            Log.e("xmksnc", "mxksnc $currentPosition new $newPosition")
            if (newPosition > (10 * 1000)) {
                exoPlayer.seekTo(newPosition)
            } else {
                exoPlayer.seekTo(0)
            }
        }
    }

    fun setPlaybackProgress(progress: Int) {
        val duration = player?.duration ?: 0L
        val position = (progress / 100.0 * duration).toLong()
        player?.seekTo(position)
    }

    fun setVolume(volume: Float) {
        player?.volume = volume
    }

    fun getVolume(): Float {
        return player?.volume ?: 1.0f
    }

    fun mute() {
        setVolume(0f)
        isMuted = true
    }

    fun unmute() {
        setVolume(1.0f)
        isMuted = false
    }

    fun isMuted(): Boolean {
        return isMuted
    }

    fun isPlaying(): Boolean? {
        return player?.isPlaying
    }

    fun toggleFullScreen() {
        // You can use your own logic to handle full-screen mode
        val isFullScreen = playerView.layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT
        playerView.layoutParams = if (isFullScreen) {
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200
            ) // Example size for non-fullscreen
        } else {
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ) // Fullscreen
        }
        playerView.requestLayout()
    }

    fun getCurrentPosition(): Long {
        return player?.currentPosition ?: 0L
    }


    fun getcurrent(): String {
        val durationMillis = player?.currentPosition ?: 0L

        val minutes = (durationMillis / 1000) / 60
        val seconds = (durationMillis / 1000) % 60

        // Format minutes and seconds to always show two digits
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getDuration(): Long {
        return player?.duration ?: 0L
    }

    fun getTotalLength(): String {
        val durationMillis = player?.duration ?: 0L

        val minutes = (durationMillis / 1000) / 60
        val seconds = (durationMillis / 1000) % 60

        // Format minutes and seconds to always show two digits
        return String.format("%02d:%02d", minutes, seconds)
    }

    var handler = Handler()

    fun stopHandler() {
        handler.removeMessages(0)
    }

    fun release() {
        player?.release()
        player = null
        handler.removeMessages(0)
    }
}
