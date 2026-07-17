package com.streamefy.component.ui.video

import VolumeManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.Surface
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
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.util.MimeTypes
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
import com.streamefy.utils.videoSeekKey
import com.streamefy.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.StringTokenizer


class VideoFragment : BaseFragment<FragmentVideoBinding>() {
    override fun bindView(): Int = R.layout.fragment_video
    lateinit var playerHandler: PlayerHandler
    var getLengthOnce = true
    var isEnded = false
    var visibilityCount = 0
    var volumeCount = 0
    var isOpenSettingFirst = false
    var playbackduration: Long = 0
    var thumbnailS3bucketId = ""
    private lateinit var volumeManager: VolumeManager
    lateinit var qualityAdapter: QualityAdapter
    var qualityList = ArrayList<QualityModel>()
    var bunneyIdList = ArrayList<BunneyIds>()
    var videoUrl = ""
    var ifFirst = true
    var nextVideoId = ""
    var mediaId = 0
    var eventId = 0
    var oldEventId = 0
    var oldBunnyId = ""
    var oldVideoDuration: Long = 0
    var videoThumb = ""
    var isNextVideoStarted = false
    var phone = ""
    var videoCount = 0
    var isVolume = false
    var bufferCount = 0
    var currentDuration = 0L
    var isSeeking=true
    var isScreenVisible = false

    private val handler = Handler(Looper.getMainLooper())
    private var hideToolsRunnable: Runnable? = null

    private fun showToolsAndAutoHide() {
        // Cancel any previously posted hide runnable
        hideToolsRunnable?.let { handler.removeCallbacks(it) }

        // Animate llTools to visible
        binding.llTools.animate()
            .alpha(1f)
            .setDuration(300)
            .withStartAction {
                binding.llTools.alpha = 0f
                binding.llTools.visibility = View.VISIBLE
                binding.ivBack.visible()
            }
            .withEndAction {
                // Start auto-hide countdown
                hideToolsRunnable = Runnable {
                    binding.llTools.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.llTools.visibility = View.GONE
                            binding.ivBack.visibility = View.GONE
                            binding.clSettingsMenu.visibility = View.GONE
                            binding.ivSeekThumb.visibility = View.GONE
                        }
                }
                handler.postDelayed(hideToolsRunnable!!, 6000)
            }
    }

    companion object {
        lateinit var videoFragment: VideoFragment
        var resumeAfterRenderSeek = 0L
    }

    private val viewModel: VideoVM by viewModel()
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resumeAfterRenderSeek = 0L
        videoFragment = this
        isVolume = true
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        arguments?.run {
            thumbnailS3bucketId = getString(PrefConstent.VIDEO_THUMB).toString()
            val newDuration = getString(PrefConstent.PLAY_BACK_DURATION).toString()

            nextVideoId = getString(PrefConstent.VIDEO_ID).toString()
            if (getString(PrefConstent.MEDIA_ID).toString().isNotEmpty()) {
                mediaId = getString(PrefConstent.MEDIA_ID).toString().toInt()
            }
            oldBunnyId = nextVideoId
            bunneyIdList.clear()

            playbackduration = newDuration.toDouble().toInt().toString().toLong()

        }

        handleKey(binding.playerView)
        volumeManager = VolumeManager(requireActivity())
        Log.e("call","#### VOLUME COUNT:::   "+getSavedSeekBarProgress())

        binding.apply {
            updatePlayer()
            thumbShow()
            newVideo()
            clickme()
            listener()
            keyMove()
            videoSeekListener()
            texture()

            handleCenterButton()
        }
        volume()
        selectorFocus()

        volumeCount = getSavedSeekBarProgress()
        binding.sbVolumeSeek.progress = volumeCount

        if (volumeCount >= 1) {
            val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumePercent = (currentVolume * 100) / maxVolume
            volumeManager.setVolumePercentage(volumePercent)
        }

