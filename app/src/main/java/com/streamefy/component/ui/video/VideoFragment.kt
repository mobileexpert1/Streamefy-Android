package com.streamefy.component.ui.video

import VolumeManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
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
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
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
import com.streamefy.utils.HlsFrameExtractor
import com.streamefy.utils.gone
import com.streamefy.utils.invisible
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showMessage
import com.streamefy.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
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
    var bufferCount = 0

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
    private lateinit var textureView: TextureView
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
            texture()
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
                        playerHandler.seekBackward(30) {
                            seekThumb(it, false)
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
            mediaKey(it)
            visibilityCount = 0
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
            mediaKey(it)
            visibilityCount = 0
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

        videoSeekListener()
    }

    fun videoSeekListener() = with(binding) {
        sbVideoSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (isFraming) {
                }
                val xPosition: Float = seekBar.getWidth() * progress / 100.0f
                ivSeekThumb.setTranslationX(xPosition - ivSeekThumb.getWidth() / 2)
                Log.e("djbvjdbv", "dncdknv $progress ")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
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
                playerHandler.seekBackward(10) {
                    seekThumb(it, false)
                }
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
                       // extractFramesToCache()
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
        var fCount = 1
        ivSkipForward.setOnClickListener {
            toShowBackButton()
            var current = playerHandler.player?.currentPosition
            var duration = playerHandler.player?.duration
            var count = current!! + 10000

            if (duration!! > count) {
                playerHandler.seekTo(count)
                lifecycleScope.launch(Dispatchers.IO) {
                    seekThumb(count, true)
                }
            } else {
                playerHandler.seekTo(duration)
                lifecycleScope.launch(Dispatchers.IO) {
                    seekThumb(duration, true)
                }
            }

//            extractFrameFromVideo()
//                extractImageAtTimestamp(current)


//                fCount +=1
//                var image=getPngFramesFromCache()
//                var thumb="https://vz-52ddfc78-e76.b-cdn.net/bcdn_token=AEykjXGwnQkbAp6xL_NQnA&expires=1735568321&token_path=%2F8f67a5e1-b743-4d3e-9d66-9234bb718ff0%2F/8f67a5e1-b743-4d3e-9d66-9234bb718ff0/480p/video$fCount.ts"
//                withContext(Dispatchers.Main){
//                    if (image!=null && image.isNotEmpty() && image.size>fCount) {
//                        var file=image[fCount]
//                        Log.e("outputfile","${file.second} output ${image}")
//                        binding.ivSeekThumb.setImageBitmap(BitmapFactory.decodeFile(file.second.absolutePath))
//                        binding.ivBack.loadUrl(thumb)
//                    }
//                }


        }
        ivSkipBack.setOnClickListener {
            toShowBackButton()
            playerHandler.seekBackward(10) {
                seekThumb(it, false)
            }
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

    fun seekThumb(seekDuration: Long, isForward: Boolean) {

        lifecycleScope.launch(Dispatchers.IO) {
            if (seekDuration>=10000L) {
                withContext(Dispatchers.Main) {
                    playerHandler.pause()
                }
              launch { captureThumbnail()}.join()
                withContext(Dispatchers.Main) {
                    delay(100)
                    binding.ivSeekThumb.visible()  // Ensure the ImageView is visible
                }
            }else{
                withContext(Dispatchers.Main) {binding.ivSeekThumb.gone()}
            }
//            if (isFraming) {
//                captureThumbnail()
//               withContext(Dispatchers.Main){
//                  binding.ivSeekThumb.visible()  // Ensure the ImageView is visible
//               }
////                var image = getPngFramesFromCache()
////                if (image != null && image.isNotEmpty()) {
////                    var data = frameList.filter { seekDuration <= it.first }
////                        .minByOrNull { it.first }
////                    Log.e("outputfile", "$isForward incomplete list  $seekDuration from list ${data} output ${image}")
////
////                    if (data != null) {
////                        withContext(Dispatchers.Main) {
////                            binding.ivSeekThumb.setImageBitmap(BitmapFactory.decodeFile(data.second.absolutePath))
////                            binding.ivSeekThumb.visible()
////                        }
////                    }
////
////                }
//            }
//            else {
//                if (frameList.isNotEmpty()) {
////
////                    for (i in 0 until frameList.size){
////                        if (frameList[i].first>=seekDuration){
////                            withContext(Dispatchers.Main) {
////                                binding.ivSeekThumb.setImageBitmap(BitmapFactory.decodeFile(frameList[i].second.absolutePath))
////                                binding.ivSeekThumb.visible()
////                            }
////                            Log.e("outputfile", "frame list  $seekDuration from list ${frameList[i]} output ${frameList}")
////                            break
////                        }else{
////                            withContext(Dispatchers.Main) {  binding.ivSeekThumb.gone()}
////                        }
////                    }
//
//                    var data =
//                        frameList.filter { seekDuration <= it.first }.minByOrNull { it.first }
//                    Log.e(
//                        "outputfile",
//                        "$isForward incomplete  $seekDuration from list ${data} output ${frameList}"
//                    )
//
//                    if (data != null) {
//                        withContext(Dispatchers.Main) {
//                            binding.ivSeekThumb.setImageBitmap(BitmapFactory.decodeFile(data.second.absolutePath))
//                            binding.ivSeekThumb.visible()
//                            delay(100)
//                            playerHandler.play()
//                        }
//                    }
//
//                }
//            }

        }
    }

    fun playPauseHandle() = with(binding) {
//        val params = ivPlay.layoutParams as LinearLayout.LayoutParams
//        params.width = resources.getDimensionPixelSize(R.dimen._16sdp)
//        params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
//        ivPlay.layoutParams = params
        visibilityCount = 0
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
        bufferCount = 0
        playerHandler.player?.setVideoSurface(null)
        ivSeekThumb.gone()
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
            tvCurrentLenght.text = ""
            tvCurrentLenght.invalidate()
            tvDuration.text = ""
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

        playerHandler.player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    showProgress()
                    bufferCount++
                } else if (playbackState == Player.STATE_READY) {
                    dismissProgress()
                    //

                    playerHandler.player?.setVideoSurface(videoSurface)
                    homeFragment.isLastPlay = true
                    Log.e("idcheckstr", "isEnded $isEnded  isNextVideoStarted $isNextVideoStarted getLengthOnce $getLengthOnce oldEventId $oldEventId oldMediaId $oldMediaId oldBunnyId $oldBunnyId")
                    videoTranisition()
                    binding.sbVideoSeek.max = 100
                        tvDuration.text = playerHandler.getTotalLength()
                        getLengthOnce = false
                        ivPlay.setImageResource(R.drawable.ic_video_pause)
                    isEnded = false
                    updateProgressBar()

                } else if (playbackState == Player.STATE_ENDED) {
                    Log.e("filteridwith", "video ended ")
                    ivPlay.setImageResource(R.drawable.ic_video_play)
                    isEnded = true
                    playNextVideo()
                    viewFocus()
                }
            }

            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
                Log.d("ExoPlayer", "First frame rendered")
                captureThumbnail()

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
//        texture()
        // Set the Surface to the player

        //  surfaceView = binding.playerView.videoSurfaceView as SurfaceView
       // getFrameFromPlayerView()
    }


    private fun getFrameFromPlayerView() {
//        surfaceTexture = SurfaceTexture(0)
//        surface = Surface(surfaceTexture)
//
//        val playerView = binding.playerView

        //  val surfaceTex = playerView.videoSurfaceView as SurfaceView
         surfaceView = binding.playerView.videoSurfaceView as SurfaceView
         surface = surfaceView?.holder?.surface
        val surfaceHolder = surfaceView?.holder
        surfaceHolder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.e("sknvks", "Surface Created ${holder.surfaceFrame}")
                surfaceWidth= 1920
                surfaceHeight= 1080
                lifecycleScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main){
                        delay(5000)
//                        val bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.ARGB_8888)
//                        val canvas = Canvas(bitmap)
                     //  binding.playerView.videoSurfaceView?.draw(canvas)
                        val bitmap =  captureFrameFromPlayer()

                        //  playerHandler.player?.setVideoSurface(surface)
                    }
                    delay(5000)
                   // setupOpenGL(surface!!)
                    //captureFrame()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.e("sknvks", "Surface Changed: width=$width, height=$height  format $format holder $holder")

                surfaceWidth = width
                surfaceHeight = height

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.e("sknvks", "Surface Destroyed")
                playerHandler.player?.setVideoSurface(null)
            }
        })

    }
    fun captureFrameFromPlayer() {
        surface?.let {
            // Create a bitmap based on the current Surface's width and height
            val width = surfaceView?.width
            val height = surfaceView?.height
            val bitmap = Bitmap.createBitmap(width!!, height!!, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw the surface contents to the bitmap
            canvas.drawColor(Color.BLACK) // Optional: draw a background
            surface?.lockCanvas(null)?.let { canvas ->
                surface?.unlockCanvasAndPost(canvas)
                // Do something with the bitmap
                Log.d("FrameCaptured", "Frame captured!")
            }

            Log.e("VideoFrame", "Frame captured: $bitmap")
            binding.ivSeekThumb.apply {
                setImageBitmap(bitmap)
                visible()
            }

        }
    }
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private var framebufferId: Int = 0
    private var textureId: Int = 0
    private var eglContext: EGLContext? = null
    var surfaceWidth = 640
    var surfaceHeight = 480
    private fun setupOpenGL(surface: Surface) {
        val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

        // Choose an EGL configuration
        val configAttributes = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 24,
            EGL14.EGL_STENCIL_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, configAttributes, 0, configs, 0, 1, numConfigs, 0)
        val eglConfig = configs[0]

        // Create an EGL context
        val contextAttributes = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        eglContext = EGL14.eglCreateContext(
            eglDisplay,
            eglConfig,
            EGL14.EGL_NO_CONTEXT,
            contextAttributes,
            0
        )

        // Create an EGL window surface
        val surfaceAttributes = intArrayOf(EGL14.EGL_NONE)
        val eglSurface = EGL14.eglCreateWindowSurface(
            eglDisplay, eglConfig, surface, surfaceAttributes, 0
        )

        // Make the context current
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

        // Setup OpenGL resources (framebuffers, textures, etc.)
        initializeOpenGLResources()

        Log.d("OpenGL", "OpenGL context is now active.")
    }

    private fun initializeOpenGLResources() {
        // Create framebuffer and texture for rendering
        val framebufferIdArray = IntArray(1)
        GLES20.glGenFramebuffers(1, framebufferIdArray, 0)
        framebufferId = framebufferIdArray[0]

        val textureIdArray = IntArray(1)
        GLES20.glGenTextures(1, textureIdArray, 0)
        textureId = textureIdArray[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, surfaceWidth, surfaceHeight, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            textureId,
            0
        )

        // Check framebuffer completeness
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)

        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            val statusMessage = when (status) {
                GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "Framebuffer Incomplete Attachment"
                GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "Framebuffer Incomplete Missing Attachment"
                GLES20.GL_FRAMEBUFFER_UNSUPPORTED -> "Framebuffer Unsupported"
                else -> "Unknown Framebuffer Status"
            }
            Log.e("OpenGL", "Framebuffer is not complete, status: $status - $statusMessage")
        } else {
            Log.d("OpenGL", "Framebuffer is complete")
        }

