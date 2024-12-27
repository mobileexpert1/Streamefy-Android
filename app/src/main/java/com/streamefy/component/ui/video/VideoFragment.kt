package com.streamefy.component.ui.video

import VolumeManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.FFprobeSession
import com.arthenica.ffmpegkit.FFprobeSessionCompleteCallback
import com.arthenica.ffmpegkit.ReturnCode

import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.HomeFragment
import com.streamefy.component.ui.home.HomeFragment.Companion.homeFragment
import com.streamefy.component.ui.video.model.BunneyIds
import com.streamefy.component.ui.video.model.PlayBackRequest
import com.streamefy.component.ui.video.model.QualityModel
import com.streamefy.component.ui.video.viewmodel.VideoVM
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentVideoBinding
import com.streamefy.media.MediaHandler
import com.streamefy.network.MyResource
import com.streamefy.utils.FrameCaptureHandler
import com.streamefy.utils.gone
import com.streamefy.utils.invisible
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showMessage
import com.streamefy.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer


class VideoFragment : BaseFragment<FragmentVideoBinding>() {
    override fun bindView(): Int = R.layout.fragment_video
    lateinit var playerHandler: PlayerHandler
    var getLengthOnce = true
    var isEnded = false
    var visibilityCount = 0
    var volumeCount = 20
    var isOpenSettingFirst = false
    var videoQualityIndex = 0
    var playbackduration: Long = 0
    var thumbnailS3bucketId = ""
    private lateinit var volumeManager: VolumeManager
    lateinit var qualityAdapter: QualityAdapter
    var qualityList = ArrayList<QualityModel>()
    var bunneyIdList = ArrayList<BunneyIds>()

    //       var videoUrl="https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
    var videoUrl = "https://ia601209.us.archive.org/17/items/ElephantsDream/ed_1024_512kb.mp4"
    var ifFirst = true

    var nextVideoId = ""
    var mediaId = 0
    var eventId = 0
    var oldMediaId = 0
    var oldEventId = 0
    var oldBunnyId = ""
    var oldVideoDuration: Long = 0
    var videoThumb = ""
    var isNextVideoStarted = false
    var phone = ""
    var videoCount = 0
    var isVolume = false

    companion object {
        lateinit var videoFragment: VideoFragment
    }

    private val viewModel: VideoVM by viewModel()
    private lateinit var mediaHandler: MediaHandler
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaHandler = MediaHandler(context)
    }
    private lateinit var frameCaptureHandler: FrameCaptureHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoFragment = this
        isVolume = true
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        arguments?.run {
            thumbnailS3bucketId = getString(PrefConstent.VIDEO_THUMB).toString()
            var newDuration = getString(PrefConstent.PLAY_BACK_DURATION).toString()

            nextVideoId = getString(PrefConstent.VIDEO_ID).toString()
            if (getString(PrefConstent.MEDIA_ID).toString().isNotEmpty()) {
                mediaId = getString(PrefConstent.MEDIA_ID).toString().toInt()
            }
            oldBunnyId = nextVideoId
            bunneyIdList.clear()

            playbackduration = newDuration.toDouble().toInt().toString().toLong()

            //  bunneyIdList.add(BunneyIds(mediaId=mediaId, eventId = eventId, bunneyId = nextVideoId))
        }