//        if (volumeCount == 0){
//            binding.ivVolume.setImageResource(R.drawable.ic_mute)
//        }
    }

    fun handleCenterButton() {
        binding.playerView.isFocusableInTouchMode = true
        binding.playerView.requestFocus()
        binding.playerView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        if (binding.llTools.alpha == 1f) {
                            togglePlayPause()
                        } else {
                            showToolsAndAutoHide()
                        }
                        true // Consume event
                    }

                    KeyEvent.KEYCODE_DPAD_UP -> {
                        // Handle DPAD UP key press
                        Log.d("RemoteControl", "DPAD_UP key pressed")
                        if (binding.llTools.alpha == 1f) {
                            binding.ivSkipBack.requestFocus()
                        }else {
                            showToolsAndAutoHide()
                        }
                        // Add your logic here
                        true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // Handle DPAD UP key press
                        Log.d("RemoteControl", "DPAD_KEY key pressed")
                        if (binding.llTools.alpha == 1f) {
                            binding.ivSkipBack.clearFocus()
                            binding.ivSkipBack.requestFocus()
                        }else {
                            showToolsAndAutoHide()
                        }
                        // Add your logic here
                        true
                    }

                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        // Handle DPAD UP key press
                        Log.d("RemoteControl", "DPAD_LEFT key pressed")
                        if (binding.llTools.alpha == 1f) {
                            binding.sbVideoSeek.requestFocus()
                        }else {
                            showToolsAndAutoHide()
                        }
                        true
                    }

                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        // Handle DPAD UP key press
                        Log.d("RemoteControl", "DPAD_RIGHT key pressed")
                        if (binding.llTools.alpha == 1f) {
                            binding.sbVideoSeek.requestFocus()
                        }else {
                            showToolsAndAutoHide()
                        }
                        // Add your logic here
                        true
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                    KeyEvent.KEYCODE_MEDIA_PLAY,
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                        playerHandler.player?.let {
                            if (it.isPlaying) {
                                it.pause()
                                updateProgressBar()
                            } else {
                                it.play()
                                updateProgressBar()
                            }
                        }
                        true // consume event
                    }

                    else -> false
                }
            } else {
                false
            }
        }
    }



    private fun togglePlayPause() {
        playerHandler.player?.let {
            if (it.isPlaying) {
                it.pause()
                binding.ivPlay.requestFocus()
                binding.ivPlay.setImageResource(R.drawable.ic_seleceted_play)
                updateProgressBar()
            } else {
                it.play()
                binding.ivPlay.requestFocus()
                binding.ivPlay.setImageResource(R.drawable.ic_selected_pause)
                updateProgressBar()
            }
        }
    }



    //     Key movement for all buttons
    private fun keyMove() = with(binding) {
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
//                        forward()
                        if (isSeeking){
                            if (playerHandler.player!=null){
                                currentDuration=playerHandler.getCurrentPosition()
                                isSeeking=false
                            }
                        }

                        //** previous code
                        /*
                          val count = currentDuration + 30000
                          fastBackward(count)
                         */

                        var totalLength =  convertToMillis(playerHandler.getTotalLength())
                        if (currentDuration < totalLength)  {
                            val count = currentDuration + 30000
                            fastBackward(count)
                        }else {
                            tvDuration.text = playerHandler.currentDuration(0L)
                            tvCurrentLenght.text = playerHandler.getTotalLength()
                            ivSeekThumb.gone()
                        }
                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
//                        playerHandler.seekBackward(30) {
//                            seekThumb(it, false)
//                        }
                        if (isSeeking){
                            if (playerHandler.player!=null){
                                currentDuration=playerHandler.getCurrentPosition()
                                isSeeking=false
                            }
                        }
                        val count = currentDuration - 30000
                        fastBackward(count)
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
            else if (event.action==KeyEvent.ACTION_UP){
                when(keyCode){
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD->{
                        mediaKey(StreamEnum.REMOVE_LONG_PRESS)
                    }
                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
                        mediaKey(StreamEnum.REMOVE_LONG_PRESS)
                    }
                    KeyEvent.KEYCODE_DPAD_UP ->{
                        fastFBshow()
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN->{
                        fastFBshow()
                    }
                }
            }
            false // Don't consume other events
        })
        sbVolumeSeek.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            Log.e("call","### 111")
