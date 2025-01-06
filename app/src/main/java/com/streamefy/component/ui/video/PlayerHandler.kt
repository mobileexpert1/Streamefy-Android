package com.streamefy.component.ui.video


import TokenAuthDataSource
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.StrictMode
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSource.Factory
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.streamefy.component.ui.video.model.QualityModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class PlayerHandler(
    private val context: Context,
    private val playerView: PlayerView
) {

    var player: ExoPlayer? = null
    private var isMuted: Boolean = false

    init {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        initializePlayer()
    }

    fun initializePlayer() {
        try {

//            val renderersFactory = DefaultRenderersFactory(context)
//                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            val renderersFactory = DefaultRenderersFactory(context)
                .setEnableDecoderFallback(true)



            val trackSelector = DefaultTrackSelector(context)
            trackSelector.parameters = DefaultTrackSelector.ParametersBuilder()
                .setForceLowestBitrate(false)
                //.setMaxVideoSize(854, 480)
                .build()

            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                )
                .build()

            player = ExoPlayer.Builder(context)
                .setRenderersFactory(
                    DefaultRenderersFactory(context)
                        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF) // Disable extension renderers
                        .setEnableDecoderFallback(true)

                )
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .build()
            playerView.player = player


//
//            val trackSelector = DefaultTrackSelector(context)
//            val renderersFactory = DefaultRenderersFactory(context).setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
//             player = ExoPlayer.Builder(context)
//                .setRenderersFactory(renderersFactory)
//                .setTrackSelector(trackSelector)
//                .build()
//            playerView.player = player

//            CoroutineScope(Dispatchers.IO).launch {
//                val trackSelector = DefaultTrackSelector(context)
//                player = ExoPlayer.Builder(context)
//                    .setTrackSelector(trackSelector)
//                    .build()
//                withContext(Dispatchers.Main) {
//                    playerView.player = player
//                }
//            }
        } catch (e: Exception) {
            Log.e("skcmskc", "initializeing error $e")
        }


    }

    fun getPLayer() = playerView.player
    var  videoUrl=""
    fun setMediaUri(uri: String, lastDuration: Long) {
        videoUrl=uri
//        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
//            setDefaultRequestProperties(mapOf("AccessKey" to "24c40ba2-d6bb-440f-991324192bf2-e4ad-4440"))
//        }
//
//        val dataSourceFactory = DefaultHttpDataSource.Factory()
//        val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory)
//            .createMediaSource(MediaItem.fromUri(uri))
////        player?.setMediaSource(mediaSource)
////        player?.prepare()
//
//        player = ExoPlayer.Builder(context).build().apply {
//            setMediaSource(mediaSource)
//            prepare()
//          //  playWhenReady = true
//        }
//        playerView.player = player


//new
//            //Creating a media item of HLS Type
//            val mediaItem = MediaItem.Builder()
//                .setUri(uri)
//                .setMimeType(MimeTypes.APPLICATION_M3U8) //m3u8 is the extension used with HLS sources
//                .build()
//
//            player?.setMediaItem(mediaItem)
//            player?.prepare()
//            player?.play()


//        val baseUrl = "https://example.com/path/to/playlist.m3u8"
//        val token = "your-authentication-token" // Replace with your token
//
//
//        // Append token to URL if needed
//
//        // Append token to URL if needed
//        val uri = Uri.parse(uri).buildUpon()
//            .appendQueryParameter("token", token)
//            .build()
//
//        // Create media source
//
//        // Create media source
        try {
            if (player != null && uri.isNotEmpty()) {

                player?.let {
                    if (player?.isPlaying!!) {
                        release()
                        initializePlayer()
                    }
                    Log.e("videourlssss", "$lastDuration player: $uri")
                    CoroutineScope(Dispatchers.IO).launch {

                        val dataSourceFactory = DefaultHttpDataSource.Factory()
                        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(uri))

//                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//                            .createMediaSource(MediaItem.fromUri(uri))

                        withContext(Dispatchers.Main) {
                            player?.apply {

                                stop()
                                clearMediaItems()

                                setMediaSource(mediaSource)
                                prepare()
                                seekTo(lastDuration)
                                play()
                            }
                        }
                    }



                }
            }
        }