//        frameCaptureHandler = FrameCaptureHandler(requireActivity())
//        mediaHandler = MediaHandler(requireActivity())
        handleKey(binding.playerView)
        volumeManager = VolumeManager(requireActivity())
        volumeManager.setVolumePercentage(5)

        binding.apply {
//            if (playbackduration<=0) {
//                ivVideoThumb.loadUrl(thumbnailS3bucketId)
//            }
            updatePlayer()
            thumbShow()
            newVideo()
            clickme()
            listener()
            keyMove()
        }
        volume()
        selectorFocus()

        Log.e(
            "ckdanmcn",
            "duration $playbackduration video id ${nextVideoId} volumeCount $volumeCount mkadnc ${videoUrl}"
        )
        binding.sbVolumeSeek.setProgress(volumeCount)


    }

    //    fun loadVideoThumbnail(videoUrl: String) = with( binding){
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                if (playerHandler.player!=null && playerHandler.isPlaying()!!) {
//                    val retriever = MediaMetadataRetriever()
//                    retriever.setDataSource(videoUrl, HashMap())
//                    val bitmap = retriever.getFrameAtTime(0)
//                    ivSeekThumb.post {
//                        ivSeekThumb.setImageBitmap(bitmap)
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
    //
    fun keyMove() = with(binding) {
        sbVideoSeek.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                toShowBackButton()
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        ivSkipForward.requestFocus()
                        return@OnKeyListener true // Consume the event
                    }

                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        ivSetting.requestFocus()
                        return@OnKeyListener true // Consume the event
                    }

                    KeyEvent.KEYCODE_DPAD_UP -> {
                        forward()
                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        playerHandler.seekBackward(30)
                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                        mediaKey(StreamEnum.KEYCODE_MEDIA_FAST_FORWARD)
                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
                        mediaKey(StreamEnum.KEYCODE_MEDIA_REWIND)
                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        mediaKey(StreamEnum.KEYCODE_MEDIA_PLAY_PAUSE)
                        return@OnKeyListener true
                    }
                }
            }
            false // Don't consume other events
        })
        sbVolumeSeek.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            binding.ivBack.animate().alpha(1f).setDuration(50).setStartDelay(50)
            binding.llTools.animate().alpha(1f).setDuration(50).setStartDelay(50)
            visibilityCount = 0
            clSettingsMenu.gone()

            if (event.action == KeyEvent.ACTION_DOWN) {
                toShowBackButton()
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        ivVolume.requestFocus()
                        return@OnKeyListener true // Consume the event
                    }

                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        ivRefresh.requestFocus()
                        return@OnKeyListener true // Consume the event
                    }

                    KeyEvent.KEYCODE_DPAD_UP -> {
                        isVolume = false
                        volumeUp()
                        // volumeManager.setVolumePercentage(volumeCount)
                        binding.sbVolumeSeek.progress = volumeCount

                        if (volumeCount <= 0) {
                            //  ivVolume.requestFocus()
                            playerHandler.mute()
                            ivVolume.setImageResource(R.drawable.ic_mute)
                            //  playerHandler.isMuted=false
                        } else {
//                                unmute
                            // playerHandler.isMuted=true
                            ivVolume.setImageResource(R.drawable.ic_video_volume)
                        }

                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        isVolume = false
                        volumeDown()
                        // volumeManager.setVolumePercentage(volumeCount)
                        binding.sbVolumeSeek.progress = volumeCount

                        if (volumeCount <= 0) {
                            //  ivVolume.requestFocus()
                            playerHandler.mute()
                            ivVolume.setImageResource(R.drawable.ic_mute)
                            // playerHandler.isMuted=false
                        } else {
                            ivVolume.setImageResource(R.drawable.ic_video_volume)
                            // playerHandler.isMuted=true
                        }

                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                        mediaKey(StreamEnum.KEYCODE_MEDIA_FAST_FORWARD)
                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
                        mediaKey(StreamEnum.KEYCODE_MEDIA_REWIND)
                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        mediaKey(StreamEnum.KEYCODE_MEDIA_PLAY_PAUSE)
                        return@OnKeyListener true
                    }
                }
            }
            false // Don't consume other events
        })
        ivSkipBack.remoteKey {
            visibilityCount = 0
            mediaKey(it)
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    ivRefresh.requestFocus()

                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    ivBack.requestFocus()

                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()

                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    ivPlay.requestFocus()

                }


                else -> {}
            }
        }

        ivPlay.remoteKey {
            mediaKey(it)
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    ivSkipBack.requestFocus()
                    visibilityCount = 0
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    ivBack.requestFocus()
                    visibilityCount = 0
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                    visibilityCount = 0
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    ivSkipForward.requestFocus()
                    visibilityCount = 0
                }

                else -> {}
            }
        }
        ivSkipForward.remoteKey {
            visibilityCount = 0
            mediaKey(it)
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    ivPlay.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    sbVideoSeek.requestFocus()
                }

                else -> {}
            }
        }
        ivSetting.remoteKey {
            visibilityCount = 0
            mediaKey(it)
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    sbVideoSeek.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    ivVolume.requestFocus()
                }

                else -> {}
            }
        }
        ivVolume.remoteKey {
            visibilityCount = 0
            mediaKey(it)
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    ivSetting.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    sbVolumeSeek.requestFocus()
                }

                else -> {}
            }
        }


        ivBack.remoteKey {
            visibilityCount = 0
            mediaKey(it)
            when (it) {
                StreamEnum.DOWN_DPAD_KEY -> {
                    if (timerLayout.isVisible) {
                        timerLayout.requestFocus()
                    } else {
                        ivRefresh.requestFocus()
                    }
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivRefresh.requestFocus()
                }

                StreamEnum.LEFT_DPAD_KEY -> {
                    ivSkipBack.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    ivRefresh.requestFocus()
                }

                else -> {}
            }
        }
        ivRefresh.remoteKey {
            visibilityCount = 0
            mediaKey(it)
            when (it) {
                StreamEnum.DOWN_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    if (timerLayout.isVisible) {
                        timerLayout.requestFocus()
                    } else {
                        ivBack.requestFocus()
                    }
                }

                StreamEnum.LEFT_DPAD_KEY -> {
                    sbVolumeSeek.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    ivSkipBack.requestFocus()
                }

                else -> {}
            }
        }
        timerLayout.remoteKey {
            visibilityCount = 0
            mediaKey(it)
            when (it) {
                StreamEnum.DOWN_DPAD_KEY -> {
                    ivRefresh.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.LEFT_DPAD_KEY -> {
                    ivRefresh.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    ivSkipBack.requestFocus()
                }

                else -> {}
            }
        }
    }

    fun mediaKey(streamEnum: StreamEnum) = with(binding) {
//        toShowBackButton()
//        viewFocus()
        when (streamEnum) {
            StreamEnum.KEYCODE_MEDIA_FAST_FORWARD -> {
                forward10()
                ivMedia.setImageResource(R.drawable.ic_remote_forward)
                ivMedia.visible()
                ivMedia.alpha = 1f
                lifecycleScope.launch {
                    delay(2000)
                    withContext(Dispatchers.Main) {
                        // ivMedia.gone()
                        ivMedia.animate().alpha(0f).setStartDelay(10).setDuration(300)
                            .withEndAction { ivMedia.gone() }.start()
                    }
                }
            }

            StreamEnum.KEYCODE_MEDIA_REWIND -> {
                playerHandler.seekBackward(10)
                ivMedia.setImageResource(R.drawable.ic_remote_backward)
                ivMedia.visible()
                ivMedia.alpha = 1f
                lifecycleScope.launch {
                    delay(2000)
                    withContext(Dispatchers.Main) {
                        ivMedia.animate().alpha(0f).setStartDelay(10).setDuration(300)
                            .withEndAction { ivMedia.gone() }.start()
                    }
                }
            }

            StreamEnum.KEYCODE_MEDIA_PLAY_PAUSE -> {
                playPauseHandle()

            }

            else -> {}
        }
    }

    fun updatePlayer() = with(binding) {
        playerHandler = PlayerHandler(requireActivity(), playerView)
    }

    fun newVideo() {
        viewModel.getVideo(requireContext(), nextVideoId)
        observe()
    }

    fun observe() {
        viewModel.videoLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isSuccess -> {
                    var data = it.data?.response
                    Log.e("skcmsknc", "$ifFirst scnsknc ${it.data}")
                    if (data != null) {

                        dismissProgress()
                          videoUrl = data.hlsUrl
                        oldEventId = eventId
                        eventId = data.eventId
                        videoCount++
                        mediaId = data.mediaId
                        oldVideoDuration = playbackduration
                        ifFirst = false
                        Log.e("skcmsknc", "play back duration $playbackduration")
                        playerHandler.setMediaUri(videoUrl, playbackduration)
                        if (data.nextVideo != null) {
                            isNewVideoAvailable = true
                            thumbnailS3bucketId = videoThumb
                            videoThumb = data.nextVideo?.nextVideoThumbnail!!
                            oldBunnyId = nextVideoId
                            nextVideoId = data.nextVideo?.nextVideoId.toString()
                            binding.ivNextVideo.loadUrl(videoThumb)
                        } else {
                            isNewVideoAvailable = false
                        }
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (bunneyIdList.none { it.bunneyId == nextVideoId }) {
                                bunneyIdList.add(
                                    BunneyIds(
                                        mediaId = mediaId,
                                        eventId = eventId,
                                        bunneyId = oldBunnyId,
                                        thumb = thumbnailS3bucketId
                                    )
                                )
                            }
                        }


//                        if (ifFirst) {
//                            videoCount++
//                            oldMediaId = mediaId
//                            oldVideoDuration = playbackduration
//                            ifFirst = false
//                            Log.e("skcmsknc", "play back duration $playbackduration")
//                            playerHandler.setMediaUri(videoUrl, playbackduration)
//                            if (data.nextVideo != null) {
//                                isNewVideoAvailable = true
//                                thumbnailS3bucketId = videoThumb
//                                videoThumb = data.nextVideo?.nextVideoThumbnail!!
//                                oldBunnyId = nextVideoId
//                                nextVideoId = data.nextVideo?.nextVideoId.toString()
//                                binding.ivNextVideo.loadUrl(videoThumb)
//                            } else {
//                                isNewVideoAvailable = false
//                            }
//                          lifecycleScope.launch(Dispatchers.IO) {
//                              if (bunneyIdList.none { it.bunneyId == nextVideoId }) {
//                                  bunneyIdList.add(
//                                      BunneyIds(
//                                          mediaId = mediaId,
//                                          eventId = eventId,
//                                          bunneyId = oldBunnyId,
//                                          thumb = thumbnailS3bucketId
//                                      )
//                                  )
//                              }
//                          }
//
//                        }
//                        else {
//                            mediaId = data.mediaId
//                            if (data.nextVideo != null) {
//                                isNewVideoAvailable = true
//                                thumbnailS3bucketId = videoThumb
//                                videoThumb = data.nextVideo?.nextVideoThumbnail!!
//                                oldBunnyId = nextVideoId
//                                nextVideoId = data.nextVideo?.nextVideoId.toString()
//                                binding.ivNextVideo.loadUrl(videoThumb)
//                                binding.ivVideoThumb.loadUrl(videoThumb)
//                                videoCount++
//                            } else {
//                                isNewVideoAvailable = false
//                            }
//
//                        }


                    } else {
                        isNewVideoAvailable = false
                        requireActivity().showMessage("Video not found")
                    }
                }

                else -> {}
            }
        }
    }

    var isNewVideoAvailable = false
    fun clickme() = with(binding) {
        ivBack.setOnClickListener { findNavController().popBackStack() }

        ivPlay.setOnClickListener {
            toShowBackButton()
            val params = ivPlay.layoutParams as LinearLayout.LayoutParams
            params.width =
                resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
            params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
            ivPlay.layoutParams = params
            playPauseHandle()
//            if (playerHandler.player != null) {
//                if (playerHandler.isPlaying()!!) {
//                    playerHandler.pause()
//                    ivPlay.setImageResource(R.drawable.ic_seleceted_play)
//                } else {
//                    if (isEnded) {
//                        playerHandler.player?.seekTo(0)
//                    } else {
//                        playerHandler.play()
//                    }
//                    ivPlay.setImageResource(R.drawable.ic_selected_pause)
//                    updateProgressBar()
//                }
//            }
        }
        ivSkipForward.setOnClickListener {
            toShowBackButton()
            var current = playerHandler.player?.currentPosition
            var duration = playerHandler.player?.duration
            var count = current!! + 10000

            if (duration!! > count) {
                playerHandler.seekTo(count)
            } else {
                playerHandler.seekTo(duration)
            }
//            extractFrameFromVideo()
          //  extractFrameAtTimestamp()
            lifecycleScope.launch(Dispatchers.IO) {
                delay(2000)
//                extractImageAtTimestamp(current)
                extractFrameAtTimestamp()

            }
        }
        ivSkipBack.setOnClickListener {
            toShowBackButton()
            playerHandler.seekBackward(10)
        }
        ivRefresh.setOnClickListener {
            //  playerHandler.toggleFullScreen()
            if (playerHandler.player != null) {
                playerHandler.refresh()
                getLengthOnce = true
//                ivNextVideo.invisible()
                timerLayout.invisible()
                ivPlay.setImageResource(R.drawable.ic_video_pause)
            }
        }

        timerLayout.setOnClickListener {
            resetView()
            playNextVideo()
            listener()
        }

        ivVolume.setOnClickListener {
            toShowBackButton()
            visibilityCount = 0
            if (playerHandler.player != null) {
                if (playerHandler.player?.volume == 0f) {
                    playerHandler.unmute()
                    volumeCount = 1
                    ivVolume.setImageResource(R.drawable.ic_selected_volume)
                    sbVolumeSeek.setProgress(volumeCount)
                } else {
                    playerHandler.mute()
                    ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                    sbVolumeSeek.setProgress(0)
                    volumeManager.setVolumePercentage(0)
                    volumeCount = 0
                }
            }
        }
        llVolumeSeek.setOnClickListener {
        }

    }

    fun playPauseHandle() = with(binding) {
//        val params = ivPlay.layoutParams as LinearLayout.LayoutParams
//        params.width = resources.getDimensionPixelSize(R.dimen._16sdp)
//        params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
//        ivPlay.layoutParams = params
        if (playerHandler.player != null) {
            if (playerHandler.isPlaying()!!) {
                playerHandler.pause()
                if (ivPlay.isFocused) {
                    ivPlay.setImageResource(R.drawable.ic_seleceted_play)
                } else {
                    ivPlay.setImageResource(R.drawable.ic_video_play)
                }
            } else {
                if (isEnded) {
                    playerHandler.player?.seekTo(0)
                } else {
                    playerHandler.play()
                }
                if (ivPlay.isFocused) {
                    ivPlay.setImageResource(R.drawable.ic_selected_pause)
                } else {
                    ivPlay.setImageResource(R.drawable.ic_video_pause)
                }
                updateProgressBar()
            }
        }

    }

    fun playNextVideo() = with(binding) {

        if (isNewVideoAvailable) {
            if (!HomeFragment.isTrailer) {
                oldVideoDuration = playerHandler.getCurrentPosition()
                var newEventId = 0
                var newMediaId = 0
                var newBunneyId = ""
                var newThumb = ""
                if (bunneyIdList.size > 0) {
                    bunneyIdList[bunneyIdList.size - 1].let {
                        newEventId = it.eventId
                        newMediaId = it.mediaId
                        newBunneyId = it.bunneyId
                        newThumb = it.thumb
                    }
                    savePlayback(
                        newEventId,
                        newMediaId,
                        newBunneyId,
                        // oldBunnyId,
                        oldVideoDuration,
                        newThumb
                    )
                }
            }
            isNextVideoStarted = true
            getLengthOnce = true
            isEnded = true
            timerLayout.invisible()
            tvCurrentLenght.setText("")
            tvCurrentLenght.invalidate()
            tvDuration.setText("")
            tvDuration.invalidate()
            sbVideoSeek.requestLayout()
            sbVideoSeek.invalidate()
            sbVideoSeek.progress = 0
            playerView.requestLayout()
            playerView.invalidate()
            playerHandler.stopHandler()
            binding.sbVideoSeek.progress = 0
            //playerHandler.setMediaUri(videoUrl, 0)
            playbackduration = 0
            newVideo()
            focusView = VideoEnum.BACKWARD
        } else {
            findNavController().navigateUp()
        }

    }

    fun resetView() = with(binding) {
        if (playerHandler.player != null) {
            playerHandler.player?.stop()
            // playerHandler.player?.release()
        }
        isNextVideoStarted = true
        getLengthOnce = true
        isEnded = true
        timerLayout.invisible()
        tvCurrentLenght.setText("")
        tvCurrentLenght.invalidate()
        tvDuration.setText("")
        tvDuration.invalidate()
        sbVideoSeek.requestLayout()
        sbVideoSeek.invalidate()
        sbVideoSeek.progress = 0
        playerView.requestLayout()
        playerView.invalidate()
        playerHandler.stopHandler()
        binding.sbVideoSeek.progress = 0
    }

    var surfaceView: SurfaceView? = null
    fun listener() = with(binding) {
        showProgress()
//        val textureId = IntArray(1)
//        GLES20.glGenTextures(1, textureId, 0)
      //  surfaceTexture = SurfaceTexture(textureId[0])
      //  surface = Surface(surfaceTexture)
      //  surfaceView = SurfaceView(requireContext())
     //   playerHandler.player?.setVideoSurface(surface)


        playerHandler.player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    showProgress()
                } else if (playbackState == Player.STATE_READY) {
                    dismissProgress()

//                    if (isEnded) {
//                        lifecycleScope.launch(Dispatchers.IO) {
//                            if (bunneyIdList.none { it.bunneyId == nextVideoId }) {
//                                bunneyIdList.add(
//                                    BunneyIds(
//                                        mediaId = mediaId,
//                                        eventId = eventId,
//                                        bunneyId = oldBunnyId,
//                                        thumb = thumbnailS3bucketId
//                                    )
//                                )
//                            }
//                           // if (!playFirst) {
////                            withContext(Dispatchers.Main) { newVideo() }
//                           // }
//                        }
//                    }

                    homeFragment.isLastPlay = true
                    Log.e(
                        "idcheckstr",
                        "isNextVideoStarted $isNextVideoStarted getLengthOnce $getLengthOnce oldEventId $oldEventId oldMediaId $oldMediaId oldBunnyId $oldBunnyId"
                    )
                    videoTranisition()
                    binding.sbVideoSeek.max = 100
                    if (getLengthOnce) {
                        tvDuration.setText(playerHandler.getTotalLength())

                        getLengthOnce = false
                        ivPlay.setImageResource(R.drawable.ic_video_pause)
                    }
                    isEnded = false

                    updateProgressBar()

                } else if (playbackState == Player.STATE_ENDED) {
                    // filterItem()
                    Log.e("filteridwith", "video ended ")
                    ivPlay.setImageResource(R.drawable.ic_video_play)
                    isEnded = true

//                    newVideo()
                    playNextVideo()

                    viewFocus()
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                if (!isOpenSettingFirst) {
                    isOpenSettingFirst = true
                    qualityList.clear()
                    lifecycleScope.launch(Dispatchers.IO) {
                        val data =
                            QualityModel(
                                "Auto", true,
                                480,
                                854
                            )
                        qualityList.add(data)
                        for (group in tracks.getGroups()) {
                            val trackCount = group.length
                            for (j in 0 until trackCount) {
                                val format = group.getTrackFormat(j)
                                // Check if the format is a video format using supported properties
                                if (format.width > 0 && format.height > 0) {
                                    val width = format.width
                                    val height = format.height
                                    var isSelected = false
//                                    if (j == trackCount - 1) {
//                                        isSelected = true
//                                    }
                                    val data =
                                        QualityModel(
                                            height.toString() + "p", isSelected,
                                            height,
                                            width
                                        )
                                    qualityList.add(data)
                                    Log.d("VideoResolution", "resolution: ${width}x${height}")

                                }
                            }

                        }
                        withContext(Dispatchers.Main) { quality() }
                    }
                }

            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayerError", "by video fragment Playback error: " + error.message, error)
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                Log.e("ExoPlayerError", "onPlayerErrorChanged " + error?.message, error)
            }
        })

        // Set the Surface to the player