//            binding.ivBack.animate().alpha(1f).setDuration(50).setStartDelay(50)
//            binding.llTools.animate().alpha(1f).setDuration(50).setStartDelay(50)
            showToolsAndAutoHide()
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

                        // for firestick
                        if (isAmazonFireTv() || isGoogleTv()){
                            Log.e("call","### AmazonFirestick -- Google stick")
                            binding.sbVolumeSeek.progress = volumeCount
                            increaseSystemVolume()
                        }else {
                            // this case is for tv
                        }

                        if (volumeCount <= 0) {
                            playerHandler.mute()
                            ivVolume.setImageResource(R.drawable.ic_mute)
                        } else {
                            ivVolume.setImageResource(R.drawable.ic_video_volume)
                        }

                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        isVolume = false
                        volumeDown()

                        // for firestick
                        if (isAmazonFireTv() || isGoogleTv()){
                            Log.e("call","AmazonFireTv or isGoogleTv")
                            binding.sbVolumeSeek.progress = volumeCount
                            decreaseSystemVolume()
                        }else {
                            // this case is for tv
                            Log.e("call","not AmazonFireTv not GoogleTv")
                        }

                        if (volumeCount <= 0) {
                            playerHandler.mute()
                            ivVolume.setImageResource(R.drawable.ic_mute)
                        } else {
                            ivVolume.setImageResource(R.drawable.ic_video_volume)
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
            else if (event.action==KeyEvent.ACTION_UP){
                when(keyCode){
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD->{
                        mediaKey(StreamEnum.REMOVE_LONG_PRESS)
                    }
                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
                        mediaKey(StreamEnum.REMOVE_LONG_PRESS)
                    }
                }
            }
            false
        })
        ivSkipBack.videoSeekKey {
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

                StreamEnum.KEYCODE_DPAD_CENTER -> {
                    Log.e("longpress", "backward $currentDuration")
                    if (isSeeking){
                        if (playerHandler.player!=null){
                            currentDuration=playerHandler.getCurrentPosition()
                            isSeeking=false
                        }
                    }
                    val count = (currentDuration - 10000)
                    fastBackward(count)
                }
                StreamEnum.REMOVE_LONG_PRESS -> {
                    Log.e("longpress", "backward center removed $currentDuration")
                    fastFBshow()
                }
                else -> {}
            }
        }

        ivPlay.remoteKey {
            mediaKey(it)
            visibilityCount = 0
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    ivSkipBack.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    ivSkipForward.requestFocus()
                }

                else -> {}
            }
        }
        ivSkipForward.videoSeekKey {
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

                StreamEnum.KEYCODE_DPAD_CENTER -> {
                    // sbVideoSeek.requestFocus()
                    Log.e("longpress", "long $currentDuration")
                    if (isSeeking){
                        if (playerHandler.player!=null){
                            currentDuration=playerHandler.getCurrentPosition()
                            isSeeking=false
                        }
                    }

                    //** previous code
                    /*
                     val count = currentDuration + 10000
                    fastForward(count)
                     */

                    var totalLength =  convertToMillis(playerHandler.getTotalLength())
                    if (currentDuration < totalLength)  {
                        val count = currentDuration + 10000
                        fastBackward(count)
                    }else {
                        tvDuration.text = playerHandler.currentDuration(0L)
                        tvCurrentLenght.text = playerHandler.getTotalLength()
                        ivSeekThumb.gone()
                    }



//                    val duration = playerHandler.getDuration()
//                    val count = currentDuration + 10000
//                    val progress = (count * 100 / duration.toDouble()).toInt()
//                    sbVideoSeek.progress = progress
//                    currentDuration = count
//                    playerHandler.pause()
//                    binding.apply {
//                        tvDuration.text = playerHandler.getRemainsDuration(currentDuration)
//                        tvCurrentLenght.text = playerHandler.currentDuration(currentDuration).toString()
//                        ivSeekThumb.gone()
//                    }

                }
                StreamEnum.REMOVE_LONG_PRESS -> {
                    Log.e("longpress", "center removed $currentDuration")
                    val duration = playerHandler.getDuration()
                    if (duration <= currentDuration) {
                        currentDuration=duration
                    }
                    fastFBshow()
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

    fun fastForward(count: Long) = with(binding){
        Log.e("longpress", "long $currentDuration")
        val duration = playerHandler.getDuration()
        val progress = (count * 100 / duration.toDouble()).toInt()
        sbVideoSeek.progress = progress
        currentDuration = count
        playerHandler.pause()
        binding.apply {
            tvDuration.text = playerHandler.getRemainsDuration(currentDuration)
            tvCurrentLenght.text = playerHandler.currentDuration(currentDuration).toString()
            ivSeekThumb.gone()
        }
    }
    fun fastBackward(count: Long) = with(binding){
        val duration = playerHandler.getDuration()
        currentDuration = if (count>10000) {
            count
        }else{ 0 }
        val progress = (count * 100 / duration.toDouble()).toInt()
        sbVideoSeek.progress = progress
        playerHandler.pause()
        binding.apply {
            tvDuration.text = playerHandler.getRemainsDuration(currentDuration)
            tvCurrentLenght.text = playerHandler.currentDuration(currentDuration).toString()
            ivSeekThumb.gone()
        }

    }

    fun fastFBshow(){
        isSeeking=true
        playerHandler.seekTo(currentDuration)
        lifecycleScope.launch(Dispatchers.IO) {
            seekThumb(currentDuration, true)
        }
    }
    //    Handle video thumb transition and listener of video seekbar
    private fun videoSeekListener() = with(binding) {
        sbVideoSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val xPosition: Float = seekBar.width * progress / 100.0f
                ivSeekThumb.translationX = xPosition - ivSeekThumb.width / 2
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    //    Handle remote forward, backward and play/pause key
    private fun mediaKey(streamEnum: StreamEnum) = with(binding) {
        Log.e("mremote","closed by $streamEnum")

        when (streamEnum) {
            StreamEnum.KEYCODE_MEDIA_FAST_FORWARD -> {
//                forward10()
                toShowBackButton()
                if (isSeeking){
                    if (playerHandler.player!=null){
                        currentDuration=playerHandler.getCurrentPosition()
                        isSeeking=false
                    }
                }
                val count = currentDuration + 10000
                fastForward(count)
                binding.apply {
                    ivMedia.setImageResource(R.drawable.ic_remote_forward)
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
            }

            StreamEnum.KEYCODE_MEDIA_REWIND -> {
//                playerHandler.seekBackward(10) {
//                    seekThumb(it, false)
//                }
                toShowBackButton()
                if (isSeeking){
                    if (playerHandler.player!=null){
                        currentDuration=playerHandler.getCurrentPosition()
                        isSeeking=false
                    }
                }
                val count = currentDuration - 10000
                fastBackward(count)

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
            }

            StreamEnum.KEYCODE_MEDIA_PLAY_PAUSE -> {
                playPauseHandle()
            }
            StreamEnum.REMOVE_LONG_PRESS -> {
                fastFBshow()
            }

            else -> {}
        }
    }

    private fun updatePlayer() = with(binding) {
        playerHandler = PlayerHandler(requireActivity(), playerView)
    }

    //      Get new video from server
    private fun newVideo() {
        viewModel.getVideo(requireContext(), nextVideoId)
        observe()
    }

    //     Handle video response
    private fun observe() {
        viewModel.videoLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isSuccess -> {
                    val data = it.data?.response
                    if (data != null) {
                        dismissProgress()
                        videoUrl = data.hlsUrl
                        oldEventId = eventId
                        eventId = data.eventId
                        videoCount++
                        mediaId = data.mediaId
                        oldVideoDuration = playbackduration
                        ifFirst = false

                        //playerHandler.setMediaUri(videoUrl, playbackduration,data.vttFileContent)
                        playerHandler.setMediaUri(videoUrl, oldVideoDuration, true)
//                        playerHandler.setMediaUri(videoUrl, 0)
                        if (data.nextVideo != null) {
                            isNewVideoAvailable = true
                            thumbnailS3bucketId = videoThumb
                            videoThumb = data.nextVideo.nextVideoThumbnail
                            oldBunnyId = nextVideoId
                            nextVideoId = data.nextVideo.nextVideoId.toString()
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

                    } else {
                        isNewVideoAvailable = false
                        requireActivity().showMessage("Video not found")
                    }
                }

                else -> {}
            }
        }
    }

    private var isNewVideoAvailable = false

    //    Handle all buttons clicks
    private fun clickme() = with(binding) {
        ivBack.setOnClickListener { findNavController().popBackStack() }
        ivPlay.setOnClickListener {
            toShowBackButton()
            val params = ivPlay.layoutParams as LinearLayout.LayoutParams
            params.width =
                resources.getDimensionPixelSize(R.dimen._16sdp)
            params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
            ivPlay.layoutParams = params
            playPauseHandle()
        }

        ivSkipForward.setOnClickListener {
            toShowBackButton()
        }


        ivSkipBack.setOnClickListener {
            toShowBackButton()
        }
        ivRefresh.setOnClickListener {
            if (playerHandler.player != null) {
                playerHandler.refresh()
                getLengthOnce = true
                timerLayout.invisible()
                ivPlay.setImageResource(R.drawable.ic_video_pause)
                binding.ivSeekThumb.visibility = View.GONE
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
                    sbVolumeSeek.progress = volumeCount
                    volumeManager.setVolumePercentage(1)
                } else {
                    playerHandler.mute()
                    ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                    sbVolumeSeek.progress = 0
                    volumeManager.setVolumePercentage(0)
                    volumeCount = 0
                }
            }
        }
    }

    //    Show thumb while seeking the video
    private fun seekThumb(seekDuration: Long, isForward: Boolean) {

        lifecycleScope.launch(Dispatchers.IO) {
            if (seekDuration >= 10000L) {
                withContext(Dispatchers.Main) {
                    playerHandler.pause()
                }
                launch { captureThumbnail() }.join()
                withContext(Dispatchers.Main) {
                    //  delay(100)
                    binding.ivSeekThumb.visible()  // Ensure the ImageView is visible
                }
            } else {
                withContext(Dispatchers.Main) { binding.ivSeekThumb.gone() }
            }
        }
    }

    //    Handle play/pause functionality
    fun playPauseHandle() = with(binding) {
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
                    playerHandler.player?.seekTo(currentDuration)

//                    playerHandler.play()
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

    //    Handle next video play functionality
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
            playbackduration = 0
            newVideo()
            focusView = VideoEnum.BACKWARD
        } else {
            findNavController().navigateUp()
        }

    }

    //    Reset all view after ended the video
    private fun resetView() = with(binding) {
        if (playerHandler.player != null) {
            playerHandler.player?.stop()
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
    }


    override fun onStart() {
        super.onStart()

        isScreenVisible = false
    }

    //    Video listener
    private fun listener() = with(binding) {
        showProgress()

        playerHandler.player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {

                        if (isScreenVisible == false){
                            binding.textureView.visibility = View.INVISIBLE
                            playerHandler.player?.playWhenReady = false
                            isScreenVisible = true
                        }

                        playerHandler.playerMute()

                        lifecycleScope.launch(Dispatchers.Main) {
                            // show loader on main thread
                            showProgress()
                        }

                        bufferCount++
                    }
                    Player.STATE_READY -> {
                        // Mute the audio by setting the volume to 0
                        // playerHandler.player?.volume = 0f
                        Log.e("call","### READYYY")

                        playerHandler.player?.setVideoSurface(videoSurface)
                        homeFragment.isLastPlay = true
                        videoTranisition()
                        sbVideoSeek.max = 100
                        tvDuration.text = playerHandler.getTotalLength()
                        getLengthOnce = false
                        ivPlay.setImageResource(R.drawable.ic_video_pause)
                        isEnded = false


                        lifecycleScope.launch {
                            Log.e("buffercnt","buffers $bufferCount")
//                            if (bufferCount<=1){
//                                delay(7000)
//                            }
                            // Show the video frame again when starting playback
                            binding.textureView.visibility = View.VISIBLE
                            playerHandler.player?.playWhenReady = true

                            updateProgressBar()
                            updateResulation()
                            dismissProgress()
                            playerHandler.playerUnMute()
                        }

                        if (playerHandler.player?.isPlaying!!){
                            Log.e("buffercnt","videostatus ${playerHandler.player?.isPlaying}")
                        }
                    }
                    Player.STATE_ENDED -> {
                        ivPlay.setImageResource(R.drawable.ic_video_play)
                        isEnded = true
                        playNextVideo()
                        viewFocus()
                    }

                    Player.STATE_IDLE -> {

                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log.e("buffercnt","isPlaying $isPlaying")
//                lifecycleScope.launch {
//                    Log.e("buffercnt","buffers $bufferCount")
//                    if (bufferCount<=1){
//                        delay(5000)
//                    }
//                    updateProgressBar()
//                    updateResulation()
//                    dismissProgress()
//                }
            }

            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
              //  captureThumbnail()
                lifecycleScope.launch {
                    // Only seek if resume duration is valid
                    if (resumeAfterRenderSeek > 0) {
                        playerHandler.seekTo(resumeAfterRenderSeek)
                       resumeAfterRenderSeek = 0L
                    }

                    //  Play now
                    playerHandler.player?.playWhenReady = true
                    playerHandler.playerUnMute()
                    binding.textureView.visibility = View.VISIBLE
                    updateProgressBar()
                    updateResulation()
//                    dismissProgress()
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
//                Get all video resolution
                if (!isOpenSettingFirst) {
                    isOpenSettingFirst = true
                    qualityList.clear()

                    lifecycleScope.launch(Dispatchers.IO) {
                        val data =
                            QualityModel(
                                "Auto", false,
                                480,
                                854
                            )
                        qualityList.add(data)
                        for (group in tracks.getGroups()) {
                            Log.e("ExoPlayersu", "code : ${group.getType()}")
                            if (group.getType() == C.TRACK_TYPE_VIDEO) {
                                // Log.d("ExoPlayer", "Current resolution playing: $group")

                                val trackCount = group.length
                                for (j in 0 until trackCount) {
                                    val format = group.getTrackFormat(j)
                                    val isSelected = group.isTrackSelected(j)

                                    Log.d("ExoPlayer", "All resolution playing: $isSelected \nformat $format\n")
                                    // Check if the format is a video format using supported properties
                                    if (format.width > 0 && format.height > 0) {
                                        val mwidth = format.width
                                        val mheight = format.height
                                        val isSelected = false

                                        qualityList.add(
                                            QualityModel(
                                                title = mheight.toString() + "p",
                                                height = mheight,
                                                width = mwidth
                                            )
                                        )

                                        val isSonyTVAndroid11 = Build.VERSION.SDK_INT == Build.VERSION_CODES.R && // Android 11
                                                Build.MANUFACTURER.equals("Sony", ignoreCase = true)

                                        // implement check for if android version then 11
                                        val sdkInt = android.os.Build.VERSION.SDK_INT

                                        if (isSonyTVAndroid11) {
                                            // Remove qualities higher than 1080p for Android < 11
                                            qualityList = qualityList.filter { it.height <= 1080 } as ArrayList<QualityModel>
                                        }else {
                                            if (sdkInt < 30) {
                                                // Remove qualities higher than 1080p for Android < 11
                                                qualityList = qualityList.filter { it.height <= 1080 } as ArrayList<QualityModel>
                                            }  else {

                                            }
                                        }
                                    }
                                }
                            }

//                            if (group.getType() == C.TRACK_TYPE_TEXT) {
//                                val trackCount = group.length
//                                for (j in 0 until trackCount) {
//                                    val format = group.getTrackFormat(j)
//                                    val isSelected = group.isTrackSelected(j)
//                                    Log.d(
//                                        "ExoPlayersu",
//                                        "Subtitle track found: $isSelected \nformat $format\n"
//                                    )
//                                    // Check if the subtitle format is VTT (or other text format like SRT)
//                                    if (format.sampleMimeType == MimeTypes.TEXT_VTT) {
//                                        Log.d("ExoPlayersu", "VTT Subtitle track found: ${format.language}")
//                                    }
//                                }
//                            }
                        }

                        withContext(Dispatchers.Main) {
                            quality()
                        }
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

        // getFrameFromPlayerView()
    }


    var isFraming = true
    var frameList = ArrayList<Pair<Long, File>>()

    //    Video frame extraction by ffmpeg library which is not in use
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
                "-hwaccel", "cuda",   // Enable CUDA acceleration
                "-hwaccel_output_format", "cuda",
                // "-strftime", "1", // Enable using strftime formatting in output filename
                File(cacheDir, "frame_%03d.png").absolutePath           // Output path
//                File(cacheDir, "frame_%03d.png").absolutePath           // Output path
            )
            val commandString = command.joinToString(" ")
//            FFmpegKit.executeAsync(commandString) { session ->
//                val returnCode = session.returnCode
//                if (ReturnCode.isSuccess(returnCode)) {
//                    Log.d("FFmpeg", "Frame extracted successfully.")
//                    getPngFramesFromCache()
//                    isFraming = false
//                } else {
//                    isFraming = false
//                    Log.e("FFmpeg", "Error during frame extraction.")
//                }
//            }
        }
    }

    //    Get saved frame from cache
    fun getPngFramesFromCache(): ArrayList<Pair<Long, File>>? {
        val cacheDir = File(requireActivity().cacheDir, "frames")
        if (!cacheDir.exists()) {
            return null
        }
        val pngFiles = cacheDir.listFiles { _, name ->
            name.endsWith(".png", ignoreCase = true)
        }

        if (pngFiles == null || pngFiles.isEmpty()) {
            return null
        }
        val sortedFiles = pngFiles.sortedBy { file ->
            file.nameWithoutExtension.split("_")[1].toInt()
        }

        val ascendingOrderList = sortedFiles.sortedDescending().sorted()
        ascendingOrderList.forEachIndexed { index, file ->
            var seek = (index + 1) * 8000L
            frameList.add(Pair(seek, file))
        }

        return frameList
    }

    //    Capture video frame
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
                                delay(2000)
                                playerHandler.play()
                                visibilityCount = 0
                                playerHandler.stopHandler()
//                                if (bufferCount<=1){
//                                    delay(7000)
//                                }
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

    var videoSurface: Surface? = null

    //    Add textureview view to play the video and initialize video frame
    private fun texture() = with(binding) {
        val textureView = binding.textureView
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                videoSurface = Surface(surface)
                playerHandler.player?.setVideoSurface(videoSurface)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                playerHandler.player?.setVideoSurface(null)
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }
        }
    }

    //    Handle video and thumb transition
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

    //    Show video thumb
    private fun thumbShow() = with(binding) {
        ivVideoThumb.run {
            alpha = 0f
            visible()
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(1000)
                .start()
        }

    }

    private fun increaseSystemVolume() {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    private fun decreaseSystemVolume() {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    fun isAmazonFireTv(): Boolean {
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        val model = android.os.Build.MODEL.lowercase()
        return manufacturer.contains("amazon") || model.contains("fire")
    }

    fun isGoogleTv(): Boolean {
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        val model = android.os.Build.MODEL.lowercase()
        return manufacturer.contains("google") || model.contains("chromecast")
    }


    //    Handle volume increase
    private fun volumeUp() {
        playerHandler.unmute()
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)


        if (volumeCount <= 99) {
            volumeCount += 1
            Log.e("call","## volumeCount:::: "+volumeCount)
            //    playerHandler.setVolume(volumeCount / 100.0f)
            // Convert volumeCount (0–100) to system volume scale

            val systemVolume = (volumeCount * maxVolume) / 100
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemVolume, AudioManager.FLAG_SHOW_UI)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumePercent = (currentVolume * 100) / maxVolume
            volumeManager.setVolumePercentage(volumePercent)
            saveSeekBarProgress(volumeCount)
        }
    }

    //     Handle volume decrease
    private fun volumeDown() {

        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        if (volumeCount >= 1) {
            volumeCount -= 1
            //     playerHandler.setVolume(volumeCount / 100.0f)

            val systemVolume = (volumeCount * maxVolume) / 100
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemVolume, AudioManager.FLAG_SHOW_UI)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumePercent = (currentVolume * 100) / maxVolume

            volumeManager.setVolumePercentage(volumePercent)
            saveSeekBarProgress(volumeCount)
        }
    }

    //    Handle video forwarding functionality
    private fun forward() {
        if (::playerHandler.isInitialized) {
            val current = playerHandler.player?.currentPosition
            val duration = playerHandler.player?.duration
            val count = current!! + 30000
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

    //    Handle video forwarding by 10 sec
    private fun forward10() {
        if (::playerHandler.isInitialized) {
            val current = playerHandler.player?.currentPosition
            val duration = playerHandler.player?.duration
            val count = current!! + 10000
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




    //    Handle all view focus
    private fun selectorFocus() = with(binding) {
        //   ivSkipBack.requestFocus()
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
            }
        }
        ivVolume.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                focusView = VideoEnum.VOLUME
                if (playerHandler.player != null) {
                    if (playerHandler.player?.volume == 0f) {
                        ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                    } else {
                        ivVolume.setImageResource(R.drawable.ic_selected_volume)
                    }
                }
            } else {
                if (playerHandler.player != null) {
                    if (playerHandler.player?.volume!! > 0f) {
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
            }

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
    }


    //    Handle video quality and setting button click
    private fun quality() = with(binding) {
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
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
        }
    }

    //    Initialize video quality list
    private fun qualityInit() = with(binding) {
        rvQuality.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())

            qualityAdapter = QualityAdapter(requireActivity(), qualityList) {

                Log.e("call","###345 "+it)

                clSettingsMenu.gone()
                ivSetting.requestFocus()

                val manufacturer = Build.MANUFACTURER
                val model = Build.MODEL
                val device = Build.DEVICE

                if (qualityList[it].title == "Auto") {
                    playerHandler.setAutoResolutionBasedOnBandwidth()
                } else {
                    if (manufacturer.equals("Amazon") && model.equals("AFTSSS")){
                        if (it == qualityList.size-1){
                            playerHandler.setQuality(qualityList[it])
                        }else{
                            playerHandler.setAutoResolutionBasedOnBandwidth()
                        }
                    } else {
                        playerHandler.setQuality(qualityList[it])
                    }
                }
            }
            adapter = qualityAdapter
        }
    }

    private fun updateResulation() {
        if (playerHandler.player != null) {
            // if (playerHandler.player?.isPlaying) {
            lifecycleScope.launch(Dispatchers.IO) {
                var width = 0
                launch(Dispatchers.Main) {
                    playerHandler.player?.run {
                        if (videoFormat != null) {
                            videoFormat?.run {
                                Log.e("call","WIDTH::: "+this.width)
                                width = this.width
                            }
                        }
                    }
                }.join()

                launch(Dispatchers.IO) {
                    Log.e("call","###3456 qualityList::  "+qualityList.toString())
                    if (width > 0) {
                        if (qualityList.isNotEmpty()) {
                            qualityList.forEachIndexed { index, qualityModel ->
                                if (qualityModel.width == width) {
                                    qualityModel.isSelected = true
                                } else {
                                    qualityModel.isSelected = false
                                }
                            }
                            Log.d(
                                "ExoPlayer",
                                "Current resolution playing: detected $width \n ${qualityList}"
                            )
                        }

                    }
                }


                //  }
            }
        }
    }

