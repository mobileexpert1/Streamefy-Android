package com.streamefy.component.ui.video

import VolumeManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.streamefy.network.MyResource
import com.streamefy.utils.convertToMillis
import com.streamefy.utils.gone
import com.streamefy.utils.invisible
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showMessage
import com.streamefy.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.floor


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
    var videoUrl = ""
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
    var playFirst = true

    companion object {
        lateinit var videoFragment: VideoFragment
    }

    private val viewModel: VideoVM by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoFragment = this
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

        handleKey(binding.playerView)
        volumeManager = VolumeManager(requireActivity())
        volumeManager.setVolumePercentage(5)

        binding.apply {
            ivVideoThumb.loadUrl(thumbnailS3bucketId)
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

    fun keyMove() = with(binding) {
        sbVideoSeek.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            toShowBackButton()
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

                }
            }
            false // Don't consume other events
        })
        ivSkipBack.remoteKey {
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    ivRefresh.requestFocus()
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
                    ivPlay.requestFocus()
                    visibilityCount = 0
                }

                else -> {}
            }
        }

        ivPlay.remoteKey {
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
            if (playerHandler.player != null) {
                if (playerHandler.isPlaying()!!) {
                    playerHandler.pause()
                    ivPlay.setImageResource(R.drawable.ic_seleceted_play)
                } else {
                    if (isEnded) {
                        playerHandler.player?.seekTo(0)
                    } else {
                        playerHandler.play()
                    }
                    ivPlay.setImageResource(R.drawable.ic_selected_pause)
                    updateProgressBar()
                }
            }
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
            playbackduration=0
            newVideo()
            focusView = VideoEnum.BACKWARD
        } else {
            findNavController().popBackStack()
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

    fun listener() = with(binding) {
        showProgress()
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
            lifecycleScope.launch(Dispatchers.Main) {
                volumeCount = volumePercentage
                toShowBackButton()
                sbVolumeSeek.setProgress(volumeCount)
            }
            if (ivVolume.requestFocus()) {
                if (volumePercentage <= 0) {
                    // ivVolume.requestFocus()
                    ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                } else {
                    ivVolume.setImageResource(R.drawable.ic_selected_volume)
                }
            } else {
                if (volumePercentage <= 0) {
                    //  ivVolume.requestFocus()
                    ivVolume.setImageResource(R.drawable.ic_mute)
                } else {
                    ivVolume.setImageResource(R.drawable.ic_video_volume)
                }
            }
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
            binding.tvCurrentLenght.setText(playerHandler.getcurrent().toString())

            if (playerHandler.isPlaying()!!) {
                playerHandler.handler.postDelayed({ updateProgressBar() }, 1000)
            }
            visibilityCount++
            if (visibilityCount == 15) {
                visibilityCount = 0
                binding.ivBack.animate().alpha(0f).setDuration(1200).setStartDelay(10)
                binding.llTools.animate().alpha(0f).setDuration(1200).setStartDelay(10)
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

            if (event.action == KeyEvent.ACTION_DOWN) {
                Log.e("sncjdnvjd", "sncksdnc handling focus $event")
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
        }
        requireActivity().window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onStop()
    }

    override fun onDestroy() {

        // homeFragment.viewFocus()
        super.onDestroy()

    }

    override fun onResume() {
        super.onResume()
        binding.playerView.onResume()
        if (playerHandler.player != null) {
            playerHandler.play()
            updateProgressBar()
        }
        isOpenSettingFirst = false
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