//        surfaceView = binding.playerView.videoSurfaceView as SurfaceView
        //getFrameFromPlayerView()
    }
    var surfaceWidth = 0
    var surfaceHeight =0
    private fun getFrameFromPlayerView() {

        val playerView = binding.playerView
        val surfaceView = playerView.videoSurfaceView as SurfaceView
        Log.e("sknvks", "initialize the ${surfaceView::class.java.simpleName} and video holder ${surfaceView.holder}")


        val surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.e("sknvks", "Surface Created")
                lifecycleScope.launch(Dispatchers.IO) {
                    delay(2000)
                    extractImageAtTimestamp(5000L)
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.e(
                    "sknvks",
                    "Surface Changed: width=$width, height=$height  format $format holder $holder"
                )
                surfaceWidth=width
                surfaceHeight=height
                lifecycleScope.launch(Dispatchers.IO) {
                    delay(2000)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.e("sknvks", "Surface Destroyed")
                playerHandler.player?.setVideoSurface(null)
            }
        })


    }

    private var eglDisplay: EGLDisplay? = null
    private var eglSurface: EGLSurface? = null
    private var eglContext: EGLContext? = null
    private var frameCaptureRunnable: Runnable? = null
    fun initializeEGL(surface: Surface) {
        // Initialize EGL only once when the surface is created
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw RuntimeException("Failed to initialize EGL display")
        }

        val config = chooseEGLConfig(eglDisplay!!)
        eglContext = createEGLContext(eglDisplay!!, config)
        eglSurface =
            EGL14.eglCreateWindowSurface(eglDisplay, config, surface, intArrayOf(EGL14.EGL_NONE), 0)

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw RuntimeException("Failed to make EGL context current")
        }
    }

    fun startPeriodicCapture(width: Int, height: Int) {
        // Use a timer or handler to periodically trigger frame capture
        //  frameCaptureRunnable = Runnable {

        captureFrame(width, height)

        //  }

        // Start periodic capture every 1000ms (1 second)
//        val intervalMillis = 1000L
//        val handler = android.os.Handler()
//        handler.postDelayed(frameCaptureRunnable!!, intervalMillis)
    }

    private fun captureFrame(width: Int, height: Int) {
        // Use OpenGL to capture the frame from the Surface
        val frameBuffer = IntArray(1)
        GLES20.glGenFramebuffers(1, frameBuffer, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])

        val pixels = IntArray(width * height)
        GLES20.glReadPixels(
            0,
            0,
            width,
            height,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            IntBuffer.wrap(pixels)
        )

        if (pixels.isEmpty()) {
            Log.e("logsss", "Error: No pixels were read.")
        } else {
            Log.e("logsss", "Pixels captured: ${pixels.size} pixels.")
        }
        val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        val flippedBitmap = flipBitmapVertically(bitmap)
        // Convert to Bitmap
        lifecycleScope.launch(Dispatchers.Main) {

            binding.ivSeekThumb.apply {
                visible()
                setImageBitmap(flippedBitmap)
            }
//            binding.ivPlay.setImageBitmap(flippedBitmap)
            Log.e("logsss", "djbcjs bitmap $bitmap")
        }
        // Save or process the Bitmap
    }

    private fun flipBitmapVertically(bitmap: Bitmap): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postScale(1f, -1f) // Flip vertically
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
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
        EGL14.eglChooseConfig(
            eglDisplay,
            configAttribs,
            0,
            configs,
            0,
            numConfigs[0],
            numConfigs,
            0
        )

        return configs[0] ?: throw RuntimeException("Unable to choose EGLConfig")
    }

    private fun createEGLContext(eglDisplay: EGLDisplay, config: EGLConfig): EGLContext {
        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, // OpenGL ES 2.0
            EGL14.EGL_NONE
        )

        return EGL14.eglCreateContext(eglDisplay, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
    }


    private fun extractFramesEvery10Seconds() {
        Log.e("videourl", "videoUrl $videoUrl uri ")
        val retriever = MediaMetadataRetriever()
        val uri = Uri.parse(videoUrl)

        try {
            retriever.setDataSource(requireContext(), uri)
            lifecycleScope.launch(Dispatchers.IO) {   // Create a MediaMetadataRetriever instance
                val duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong()
                        ?: 0
                val interval = 10000L // 10 seconds in milliseconds

                val bitmap: Bitmap? =
                    retriever.getFrameAtTime(interval * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
                bitmap?.let {
                    withContext(Dispatchers.Main) {
                        binding.ivSkipBack.setImageBitmap(bitmap)
                        Log.e("FrameCapture", "MediaMetadataRetriever $bitmap")
                    }
                }
                retriever.release()
            }
        } catch (e: IllegalArgumentException) {
            Log.e(
                "FrameCapture",
                "Error setting data source: Invalid URL or file path $videoUrl",
                e
            )
        } catch (e: Exception) {
            Log.e("FrameCapture", "Unexpected error", e)
        } finally {
            retriever.release()  // Ensure retriever is released after use
        }
    }

    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private fun extractFrameAtTimestampddd() {
        // Seek to the desired timestamp and pause the player
        playerHandler.apply {

            val bitmap = captureFrameFromSurface(surfaceTexture!!)
            Log.e("sknvks", " bitmap $bitmap")
            if (bitmap != null) {
                binding.ivSeekThumb.setImageBitmap(bitmap)
            }

        }
    }

    private fun captureFrameFromSurface(surfaceTexture: SurfaceTexture): Bitmap? {
        // Initialize OpenGL context and framebuffer
        val width = 1920 // Video width
        val height = 1080 // Video height

        // Create an OpenGL framebuffer and texture to read from
        val frameBuffer = IntArray(1)
        GLES20.glGenFramebuffers(1, frameBuffer, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])

        val textureId = IntArray(1)
        GLES20.glGenTextures(1, textureId, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])

        // Set texture parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // Attach the texture to the framebuffer
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId[0], 0)

        // Clear the framebuffer
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Update the SurfaceTexture with the current frame
        //surfaceTexture.updateTexImage()

        // Read the pixels from the framebuffer
        val buffer = ByteBuffer.allocateDirect(4 * width * height)
        buffer.order(ByteOrder.nativeOrder())
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)

        // Flip the buffer for bitmap creation
        buffer.rewind()
        val pixels = IntArray(width * height)
        buffer.asIntBuffer().get(pixels)

        // Create a Bitmap from the pixel array
        val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

        // Clean up OpenGL resources
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glDeleteFramebuffers(1, frameBuffer, 0)
        GLES20.glDeleteTextures(1, textureId, 0)

        return bitmap
    }


    private fun extractImageAtTimestamp(timestamp: Long) {
        if (videoUrl != null) {
            Log.e("sknvks", "Video URI is null $videoUrl")
        //    val retriever = MediaMetadataRetriever()
            try {

                val bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.ARGB_8888)

                // 4. Draw the Surface content onto the Canvas
                val canvas = Canvas(bitmap)
                surfaceView?.draw(canvas)

                // 5. Use this bitmap as needed (e.g., display it on ImageView)
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.e("sknvks", "Frame captured at $timestamp ms $bitmap")
                    binding.ivSeekThumb.setImageBitmap(bitmap)
                }


//                retriever.setDataSource(videoUrl)
//                val bitmap: Bitmap? = retriever.getFrameAtTime(
//                    timestamp * 1000,
//                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
//                )
//                bitmap?.let {
//                    Log.e("sknvks", "Frame captured at $timestamp ms $bitmap")
//                    lifecycleScope.launch(Dispatchers.Main) { binding.ivSeekThumb.setImageBitmap(it) }
//                } ?: Log.e("sknvks", "No frame available at $timestamp ms")
            } catch (e: Exception) {
                Log.e("sknvks", "Error extracting frame: ${e.message}")
            } finally {
               // retriever.release()
            }
        }
    }


    private fun extractFrameAtTimestampddddd(timestamp: Long) {
        // FFmpeg command to extract a frame at the given timestamp (in seconds)
        val timestampInSeconds = timestamp / 1000.0
//        val command = arrayOf(
//            "-i", videoUrl,                // Input video URL
//            "-ss", String.format("%.3f", timestampInSeconds), // Seek to the timestamp
//            "-vframes", "1",               // Extract one frame
//            "-q:v", "2",                   // Set the quality of the output image
//            "pipe:1"                       // Output to stdout (pipe)
//        )
//        val command = "-ss ${timestampInSeconds.toInt()} -i $videoUrl -vframes 1 -f image2 -vcodec png pipe:1"

//        val command = arrayOf(
//            "-i", videoUrl,                // Input video URL
//            "-ss", "00:00:5.000",          // Seek to the timestamp
//            "-vframes", "1",               // Extract one frame
//            "-q:v", "2",                   // Set the quality of the output image
//            "-f", "image2",                // Specify the output format
//            "pipe:1"                       // Output to stdout (pipe)
//        )
        val command = arrayOf(
            "-i", videoUrl,                // Input video URL
            "-ss", "00:00:5.000",         // Seek to the timestamp
            "-vframes", "1",               // Extract one frame
            "-f", "image2",                // Specify the output format
            "-pix_fmt", "rgba",            // Set pixel format to RGBA for PNG
            "pipe:1"                       // Output to stdout (pipe)
        )
        val session = FFmpegKit.execute(command.joinToString(" "))

        if (!ReturnCode.isSuccess(session.getReturnCode())) {
            Log.d("FFmpeg", "Command failed. Please check output for the details.")
            val logs = session.getLogs()
            logs.forEach { log ->
                Log.d("FFmpeg Log", log.getMessage())
            }
        } else {
            // Capture the output image from stdout
            val output = session.getOutput()
            if (output != null) {
                // Convert the output byte array to a Bitmap
                val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(output.toByteArray()))
                if (bitmap != null) {
                    Log.d("FFmpeg", "Bitmap dimensions: ${bitmap.width} x ${bitmap.height} bitmap $bitmap")
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.ivSeekThumb.setImageBitmap(bitmap)
                        Log.e("FFmpeg", "Frame extraction successful.")
                    }
                } else {
                    Log.e("FFmpeg", "Bitmap is null")
                }
            }
        }
        // Run FFmpegKit to execute the command