//    private fun updateResulation() {
//        if (playerHandler.player != null) {
//            lifecycleScope.launch(Dispatchers.IO) {
//                var width = 0
//
//                // Get current video width on Main thread
//                withContext(Dispatchers.Main) {
//                    playerHandler.player?.videoFormat?.let { format ->
//                        Log.e("call", "WIDTH::: ${format.width}")
//                        width = format.width
//                    }
//                }
//
//                // Update qualityList on IO thread
//                if (width > 0 && qualityList.isNotEmpty()) {
//                    // If video is playing in 4K but 4K was removed, fallback to 1080p
//                    if (width >= 3840 && qualityList.none { it.width == width }) {
//                        Log.e("call", "Detected 2160p but it's removed, falling back to 1080p")
//                        qualityList.forEach { qualityModel ->
//                            qualityModel.isSelected = (qualityModel.width == 1920)
//                        }
//                    } else {
//                        qualityList.forEach { qualityModel ->
//                            qualityModel.isSelected = (qualityModel.width == width)
//                        }
//                    }
//
//                    Log.d(
//                        "ExoPlayer",
//                        "Current resolution playing: detected $width \n $qualityList"
//                    )
//                }
//            }
//        }
//    }


    //    Handle video volume functionality
    private fun volume() = with(binding) {

        if (isAmazonFireTv() || isGoogleTv()){
            // fire stick
        }else {

            volumeManager.setOnVolumeChangeListener { volumePercentage ->
                if (!isVolume) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        volumeCount = volumePercentage
                        saveSeekBarProgress(volumeCount)
                        sbVolumeSeek.progress = volumeCount
                    }
                    if (ivVolume.isFocused) {
                        if (volumePercentage <= 0) {
                            playerHandler.mute()
                            ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                        } else {
                            //     playerHandler.setVolume(volumeCount / 100.0f)
                            ivVolume.setImageResource(R.drawable.ic_selected_volume)
                            Log.e("call","##Volume change focues   "+volumeCount )
                            playerHandler.unmute()
                        }
                    } else {
                        if (volumePercentage <= 0) {
                            playerHandler.mute()
                            ivVolume.setImageResource(R.drawable.ic_mute)
                        } else {
                            //    playerHandler.setVolume(volumeCount / 100.0f)
                            Log.e("call","##Volume change   "+volumeCount )
                            ivVolume.setImageResource(R.drawable.ic_video_volume)
                            playerHandler.unmute()
                        }
                    }
                }
                isVolume = false
            }
            volumeManager.setOnAudioStatusListener { isPlaying ->
                if (isPlaying) {
                    Log.d("AudioStatus", "Audio is playing and producing sound.")
                } else {
                    Log.d("AudioStatus", "No audio is playing.")
                }
            }

            volumeManager.startMonitoring()
        }



    }

    fun volume_Intilize_To_Start_OneCount(){
//        if (volumeCount == 0){
//            volumeCount = 1
            binding.ivVolume.setImageResource(R.drawable.ic_selected_volume)
            binding.sbVolumeSeek.progress = volumeCount
            volumeManager.setVolumePercentage(volumeCount)
       // }
    }

    //    Handle following functionality
