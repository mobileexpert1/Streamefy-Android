package com.streamefy.component.ui.video

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

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
        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaSource: MediaSource = com.google.android.exoplayer2.source.hls.HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))

//        val mediaItem = MediaItem.fromUri(Uri.parse(uri))
//        player?.setMediaItem(mediaItem)
        player?.setMediaSource(mediaSource)

        player?.prepare()
    }

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
            Log.e("xmksnc","mxksnc $currentPosition new $newPosition")
            if (newPosition>(10*1000)) {
                exoPlayer.seekTo(newPosition)
            }else{
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
        isMuted=true
    }

    fun unmute() {
        setVolume(1.0f)
        isMuted=false
    }

    fun isMuted(): Boolean {
        return isMuted
    }
    fun isPlaying():Boolean?{
        return player?.isPlaying
    }

    fun toggleFullScreen() {
        // You can use your own logic to handle full-screen mode
        val isFullScreen = playerView.layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT
        playerView.layoutParams = if (isFullScreen) {
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200) // Example size for non-fullscreen
        } else {
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) // Fullscreen
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