//        FFmpegKit.executeAsync(command) { session ->
//            val returnCode = session.returnCode
//            Log.e("FFmpeg", "output $session")
//            if (ReturnCode.isSuccess(returnCode)) {
//                val outputData = session.output
//               // val bitmap = getBitmapFromByteArray(outputData)
//
//               // Log.e("FFmpeg", "convert output to bitmap. $bitmap")
//               // if (bitmap != null) {
//                    lifecycleScope.launch(Dispatchers.Main) {
////                        binding.ivSeekThumb.setImageBitmap(bitmap)
//                        binding.ivSeekThumb.loadUrl(outputData)
//                  //  }
////                } else {
////                    Log.e("FFmpeg", "Failed to convert output to bitmap.")
//                }
//            } else {
//                // Handle failure
//                val errorMessage = session.failStackTrace
//                Log.e("FFmpeg", "FFmpeg failed: $errorMessage")
//            }
//        }
    }
    private fun getByteArrayFromOutput(output: String?): ByteArray? {
        return try {
            // FFmpegKit output is a path or URL. We need to read the actual content.
            val inputStream = FileInputStream(output) // Open the output file
            val byteArrayOutputStream = ByteArrayOutputStream()

            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            byteArrayOutputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("FFmpeg", "Error reading FFmpeg output: ${e.message}")
            null
        }
    }