//    - update video seek every second
//    - Hide video tools after 5 seconds
//    - Show next video thumb before 10 seconds
    var time: Long = 10
    private fun updateProgressBar() {
        if (playerHandler.player != null) {
            val duration = playerHandler.getDuration()
            val currentPosition = playerHandler.getCurrentPosition()
            if (isSeeking) {
                currentDuration = currentPosition
                val progress = (currentPosition * 100 / duration.toDouble()).toInt()
                binding.sbVideoSeek.progress = progress
                binding.tvCurrentLenght.text = playerHandler.getcurrent().toString()
            }
            if (playerHandler.isPlaying()!!) {
                binding.tvDuration.text = playerHandler.getRemainsDuration()
                playerHandler.handler.postDelayed({ updateProgressBar() }, 1000)
            }

            if (visibilityCount == 5) {
                visibilityCount = 0
                Log.e("call","### 222")
//                binding.ivBack.animate().alpha(0f).setDuration(400).setStartDelay(10)
//                binding.llTools.animate().alpha(0f).setDuration(400).setStartDelay(10)
                //   showToolsAndAutoHide()
                //    binding.playerView.requestFocus()
                binding.clSettingsMenu.gone()
                binding.ivSeekThumb.invisible()
            }
            visibilityCount++
            // Log.e("updatevideo", "$isSeeking update $currentDuration")

            if (isNewVideoAvailable) {
                val video_show_count = duration - currentPosition
                if (duration > 10000) {
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
                            binding.tvRemains.setText("Playing Next Video in $time s")
                            if (time >= 1L) {
                                binding.tvRemains.setText("Playing Next Video in $time s")
                            }
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

    //    Show video tolls
    private fun toShowBackButton() = with(binding) {
        Log.e("call","### 333")
//      binding.ivBack.animate().alpha(1f).setDuration(50).setStartDelay(50)
//      binding.llTools.animate().alpha(1f).setDuration(50).setStartDelay(50)
        showToolsAndAutoHide()
        visibilityCount = 0
        clSettingsMenu.gone()
        binding.playerView.clearFocus()
    }

    //    Handle remote key after all tools where hide
    private fun handleKey(view: View) {

        view.setFocusableInTouchMode(true)
        view.requestFocus()
        view.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                visibilityCount = 0
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
                        mediaKey(StreamEnum.KEYCODE_MEDIA_FAST_FORWARD)
//                        forward10()
//                        binding.apply {
//                            ivMedia.setImageResource(R.drawable.ic_remote_forward)
//                            ivMedia.visible()
//                            ivMedia.alpha = 1f
//                            lifecycleScope.launch {
//                                delay(2000)
//                                withContext(Dispatchers.Main) {
//                                    ivMedia.animate().alpha(0f).setStartDelay(10).setDuration(300)
//                                        .withEndAction { ivMedia.gone() }.start()
//                                }
//                            }
//                        }
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
//                        playerHandler.seekBackward(10) {}
                        mediaKey(StreamEnum.KEYCODE_MEDIA_REWIND)
//                        binding.apply {
//                            ivMedia.setImageResource(R.drawable.ic_remote_backward)
//                            ivMedia.visible()
//                            ivMedia.alpha = 1f
//                            lifecycleScope.launch {
//                                delay(2000)
//                                withContext(Dispatchers.Main) {
//                                    ivMedia.animate().alpha(0f).setStartDelay(10).setDuration(300)
//                                        .withEndAction { ivMedia.gone() }.start()
//                                }
//                            }
//                        }
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        playPauseHandle()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_CENTER -> {
                        toShowBackButton()
                        viewFocus()
                        return@setOnKeyListener true
                    }
                }
            }
            else if (event.action==KeyEvent.ACTION_UP){
                when(keyCode){
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD->{
//                        fastFBshow()
                        mediaKey(StreamEnum.REMOVE_LONG_PRESS)
                    }
                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
//                        fastFBshow()
                        mediaKey(StreamEnum.REMOVE_LONG_PRESS)
                    }
                }
            }
            false
        }
    }


    override fun onPause() {
        super.onPause()
        bufferCount++
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
        bufferCount=0
        super.onStop()
    }

    override fun onDestroy() {
        Log.e("call","##### 678888888888 DESTROY")
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
    }

    //    Save video play back duration
    private fun savePlayback(
        event: Int,
        mediaId: Int,
        bunneyId: String,
        videoDuration: Long,
        thumb: String
    ) {
        if (videoDuration >= 0) {
            val request = PlayBackRequest()
            request.phoneNumber = phone
            request.mediaId = mediaId
            request.videoId = bunneyId
            request.duration = videoDuration.toString()
            homeFragment.filterItem(event, mediaId, videoDuration, bunneyId, thumb, isEnded)
            homeFragment.saveDuration(request)

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

    //    Handle all view focus
    var focusView = VideoEnum.BACKWARD
    fun viewFocus() = with(binding) {
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
            }

        }

    }


    private fun saveSeekBarProgress(progress: Int) {
        val sharedPref = requireActivity().getSharedPreferences("VideoPrefs", MODE_PRIVATE)
        sharedPref.edit().putInt("seek_progress", progress).apply()
    }

    private fun getSavedSeekBarProgress(): Int {
        val sharedPref = requireActivity().getSharedPreferences("VideoPrefs", MODE_PRIVATE)
        return sharedPref.getInt("seek_progress", 0) // 0 is default if nothing saved
    }


}