//        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
//            Log.e("OpenGL", "Framebuffer is not complete, status: $status")
//        } else {
//            Log.d("OpenGL", "Framebuffer is complete and ready.")
//        }
    }

    private fun captureFrame() {

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId)
        // surfaceTexture?.updateTexImage()

        // Clear the framebuffer
        GLES20.glClearColor(0f, 0f, 0f, 1f) // Clear to black
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Read pixels from the framebuffer
        val buffer = ByteBuffer.allocateDirect(4 * surfaceWidth * surfaceHeight)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        GLES20.glReadPixels(
            0,
            0,
            surfaceWidth,
            surfaceHeight,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            buffer
        )

        buffer.rewind()
        val bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)

        // Optionally flip the bitmap
        val flippedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply {
                postScale(1f, -1f, bitmap.width / 2f, bitmap.height / 2f)
            }, true)
        lifecycleScope.launch(Dispatchers.Main) {

            binding.ivSeekThumb.apply {
                visible()
                setImageBitmap(flippedBitmap)
            }
//            binding.ivPlay.setImageBitmap(flippedBitmap)
            Log.e("OpenGL", "djbcjs bitmap $bitmap")
        }
        // Save or process the Bitmap
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

    private fun extractImageAtTimestamp(timestamp: Long, surfaceView: SurfaceView) {

        val framebufferId = 0
        try {
            // Create a Bitmap to hold the current frame
//            val bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.ARGB_8888)
//            val canvas = Canvas(bitmap)
//            surfaceTexture?.updateTexImage()
//
//            canvas.drawColor(Color.RED)
//            canvas.drawBitmap(bitmap, 0f, 0f, null)


            surfaceTexture?.updateTexImage()

            // Allocate a buffer to store the pixel data from the framebuffer
            val buffer = ByteBuffer.allocateDirect(4 * surfaceWidth * surfaceHeight)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            // Bind the framebuffer to read from
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId)

            // Read pixels from the framebuffer
            GLES20.glReadPixels(
                0,
                0,
                surfaceWidth,
                surfaceHeight,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                buffer
            )

            // Reset the buffer position
            buffer.rewind()

            // Create a bitmap to store the frame
            val bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.ARGB_8888)

            // Copy the pixel data into the bitmap
            bitmap.copyPixelsFromBuffer(buffer)

            lifecycleScope.launch(Dispatchers.Main) {
                Log.e("sknvks", "Frame captured at $timestamp ms")
                binding.ivSeekThumb.apply {
                    setImageBitmap(bitmap)
                    visible()
                }

            }
        } catch (e: Exception) {
            Log.e("sknvks", "Error extracting frame: ${e.message}")
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
//        val session = FFmpegKit.execute(command.joinToString(" "))
//
//        if (!ReturnCode.isSuccess(session.getReturnCode())) {
//            Log.d("FFmpeg", "Command failed. Please check output for the details.")
//            val logs = session.getLogs()
//            logs.forEach { log ->
//                Log.d("FFmpeg Log", log.getMessage())
//            }
//        } else {
//            // Capture the output image from stdout
//            val output = session.getOutput()
//            if (output != null) {
//                // Convert the output byte array to a Bitmap
//                val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(output.toByteArray()))
//                if (bitmap != null) {
//                    Log.d(
//                        "FFmpeg",
//                        "Bitmap dimensions: ${bitmap.width} x ${bitmap.height} bitmap $bitmap"
//                    )
//                    lifecycleScope.launch(Dispatchers.Main) {
//                        binding.ivSeekThumb.setImageBitmap(bitmap)
//                        Log.e("FFmpeg", "Frame extraction successful.")
//                    }
//                } else {
//                    Log.e("FFmpeg", "Bitmap is null")
//                }
//            }
//        }
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


    var timestamp = 10
    private fun extractFrameAtTimestamps() {
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
//                    "-vf", "scale=640:360",
        val frameList = mutableListOf<Bitmap>()

//            val fileArray = mutableListOf<File>()
//          //  for (timestamp in 0..duration step 10) {
        val outputFile = File(requireActivity().cacheDir, "extracted_frame_${timestamp}.png")
        val command = arrayOf(
            "-y",                             // Force overwrite of existing files
            "-ss",
            String.format("00:00:%02d.000", timestamp),  // Timestamp in HH:MM:SS format
            "-i",
            videoUrl,
//                     "-vf", "fps=1/20,scale=640:360",
            "-vf",
            "scale=640:360",
            "-map",
            "0:v:0",                  // Select the first video stream
            "-vframes",
            "1",
            "-an",                            // Disable audio
            "-f",
            "image2pipe",               // Output format: image pipe
            "-pix_fmt",
            "rgba", //"yuvj420p",               // Use RGBA for PNG
            "-vcodec",
            "png",               // Use PNG codec
            "-threads",
            "4",
            "-copyts",                        // Copy timestamps for better performance with HLS
            "-hls_flags",
            "single_file",      // Treat the stream as a single file and avoid resolution switching
            "-hls_time",
            "10",                // Limit the segment duration to 10 seconds (or adjust as needed)
            outputFile.absolutePath          // Write output directly to file
        )
//        timestamp+=10

//        val command = arrayOf(
//            "-y",                             // Force overwrite of existing files
//            "-i", videoUrl,                   // Input video URL (HLS stream)
//          //  "-vf", "fps=1/20,scale=640:360",  // Capture one frame every 'intervalSeconds' and resize
//            "-vf", "scale=640:360",
////            "-map", "0:v:0",                  // Select the first video stream
//            "-an",                            // Disable audio
//            "-f", "image2pipe",               // Output format: image pipe
//            "-pix_fmt", "rgba",               // Use RGBA for PNG
//            "-vcodec", "png",                 // Use PNG codec
//            "-threads", "4",                   // Use 4 threads
//            outputFile.absolutePath
//        )
        // Execute the FFmpeg command asynchronously
//        FFmpegKit.executeAsync(command.joinToString(" ")) { session ->
//            val returnCode = session.returnCode
//            val output = session.output
//
//            if (ReturnCode.isSuccess(returnCode)) {
//                Log.d("FFmpeg", "Command executed successfully")
//
//                // Process the image data from the output stream
//                try {
//                    if (output != null && output.isNotEmpty()) {
//                        val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
//                        Log.d(
//                            "FFmpeg",
//                            "Output size: ${output.length} bytes ${bitmap.width} height ${bitmap.height} bitmap $bitmap "
//                        )
//                        if (bitmap != null) {
//                            lifecycleScope.launch(Dispatchers.Main) {
//                                binding.ivSeekThumb.setImageBitmap(bitmap)  // Set the Bitmap to the ImageView
//                                Log.d("FFmpeg", "Image displayed successfully.")
//                            }
//                        } else {
//                            Log.e("FFmpeg", "Failed to decode Bitmap from file.")
//                        }
//                    } else {
//                        Log.e("FFmpeg", "Output is null or empty")
//                    }
//                } catch (e: Exception) {
//                    Log.e("FFmpeg", "Error processing frames", e)
//                }
//            } else {
//                Log.e("FFmpeg", "Command execution failed with error: ${session.returnCode}")
//            }
//            Log.e("FFmpeg", "bitmap images $frameList")
//        }


//                val session = FFmpegKit.execute(command.joinToString(" "))
//
//                if (!ReturnCode.isSuccess(session.returnCode)) {
//                    val logs = session.logs
//                    logs.forEach { log ->
//                        Log.d("FFmpeg Log", log.message)
//                    }
//                }
//                else {
//                    val output = session.output
//                    if (output != null && output.isNotEmpty()) {
//                        val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
//                        Log.d(
//                            "FFmpeg",
//                            "Output size: ${output.length} bytes ${bitmap.width} height ${bitmap.height} bitmap $bitmap "
//                        )
//                        if (bitmap != null) {
//                            lifecycleScope.launch(Dispatchers.Main) {
//                                binding.ivSeekThumb.setImageBitmap(bitmap)  // Set the Bitmap to the ImageView
//                                Log.d("FFmpeg", "Image displayed successfully.")
//                            }
//                        } else {
//                            Log.e("FFmpeg", "Failed to decode Bitmap from file.")
//                        }
//                    } else {
//                        Log.e("FFmpeg", "Output is null or empty")
//                    }
//             //   }
//           // }
//            Log.d("FFmpeg", "Total frames extracted: ${fileArray.size}")
//
//        }
    }

    var bitmaplist = ArrayList<File>()

    // Function to extract frames frame by frame from a video stream
    fun extractFrameAtTimestamp() {
        var duration = playerHandler.player?.duration!! / 1000
        Log.e("sjbchd", "sknsk buffer count $bufferCount duration $duration ")

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
//        for (timestamp in 10..duration step 10) {
            val cacheDir = File(requireActivity().cacheDir, "frames")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            } else {
                val files = cacheDir.listFiles()
                files?.forEach { it.delete() }
            }
            while (timestamp <= duration) {
                val timeString = convertSecondsToTimeFormat(timestamp)
//                val command = arrayOf(
//                    "-y",
//                    "-ss",timeString,
//                    "-i",
//                    videoUrl,
//                    "-vf",
//                    "scale=640:360",
//                    "-map",
//                    "0:v:0",
//                    "-vframes",
//                    "1",
//                    "-an",
//                    "-f",
//                    "image2pipe",
//                    "-pix_fmt",
//                    "rgba", //"yuvj420p",
//                    "-vcodec",
//                    "png",
//                    "-q:v", "20",
//                    "-threads",
//                    "4",
//                    "-copyts",
//                    "-hls_flags",
//                    "single_file",
////                    "-hls_time",
////                    "10",
//                    outputFile.absolutePath
//                )


                val outputFile = File(cacheDir, "${timestamp}.png")

                val command = arrayOf(
                    "-y",
                    "-i",
                    videoUrl,                    // Input video
                    "-vf",
                    "fps=1/10,scale=640:360",   // Extract frame every 10 seconds
                    "-map",
                    "0:v:0",                   // Select the video stream
                    "-an",                             // Disable audio
                    "-q:v",
                    "20",
                    "-threads",
                    "4",                   // Use 4 threads for faster processing
                    "-vsync",
                    "1",                    // Ensures the frames are extracted in sync with the video
                    "-strftime",
                    "1", // Enable using strftime formatting in output filename
                    outputFile.absolutePath
                )

//                FFmpegKit.executeAsync(command.joinToString(" ")) { session ->
//                    val returnCode = session.returnCode
//
//                    if (ReturnCode.isSuccess(returnCode)) {
//                        try {
//
//                            Log.e("FFmpeg", "successfully  ")
////                                bitmaplist.add(outputFile)
////                                lifecycleScope.launch(Dispatchers.Main) {
////                                    binding.ivSeekThumb.setImageBitmap(BitmapFactory.decodeFile(outputFile.absolutePath))  // Set the Bitmap to the ImageView
////                                    Log.d("FFmpeg", "Image displayed successfully.")
////                                }
//                        } catch (e: Exception) {
//                            Log.e("FFmpeg", "Error processing frame", e)
//                        }
//                    } else {
//                        Log.e("FFmpeg", "Failed to extract frame at timestamp $timestamp")
//
//                    }
//                    Log.e("FFmpeg", "${bitmaplist.size} bitmap lsit $bitmaplist")
//                }
                timestamp += 10
            }
        }
    }

    fun convertSecondsToTimeFormat(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    /// each frames
    fun extractFramesToCache() {

        lifecycleScope.launch(Dispatchers.IO) {
            isFraming = true
            val cacheDir = File(requireActivity().cacheDir, "frames")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            } else {
                val files = cacheDir.listFiles()
                files?.forEach { it.delete() }
            }
            val command = arrayOf(
                "-y",
                "-i",
                videoUrl,                    // Input video
                "-vf",
                "fps=1/8,scale=640:360",   // Extract frame every 10 seconds
                "-map",
                "0:v:0",                   // Select the video stream
                "-an",                             // Disable audio
                "-q:v", "10",
                "-threads",
                "8",                   // Use 4 threads for faster processing
                "-vsync",
                "1",                    // Ensures the frames are extracted in sync with the video
                "-sws_flags", "fast_bilinear",     // Use faster scaling method
                "-hwaccel", "cuda",  // Enable CUDA acceleration
                "-hwaccel_output_format", "cuda",
                        // "-strftime", "1", // Enable using strftime formatting in output filename
                File(cacheDir, "frame_%03d.png").absolutePath           // Output path
//                File(cacheDir, "frame_%03d.png").absolutePath           // Output path
            )
            val commandString = command.joinToString(" ")
            FFmpegKit.executeAsync(commandString) { session ->
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)) {
                    Log.d("FFmpeg", "Frame extracted successfully.")
                    getPngFramesFromCache()
                    isFraming = false
                } else {
                    isFraming = false
                    Log.e("FFmpeg", "Error during frame extraction.")
                }
            }
        }
    }

    var isFraming = true
    var frameList = ArrayList<Pair<Long, File>>()
    fun getPngFramesFromCache(): ArrayList<Pair<Long, File>>? {
        val cacheDir = File(requireActivity().cacheDir, "frames")
        if (!cacheDir.exists()) {
            Log.e("Cachedirec", "Cache directory does not exist.")
            return null
        }
        val pngFiles = cacheDir.listFiles { _, name ->
            Log.e("Cachedirec", "file name $name")
            name.endsWith(".png", ignoreCase = true)
        }

        Log.d("PNG Files", "Number of frames: ${pngFiles?.size}")
        if (pngFiles == null || pngFiles.isEmpty()) {
            Log.e("Cachedirec", "No PNG files found in the cache directory.")
            return null
        }
        val sortedFiles = pngFiles.sortedBy { file ->
            file.nameWithoutExtension.split("_")[1].toInt()
        }

//        var sordetList = ArrayList<Pair<Long, File>>()
        val ascendingOrderList = sortedFiles.sortedDescending().sorted()
        ascendingOrderList.forEachIndexed { index, file ->
            var seek = (index + 1) * 8000L
            frameList.add(Pair(seek, file))
        }

        return frameList //sortedFiles.sortedDescending().sorted()
    }

    fun extractFrameFromHLS(timeInMs: Long): Bitmap? {

        val cacheDir = File(requireActivity().cacheDir, "frames")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        } else {
            val files = cacheDir.listFiles()
            files?.forEach { it.delete() }
        }
        var file = File(cacheDir, "frame_%03d.png")
        val timeInSeconds = timeInMs / 1000.0
        val command =
            "-ss $timeInSeconds -i $videoUrl -vf \"scale=640:360\" -vframes 1 ${file.absolutePath}"

        val session = FFmpegKit.execute(command)

        if (session.returnCode.isSuccess) {
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.absolutePath)
            }
        } else {
            // Log the error message if FFmpegKit failed
            val errorMessage = session.allLogs.joinToString("\n")
            Log.e("FFmpegKitError", "Error: $errorMessage")
        }

        return null

    }
    private fun captureThumbnail() {
        try {
            lifecycleScope.launch(Dispatchers.Main) {
                val textureView = binding.textureView
                if (textureView.isAvailable) {
                    val width = textureView.width
                    val height = textureView.height
                    launch(Dispatchers.IO) {
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        withContext(Dispatchers.Main) {
                            textureView.getBitmap(bitmap)
                            binding.ivSeekThumb.apply {
                                setImageBitmap(bitmap)
                                delay(200)
                                playerHandler.play()
                                visibilityCount=0
                                playerHandler.stopHandler()
                                updateProgressBar()
                            }
                        }
                    }
                } else {
                    Log.e("ExoPlayer", "TextureView is not available.")
                }
            }
        } catch (e: Exception) {
            Log.e("ExoPlayer", "Error capturing thumbnail", e)
        }
    }

    var videoSurface:Surface?=null

    private fun texture()= with(binding){
        val textureView = binding.textureView
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                videoSurface=Surface(surface)
                playerHandler.player?.setVideoSurface(videoSurface)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.e("kdsncdnc","smclsn destroy")
                playerHandler.player?.setVideoSurface(null)
             return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }
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
                seekThumb(count, true)
            } else {
                playerHandler.seekTo(duration)
                seekThumb(duration, true)
            }
            playerHandler.pause()
        }

    }

    fun forward10() {
        if (::playerHandler.isInitialized) {
            var current = playerHandler.player?.currentPosition
            var duration = playerHandler.player?.duration
            var count = current!! + 10000
            if (duration!! > count) {
                playerHandler.seekTo(count)
                seekThumb(count, true)
            } else {
                playerHandler.seekTo(duration)
                seekThumb(duration, true)
            }
            playerHandler.pause()
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

            if (visibilityCount == 5) {
                visibilityCount = 0
                binding.ivBack.animate().alpha(0f).setDuration(400).setStartDelay(10)
                binding.llTools.animate().alpha(0f).setDuration(400).setStartDelay(10)
                binding.playerView.requestFocus()
                binding.clSettingsMenu.gone()
                binding.ivSeekThumb.invisible()
            }
            visibilityCount++
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

                        } else {

                            if (time >= 1L) {
                                binding.tvRemains.setText("Playing Next Video in $time s")
                            }
                        }
                        time--
                    } else if (binding.timerLayout.isVisible) {
                        binding.timerLayout.gone()
                        time = 0
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
            //  Log.e("mremote", "kckdnc $keyCode event $event")
            if (event.action == KeyEvent.ACTION_DOWN) {
                visibilityCount=0
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        toShowBackButton()
                        viewFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        toShowBackButton()
                        viewFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        toShowBackButton()
                        viewFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        toShowBackButton()
                        viewFocus()
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
                        playerHandler.seekBackward(10) {}
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

                    KeyEvent.KEYCODE_DPAD_CENTER -> {

                        Log.e("mremote", "Enter button pressed")
                        toShowBackButton()
                        viewFocus()
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