var timestamp=5
    private fun extractFrameAtTimestamp() {
          // Set the timestamp where you want to extract the frame
//        val outputFile = File(requireActivity().filesDir, "extracted_frame.png")  // Specify output file path

//
//        val command = arrayOf(
//            "-y",
//            "-i", videoUrl,
//            "-ss", "00:00:$timestamp.000",  // Timestamp
//            "-vframes", "1",        // Single frame extraction
//            "-an",                  // Disable audio
//            "-f", "image2pipe",     // Output format as image pipe
//            "-pix_fmt", "rgba",               // Output as RGBA (PNG)
//            "-vcodec", "png",      // Output as RGBA PNG
//            outputFile.absolutePath // Save to file
//        )
       // var duration = playerHandler.getDuration() / 1000
      //  lifecycleScope.launch(Dispatchers.IO) {

            val fileArray = mutableListOf<File>()
          //  for (timestamp in 0..duration step 10) {
                val outputFile = File(requireActivity().cacheDir, "extracted_frame_${timestamp}.png")
                val command = arrayOf(
                    "-y",                             // Force overwrite of existing files
                    "-ss",
                    String.format("00:00:%02d.000", timestamp),  // Timestamp in HH:MM:SS format
                    "-i",
                    videoUrl,                   // Input video URL
                    "-vframes",
                    "1",                  // Extract a single frame
                    "-an",                            // Disable audio
                    "-f",
                    "image2pipe",               // Output format: image pipe
                    "-pix_fmt",
                    "rgba",               // Use RGBA for PNG
                    "-vcodec",
                    "png",                 // Use PNG codec
                    outputFile.absolutePath          // Write output directly to file
                )
        timestamp+=5
                val session = FFmpegKit.execute(command.joinToString(" "))

                if (!ReturnCode.isSuccess(session.returnCode)) {
                    val logs = session.logs
                    logs.forEach { log ->
                        Log.d("FFmpeg Log", log.message)
                    }
                }
                else {
                    val output = session.output
                    if (output != null && output.isNotEmpty()) {
                        val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
                        Log.d(
                            "FFmpeg",
                            "Output size: ${output.length} bytes ${bitmap.width} height ${bitmap.height} bitmap $bitmap "
                        )
                        if (bitmap != null) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                binding.ivSeekThumb.setImageBitmap(bitmap)  // Set the Bitmap to the ImageView
                                Log.d("FFmpeg", "Image displayed successfully.")
                            }
                        } else {
                            Log.e("FFmpeg", "Failed to decode Bitmap from file.")
                        }
                    } else {
                        Log.e("FFmpeg", "Output is null or empty")
                    }
             //   }
           // }
            Log.d("FFmpeg", "Total frames extracted: ${fileArray.size}")

        }
    }

    fun videoTranisition() = with(binding) {
        ivVideoThumb.animate()
            .alpha(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(1500)
            .withEndAction {
                playerView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(DecelerateInterpolator())// Scale to original size
                    .setDuration(50)
                    .start()
            }
            .start()
    }

    fun thumbShow() = with(binding) {
        ivVideoThumb.run {
            alpha = 0f
            visible()
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(5000)
                .start()
        }

    }

    fun volumeUp() {
        if (volumeCount <= 99) {
            volumeCount += 1
            playerHandler.setVolume(volumeCount / 100.0f)
            volumeManager.setVolumePercentage(volumeCount)
        }
    }

    fun volumeDown() {
        if (volumeCount >= 1) {
            volumeCount -= 1
            playerHandler.setVolume(volumeCount / 100.0f)
            volumeManager.setVolumePercentage(volumeCount)
        }
    }

    fun forward() {
        if (::playerHandler.isInitialized) {
            var current = playerHandler.player?.currentPosition
            var duration = playerHandler.player?.duration
            var count = current!! + 30000
            if (duration!! > count) {
                playerHandler.seekTo(count)
            } else {
                playerHandler.seekTo(duration)
            }
        }

    }

    fun forward10() {
        if (::playerHandler.isInitialized) {
            var current = playerHandler.player?.currentPosition
            var duration = playerHandler.player?.duration
            var count = current!! + 10000
            if (duration!! > count) {
                playerHandler.seekTo(count)
            } else {
                playerHandler.seekTo(duration)
            }
        }
    }

    fun selectorFocus() = with(binding) {
        ivSkipBack.requestFocus()
        ivSkipBack.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.BACKWARD
            }
        }
        ivSkipForward.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.FORWARD
            }
        }
        ivPlay.setOnFocusChangeListener { _, hasFocus ->
            if (playerHandler.player != null) {
                if (hasFocus) {
                    toShowBackButton()
                    focusView = VideoEnum.VIDEO_PLAY

                    if (playerHandler.isPlaying()!!) {
                        ivPlay.setImageResource(R.drawable.ic_selected_pause)
                    } else {
                        ivPlay.setImageResource(R.drawable.ic_seleceted_play)
                    }
                } else {
                    if (playerHandler.isPlaying()!!) {
                        ivPlay.setImageResource(R.drawable.ic_video_pause)
                    } else {
                        ivPlay.setImageResource(R.drawable.ic_video_play)
                    }

                }
            }

        }

        ivSetting.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.SETTING