//        catch (e: ExoPlaybackException) {
//            when (e.type) {
//                ExoPlaybackException.TYPE_SOURCE -> {
//                    Log.e("ExoPlayerError", "Source error: ${e.sourceException?.message}")
//                }
//                ExoPlaybackException.TYPE_RENDERER -> {
//                    Log.e("ExoPlayerError", "Renderer error: ${e.rendererException?.message}")
//                }
//                ExoPlaybackException.TYPE_UNEXPECTED -> {
//                    Log.e("ExoPlayerError", "Unexpected error: ${e.message}")
//                }
//            }
//        }
        catch (e: Exception) {
            Log.e("skcmskc", "video playing error $e")
        }
        Log.e("sjkcnsakjbc", "akjcnkja play")

        //  playTokenise()
    }

    var retriever:MediaMetadataRetriever?=null
      fun initVideoFrame(videoUrl: String){
          GlobalScope.launch(Dispatchers.IO){
              try {
                  if (player != null && retriever == null) {
                      retriever = MediaMetadataRetriever()
                      retriever?.setDataSource(videoUrl, HashMap())

                  }
              } catch (e: Exception) {
                  e.printStackTrace()
              }

        }}

     fun getFrame(callBack:(Bitmap?)->Unit){
                 if ( player != null) {

                     val currentPositionInMicroseconds = player?.currentPosition?.times(1000) ?: 0L
                     GlobalScope.launch(Dispatchers.IO) {
                         val retriever = MediaMetadataRetriever()
                         retriever.setDataSource(videoUrl, HashMap())
                         val frame = retriever.getFrameAtTime(currentPositionInMicroseconds)
                         callBack.invoke(frame)
                         retriever.release()

//                     if (player?.bufferedPosition!! >player?.currentPosition!!) {
//                         val currentPositionInMicroseconds = player?.currentPosition!! * 1000
//                         val bitmap = retriever?.getFrameAtTime(currentPositionInMicroseconds)
//                         callBack.invoke(bitmap)
//                     }else{
//                         Log.e("sjbcjsbc","buffers is lower ${player?.bufferedPosition!!}")
//                     }
                     }
                 }
    }

    fun seekWithInitialise(uri: String, currentDuration: Long) {

        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))

        // Prepare player with media source
        player!!.setMediaSource(mediaSource)
        player!!.prepare()