//                val params = ivSetting.layoutParams as LinearLayout.LayoutParams
//                params.width = resources.getDimensionPixelSize(R.dimen._16sdp)
//                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
//                ivSetting.layoutParams = params

            } else {
//                val params = ivSetting.layoutParams as LinearLayout.LayoutParams
//                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
//                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
//                ivSetting.layoutParams = params

            }
        }

        ivVolume.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.VOLUME
//                val params = ivVolume.layoutParams as LinearLayout.LayoutParams
//                params.width =
//                    resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
//                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
//                ivVolume.layoutParams = params
                if (playerHandler.player != null) {
                    if (playerHandler.player?.volume == 0f) {
//                if (playerHandler.isMuted()) {
                        ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                    } else {
                        ivVolume.setImageResource(R.drawable.ic_selected_volume)
                    }
                }

            } else {
//                val params = ivSetting.layoutParams as LinearLayout.LayoutParams
//                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
//                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
//                ivVolume.layoutParams = params
                if (playerHandler.player != null) {
                    if (playerHandler.player?.volume!! > 0f) {
//                if (playerHandler.isMuted()) {
                        ivVolume.setImageResource(R.drawable.ic_video_volume)
                    } else {
                        ivVolume.setImageResource(R.drawable.ic_mute)
                    }
                }
            }
        }

        ivBack.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.BACK_TO_VIDEO
                val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._62sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._33sdp)
                ivBack.layoutParams = params
            } else {
                val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._60sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._31sdp)
                ivBack.layoutParams = params
            }
        }
        ivRefresh.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.REFRESH
            }
        }
        timerLayout.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.NEXT_VIDEO
//                val params = ivNextVideo.layoutParams as ConstraintLayout.LayoutParams
//                params.width = resources.getDimensionPixelSize(R.dimen._90sdp)
//                params.height = resources.getDimensionPixelSize(R.dimen._50sdp)
//                ivNextVideo.layoutParams = params
            }
//            else {
//                val params = ivNextVideo.layoutParams as ConstraintLayout.LayoutParams
//                params.width = resources.getDimensionPixelSize(R.dimen._80sdp)
//                params.height = resources.getDimensionPixelSize(R.dimen._40sdp)
//                ivNextVideo.layoutParams = params
//            }

        }

        sbVideoSeek.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.VIDEO_SEEK
            }
        }
        sbVolumeSeek.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.VOLUME_SEEK
            }
        }

//        llTools.setOnClickListener { }
//        llTools.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                ivSkipBack.requestFocus()
//                focusView = VideoEnum.BACKWARD
//            }
//        }
//        llVolumeSeek.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                llVolumeSeek.setBackgroundColor(ContextCompat.getColor(requireActivity(),R.color.light_gray) )
//            }else{
//                llVolumeSeek.setBackgroundColor(ContextCompat.getColor(requireActivity(),
//                    com.otpview.R.color.transparent) )
//            }
//        }

    }


    private fun quality() = with(binding) {
//        val qualityButtons = ArrayList<TextView>()
//        qualityButtons.clear()
//        if (isSmartRevision) {
//            qualityButtons.add(tv480p)
//            qualityButtons.add(tv1080p)
//            qualityButtons.add(tv2080p)
//        }
//        else {
//            qualityButtons.add(tv480p)
//            tv1080p.gone()
//            tv2080p.gone()
//        }
//        ivSetting.setOnClickListener {
//            if (clSettingsMenu.isVisible) {
//                clSettingsMenu.gone()
//            } else {
//                clSettingsMenu.visible()
//
//                qualityButtons[videoQualityIndex].requestFocus()
//                qualityButtons[videoQualityIndex].setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireActivity(),
//                        R.color.light_gray
//                    )
//                )
//            }
//        }
//        qualityButtons.forEachIndexed { index, textView ->
//            textView.setOnClickListener {
//                clSettingsMenu.gone()
//              //  playerHandler.setQuality(textView.text.toString())
//                ivSetting.requestFocus()
//                isOpenSettingFirst = true
//                qualityButtons.forEachIndexed { subindex, subText ->
//                    if (index == subindex) {
//                        subText.setBackgroundColor(
//                            ContextCompat.getColor(
//                                requireActivity(),
//                                R.color.light_gray
//                            )
//                        )
//                        videoQualityIndex = subindex
//
//                    } else {
//                        subText.setBackgroundColor(
//                            ContextCompat.getColor(
//                                requireActivity(),
//                                R.color.white
//                            )
//                        )
//                    }
//
//                }
//            }
//
//            textView.setOnFocusChangeListener { _, hasFocus ->
//                if (hasFocus) {
//                    textView.setBackgroundColor(
//                        ContextCompat.getColor(
//                            requireActivity(),
//                            R.color.light_gray
//                        )
//                    )
//                } else {
//                    textView.setBackgroundColor(
//                        ContextCompat.getColor(
//                            requireActivity(),
//                            R.color.white
//                        )
//                    )
//                    // clSettingsMenu.gone()
//                }
//            }
//
//            textView.setOnKeyListener { v, keyCode, event ->
//                visibilityCount = 0
//                if (event.action == KeyEvent.ACTION_DOWN) {
//                    when (keyCode) {
//                        KeyEvent.KEYCODE_DPAD_DOWN -> {
//                            if (index + 1 < qualityButtons.size) {
//                                qualityButtons[index + 1].requestFocus()
//                            } else {
//                                //qualityButtons[index].requestFocus()
//                                ivSetting.requestFocus()
//                            }
//                            return@setOnKeyListener true
//                        }
//
//                        KeyEvent.KEYCODE_DPAD_UP -> {
//                            if (index - 1 >= 0) {
//                                qualityButtons[index - 1].requestFocus()
//                            } else {
//                                qualityButtons[index].requestFocus()
////                                ivBack.requestFocus()
//                            }
//                            return@setOnKeyListener true
//                        }
//
//                    }
//                }
//                false
//            }
//        }

        // view initialize

        ivSetting.setOnClickListener {
            if (clSettingsMenu.isVisible) {
                clSettingsMenu.gone()

            } else {
                qualityList.forEachIndexed { index, qualityModel ->
                    if (qualityModel.isSelected) {
                        clSettingsMenu.requestFocus()
                        rvQuality.requestFocus()
                        rvQuality.post {
                            rvQuality.getChildAt(index)?.requestFocus()
                        }
                    } else {
                        rvQuality.post {
                            rvQuality.getChildAt(index)?.clearFocus()
                        }
                    }
                }
                qualityAdapter.notifyDataSetChanged()
                rvQuality.adapter = qualityAdapter
                clSettingsMenu.visible()
            }
        }
        qualityInit()

    }

    fun qualityInit() = with(binding) {
        rvQuality.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            qualityAdapter = QualityAdapter(requireActivity(), qualityList) {

                clSettingsMenu.gone()
                ivSetting.requestFocus()

                if (qualityList[it].title == "Auto") {
                    playerHandler.setAutoResolutionBasedOnBandwidth()
                } else {
                    playerHandler.setQuality(qualityList[it])
                }
            }
            adapter = qualityAdapter
        }
    }

    private fun volume() = with(binding) {

        volumeManager.setOnVolumeChangeListener { volumePercentage ->
            // Update the SeekBar with the volume percentage
            Log.e("sjbcjsbc", "remote volume before $volumeCount after $volumePercentage")
            if (!isVolume) {
                lifecycleScope.launch(Dispatchers.Main) {
                    volumeCount = volumePercentage
                    // toShowBackButton()
                    sbVolumeSeek.setProgress(volumeCount)
                }
                if (ivVolume.isFocused) {
                    if (volumePercentage <= 0) {
                        playerHandler.mute()
                        ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                    } else {
                        playerHandler.setVolume(volumeCount / 100.0f)
                        ivVolume.setImageResource(R.drawable.ic_selected_volume)
                    }
                } else {
                    if (volumePercentage <= 0) {
                        playerHandler.mute()
                        ivVolume.setImageResource(R.drawable.ic_mute)
                    } else {
                        playerHandler.setVolume(volumeCount / 100.0f)
                        ivVolume.setImageResource(R.drawable.ic_video_volume)
                    }
                }
            }
            isVolume = false
        }
        volumeManager.startMonitoring()
    }

    var time: Long = 10
    private fun updateProgressBar() {
        if (playerHandler.player != null) {
            val duration = playerHandler.getDuration()
            val currentPosition = playerHandler.getCurrentPosition()
            val progress = (currentPosition * 100 / duration.toDouble()).toInt()
            binding.sbVideoSeek.progress = progress
            binding.tvCurrentLenght.text = playerHandler.getcurrent().toString()
            if (playerHandler.isPlaying()!!) {
                binding.tvDuration.text = playerHandler.getRemainsDuration()
                playerHandler.handler.postDelayed({ updateProgressBar() }, 1000)
            }
            visibilityCount++
            if (visibilityCount == 5) {
                visibilityCount = 0
                binding.ivBack.animate().alpha(0f).setDuration(400).setStartDelay(10)
                binding.llTools.animate().alpha(0f).setDuration(400).setStartDelay(10)
                binding.playerView.requestFocus()
                binding.clSettingsMenu.gone()
            }

//             lifecycleScope.launch(Dispatchers.IO) {
            Log.e(
                "focussss",
                "timmer $focusView progress ${progress} duration $currentPosition isNewVideoAvailable $isNewVideoAvailable"
            )
            if (isNewVideoAvailable) {
                var video_show_count = duration - currentPosition
                if (duration > 10000) {
                    if (video_show_count <= 10000) {
                        visibilityCount = 0
                        time = video_show_count + 1000
                        while (time >= 10) {
                            time /= 10
                        }
                        Log.e(
                            "timechecks",
                            "$time timmer $video_show_count duration $currentPosition"
                        )
                        if (!binding.timerLayout.isVisible) {
                            focusView = VideoEnum.NEXT_VIDEO
                            binding.timerLayout.apply {
                                visible()
                                isFocusable = true
                                isFocusableInTouchMode = true
                                requestFocus()
                            }
                            binding.tvRemains.setText("Playing Next Video in $time s")
                            if (time >= 1L) {
                                binding.tvRemains.setText("Playing Next Video in $time s")
                            }
                            Log.e("sbhsbc", "10000 now visible $video_show_count")
                            viewFocus()
                            //  newVideo()

                        } else {

                            if (time >= 1L) {
                                binding.tvRemains.setText("Playing Next Video in $time s")
                            }
                        }
                        time--
                    } else if (binding.timerLayout.isVisible) {
                        binding.timerLayout.gone()
                        time = 0
//                        if (::countDownTimer.isInitialized) {
//                            countDownTimer.onFinish()
//                        }
                    }

                } else {
                    if (video_show_count <= 10000) {
                        visibilityCount = 0
                        time = video_show_count + 1000
                        while (time >= 10) {
                            time /= 10
                        }
                        if (!binding.timerLayout.isVisible) {
                            focusView = VideoEnum.NEXT_VIDEO
                            binding.timerLayout.apply {
                                visible()
                                isFocusable = true
                                isFocusableInTouchMode = true
                                requestFocus()
                            }
                            if (time >= 1L) {
                                binding.tvRemains.setText("Playing Next Video in $time s")
                            }
                            Log.e("sbhsbc", "3000 now visible $video_show_count")
                            viewFocus()
                            //  newVideo()
                        } else {
                            if (time >= 1L) {
                                binding.tvRemains.setText("Playing Next Video in $time s")
                            }
                        }
                        time--
                    } else if (binding.timerLayout.isVisible) {
                        time = 0
//                        if (::countDownTimer.isInitialized) {
//                            countDownTimer.onFinish()
//                        }
                        binding.timerLayout.gone()
                    }

                }
            } else {
                binding.timerLayout.apply {
                    gone()
                }
            }
        }

    }

    lateinit var countDownTimer: CountDownTimer

    fun showRemainsTime(time: Long) = with(binding) {
//        tvRemains.startCountdownTimer(
//            time,
//            onFinish = {
//                timerLayout.gone()
//            },
//            onTick = { seconds ->
//
//                val formattedSeconds = seconds.toString().padStart(2, '0')
//
//                if (formattedSeconds != "00") {
//                    timerLayout.visible()
//                    tvRemains.apply {
////                        text = "Resend OTP in 00:" + formattedSeconds
//                        text= "Playing Next Video in $formattedSeconds s"
//                       // text = "00:" + formattedSeconds
////                        isEnabled = false
////                        if (isAdded) {
////                            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
////                        }
//                        //clearFocus()
//                    }
//                }
//            }
//        )

        countDownTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toLong()
                if (seconds == 0L) {
                    onFinish()
                    timerLayout.gone()
                } else {

//                    val formattedSeconds = seconds.toString().padStart(2, '0')
                    //  if (formattedSeconds != "00") {
                    timerLayout.visible()
                    tvRemains.apply {
                        text = "Playing Next Video in $seconds s"
                    }
                    // }
                }
            }

            override fun onFinish() {
                onFinish()
            }
        }
        countDownTimer.start()

    }

    fun toShowBackButton() = with(binding) {
        binding.ivBack.animate().alpha(1f).setDuration(50).setStartDelay(50)
        binding.llTools.animate().alpha(1f).setDuration(50).setStartDelay(50)
        visibilityCount = 0
//        binding.ivBack.visible()
        clSettingsMenu.gone()
        binding.playerView.clearFocus()
    }

    fun handleKey(view: View) {

        view.setFocusableInTouchMode(true)
        view.requestFocus()
        view.setOnKeyListener { v, keyCode, event ->
            Log.e("mremote", "kckdnc $keyCode event $event")
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        toShowBackButton()
//                        binding.ivBack.requestFocus()
                        viewFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        toShowBackButton()
//                        binding.ivSkipBack.requestFocus()
                        viewFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        toShowBackButton()
                        viewFocus()
//                        binding.ivSkipBack.requestFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        toShowBackButton()
                        viewFocus()
//                        binding.ivRefresh.requestFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                        forward10()
                        binding.apply {
                            ivMedia.setImageResource(R.drawable.ic_remote_forward)
                            ivMedia.visible()
                            ivMedia.alpha = 1f
                            lifecycleScope.launch {
                                delay(2000)
                                withContext(Dispatchers.Main) {
                                    // ivMedia.gone()
                                    ivMedia.animate().alpha(0f).setStartDelay(10).setDuration(300)
                                        .withEndAction { ivMedia.gone() }.start()
                                }
                            }
                        }
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
                        playerHandler.seekBackward(10)
                        binding.apply {
                            ivMedia.setImageResource(R.drawable.ic_remote_backward)
                            ivMedia.visible()
                            ivMedia.alpha = 1f
                            lifecycleScope.launch {
                                delay(2000)
                                withContext(Dispatchers.Main) {
                                    ivMedia.animate().alpha(0f).setStartDelay(10).setDuration(300)
                                        .withEndAction { ivMedia.gone() }.start()
                                }
                            }
                        }
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        playPauseHandle()
                        return@setOnKeyListener true
                    }


                }
            }
            false
        }