//        player!!.seekTo(currentDuration)
        player!!.play()

        Log.e("sjkcnsakjbc", "akjcnkja play $uri kkkk")

    }

    fun playTokenise() {
        val url =
            "https://vz-4aa86377-b82.b-cdn.net/bcdn_token=VTzc7imuotCSMWo-2B8xPdfacWpngzRH0k5u6l5GeYk&expires=1726208026&token_path=%2F06a93993-df8b-44c5-bf95-24d107ff5a95%2F/06a93993-df8b-44c5-bf95-24d107ff5a95/playlist.m3u8"

        val token = "VTzc7imuotCSMWo-2B8xPdfacWpngzRH0k5u6l5GeYk"
        val expires = 1726208026
        val tokenPath = "/06a93993-df8b-44c5-bf95-24d107ff5a95/"
        val playlistUrl = "https://vz-4aa86377-b82.b-cdn.net/$tokenPath/playlist.m3u8"

        val upstreamDataSource = DefaultDataSourceFactory(context).createDataSource()

        //  val tokenAuthDataSource = TokenAuthDataSource(token, upstreamDataSource)


        val dataSourceFactory = object : Factory {
            override fun createDataSource(): DataSource {
                return TokenAuthDataSource(token, upstreamDataSource)
            }
        }

        val mediaItem = MediaItem.fromUri(Uri.parse(playlistUrl))
        val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        player?.setMediaSource(hlsMediaSource)
        player?.prepare()
        player?.play()


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


    fun setQuality(resolution: QualityModel) {
        val trackSelector = player?.trackSelector as DefaultTrackSelector
        val trackSelectionParameters = trackSelector.buildUponParameters()
            .setMaxVideoSize(resolution.width, resolution.height)
            .build()
        Log.e("dcbdhbcd","cjdc testing $resolution")
        trackSelector.setParameters(trackSelectionParameters)
    }


    fun setAutoResolutionBasedOnBandwidth() {
        val trackSelector = player?.trackSelector as DefaultTrackSelector
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        val estimatedBandwidth = bandwidthMeter.getBitrateEstimate()
        val resolution = when {
          //  estimatedBandwidth >= 5000000 -> {
            estimatedBandwidth >= 3000000 -> {
                // High bandwidth, select 1080p (landscape)
                Pair(1920, 1080)
            }
            estimatedBandwidth >= 2000000 -> {
                // Medium bandwidth, select 720p
                Pair(1280, 720)
            }
            estimatedBandwidth >= 1000000 -> {
                // Lower bandwidth, select 480p
                Pair(854, 480)
            }
            else -> {
                Pair(1080, 1920)
            }
        }
        Log.e("resulation"," resulation $resolution bandwidth $estimatedBandwidth  ")
        val trackSelectionParameters = trackSelector.buildUponParameters()
            .setMaxVideoSize(resolution.first, resolution.second)  // Set the dynamic resolution
            .build()
        trackSelector.setParameters(trackSelectionParameters)
    }

    fun play() {
        player?.playWhenReady = true
    }
    fun refresh() {
        player?.let {
            player?.seekTo(0)
            player?.playWhenReady = true
        }

    }
    fun pause() {
        if (player!=null) {
            player?.playWhenReady = false
        }
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun seekBackward(seconds: Long,callBack:(Long)->Unit) {
        player?.let { exoPlayer ->
            val currentPosition = exoPlayer.currentPosition
            val newPosition = (currentPosition - seconds * 1000)
            Log.e("xmksnc", "mxksnc $currentPosition new $newPosition")
            if (newPosition > (10 * 1000)) {
                exoPlayer.seekTo(newPosition)
                exoPlayer.pause()
                callBack.invoke(newPosition)
            } else {
                exoPlayer.seekTo(0)
                exoPlayer.pause()
                callBack.invoke(0)
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
        setVolume(0.001f)
        //player?.volume = volume
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

        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        // Format hours, minutes, and seconds to always show two digits
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)


//        val minutes = (durationMillis / 1000) / 60
//        val seconds = (durationMillis / 1000) % 60
//
//        // Format minutes and seconds to always show two digits
//        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getDuration(): Long {
        return player?.duration ?: 0L
    }

    fun getTotalLength(): String {

        val durationMillis = player?.duration ?: 0L

        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        // Format hours, minutes, and seconds to always show two digits
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)

//        val durationMillis = player?.duration ?: 0L
//
//        val minutes = (durationMillis / 1000) / 60
//        val seconds = (durationMillis / 1000) % 60
//
//        // Format minutes and seconds to always show two digits
//        return String.format("%02d:%02d", minutes, seconds)
    }
     fun getRemainsDuration(): String {

        val durationMillis = player?.duration ?: 0L
        val currentDur = player?.currentPosition ?: 0L
        var remains=durationMillis-currentDur
        val totalSeconds = remains / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)


    }

    var handler = Handler()

    fun stopHandler() {
        handler.removeMessages(0)
    }

    fun release() {
     //   player?.stop()
        player?.release()
        player = null
        handler.removeMessages(0)

    }
}