//       MainActivity().onKeyDown()
    }


    override fun onPause() {
        super.onPause()
        if (playerHandler.player != null) {
            if (!HomeFragment.isTrailer) {
                playerHandler.player?.run {
                    HomeFragment.videoduraion = currentPosition
                }
            }
            if (playerHandler.isPlaying()!!) {

                playerHandler.pause()
            }
        }
    }

    override fun onStop() {

        super.onStop()
    }

    override fun onDestroy() {
        if (playerHandler.player != null) {
            if (!HomeFragment.isTrailer) {
                playerHandler.player?.run {
                    HomeFragment.videoduraion = currentPosition
                }
                oldVideoDuration = playerHandler.getCurrentPosition()
                var newEventId = 0
                var newMediaId = 0
                var newBunneyId = ""
                var newThumb = ""
                if (bunneyIdList.size > 0) {
                    bunneyIdList[bunneyIdList.size - 1].let {
                        newEventId = it.eventId
                        newMediaId = it.mediaId
                        newBunneyId = it.bunneyId
                        newThumb = it.thumb
                    }

                    savePlayback(
                        newEventId,
                        newMediaId,
                        newBunneyId,
                        oldVideoDuration,
                        newThumb
                    )
                }
            }

            playerHandler.pause()
            playerHandler.release()
            volumeManager.stopMonitoring()
            isVolume = true
        }
        requireActivity().window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // homeFragment.viewFocus()
        super.onDestroy()

    }

    override fun onResume() {
        super.onResume()
        binding.playerView.onResume()
        if (isNetworkAvailable) {
            if (playerHandler.player != null) {
                playerHandler.seekTo(HomeFragment.videoduraion)
                playerHandler.play()
                updateProgressBar()
                if (playerHandler.player?.isPlaying!!) {
                    binding.ivPlay.setImageResource(R.drawable.ic_selected_pause)
                }
            }
            isOpenSettingFirst = false
        }
    }

    override fun netStatus() {
        Log.e(
            "videoscreen",
            "videotest $isNetworkAvailable isNewVideoAvailable $isNewVideoAvailable"
        )
//        if (isNetworkAvailable){
//            if (isNewVideoAvailable) {
//                if (playerHandler.player != null) {
//                    playerHandler.seekTo(HomeFragment.videoduraion)
//                    playerHandler.play()
//                    updateProgressBar()
//                    if (playerHandler.player?.isPlaying!!) {
//                        binding.ivPlay.setImageResource(R.drawable.ic_selected_pause)
//                    }
//                }
//                isOpenSettingFirst = false
//            }
//        }else{
//            if (playerHandler.player != null) {
//                if (!HomeFragment.isTrailer) {
//                    playerHandler.player?.run {
//                        HomeFragment.videoduraion = currentPosition
//                    }
//                }
//                if (playerHandler.isPlaying()!!) {
//                    playerHandler.pause()
//                }
//            }
//        }
    }

    fun savePlayback(
        event: Int,
        mediaId: Int,
        bunneyId: String,
        videoDuration: Long,
        thumb: String
    ) {
        if (videoDuration >= 0) {
            var request = PlayBackRequest()
            request.phoneNumber = phone
            request.mediaId = mediaId
            request.videoId = bunneyId
            request.duration = videoDuration.toString()

            Log.e(
                "filteridwith",
                "mediaId $mediaId event id $event bunneyId $bunneyId savePlayback api initialize $videoDuration  $request"
            )

            homeFragment.filterItem(event, mediaId, videoDuration, bunneyId, thumb, isEnded)
            homeFragment.saveDuration(request)
            lifecycleScope.launch(Dispatchers.Main) {

//                viewModel.saveDuration(requireActivity(), request)
//                durationObserve(event, mediaId, videoDuration)
            }
        }
    }

    fun durationObserve(event: Int, mediaId: Int, videoDuration: Long) {
        viewModel._videoduraion.observe(requireActivity()) {
            when (it) {
                is MyResource.isLoading -> {
                }

                is MyResource.isSuccess -> {

                }

                is MyResource.isError -> {
                }

                else -> {}
            }
        }
    }

    var focusView = VideoEnum.BACKWARD
    fun viewFocus() = with(binding) {
        Log.e("handlefocus", "focus $focusView")
        playerView.clearFocus()
        when (focusView) {
            VideoEnum.BACKWARD -> {
                ivSkipBack.requestFocus()
            }

            VideoEnum.FORWARD -> {
                ivSkipForward.requestFocus()
            }

            VideoEnum.VIDEO_PLAY -> {
                ivPlay.requestFocus()
            }

            VideoEnum.VIDEO_SEEK -> {
                sbVideoSeek.requestFocus()
            }

            VideoEnum.SETTING -> {
                ivSetting.requestFocus()
            }

            VideoEnum.VOLUME -> {
                ivVolume.requestFocus()
            }

            VideoEnum.VOLUME_SEEK -> {
                sbVolumeSeek.requestFocus()
            }

            VideoEnum.REFRESH -> {
                ivRefresh.requestFocus()
            }

            VideoEnum.NEXT_VIDEO -> {
                timerLayout.requestFocus()
            }

            VideoEnum.BACK_TO_VIDEO -> {
                ivBack.requestFocus()
            }

            VideoEnum.RESULATION -> {
                // timerLayout.requestFocus()
            }

            else -> {}
        }

    }


}