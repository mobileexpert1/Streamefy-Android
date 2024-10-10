package com.streamefy.component.ui.video

import VolumeManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.HomeFragment
import com.streamefy.data.PrefConstent
import com.streamefy.databinding.FragmentVideoBinding
import com.streamefy.utils.gone
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showMessage
import com.streamefy.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class VideoFragment : BaseFragment<FragmentVideoBinding>() {
    override fun bindView(): Int = R.layout.fragment_video
    lateinit var playerHandler: PlayerHandler
    var getLengthOnce = true
    var isEnded = false
    var visibilityCount = 0
    var volumeCount = 20
    var isOpenSettingFirst = false
    var videoQualityIndex = 0
    var playbackduration:Long=0
    private lateinit var volumeManager: VolumeManager

    //       var videoUrl="https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
    var videoUrl = ""
    var isSmartRevision = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleKey(binding.playerView)
        volumeManager = VolumeManager(requireActivity())
        volumeManager.setVolumePercentage(5)
        arguments?.run {
            videoUrl = getString(PrefConstent.VIDEO_URL).toString()
            isSmartRevision = getBoolean(PrefConstent.SMART_REVISION)
            playbackduration = getString(PrefConstent.PLAY_BACK_DURATION).toString().toLong()
            Log.e("ckdanmcn", "$isSmartRevision mkadnc $videoUrl")
        }
        binding.apply {
            playerHandler = PlayerHandler(requireActivity(), playerView)
            playerHandler.setMediaUri(videoUrl,playbackduration)

//            val progress = (playbackduration * 100 / duration.toDouble()).toInt()
//            binding.sbVideoSeek.progress = progress

            clickme()
            listener()
            quality()
        }
        volume()

        // bitPlayer()
        selectorFocus()
    }

    fun clickme() = with(binding) {
        ivBack.setOnClickListener { findNavController().popBackStack() }

        ivPlay.setOnClickListener {
            val params = ivPlay.layoutParams as LinearLayoutCompat.LayoutParams
            params.width =
                resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
            params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
            ivPlay.layoutParams = params
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
        ivSkipForward.setOnClickListener {
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
            playerHandler.seekBackward(10)
        }
        ivZoom.setOnClickListener {
            //  playerHandler.toggleFullScreen()
        }
        ivVolume.setOnClickListener {
            Log.e("sncsnc", "dndnv $volumeCount ${playerHandler.isMuted()}")
            visibilityCount = 0
            if(playerHandler.player!=null) {
                if (playerHandler.player?.volume==0f) {
                    playerHandler.unmute()
                    volumeCount = 1
                    ivVolume.setImageResource(R.drawable.ic_selected_volume)
                    sbVolumeSeek.setProgress(volumeCount)
//                volumeManager.setVolumePercentage(volumeCount)
                    // volumeUp()

                } else {
                    playerHandler.mute()
                    ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                    sbVolumeSeek.setProgress(0)
//                playerHandler.setVolume(0 / 100.0f)
                    volumeManager.setVolumePercentage(0)
                    volumeCount = 0
                }
            }
        }
        llVolumeSeek.setOnClickListener {
        }
        llSeek.setOnClickListener {
        }
    }

    fun listener() = with(binding) {
        playerHandler.player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    binding.sbVideoSeek.max = 100
                    if (getLengthOnce) {
                        tvDuration.setText(playerHandler.getTotalLength())
                        getLengthOnce = false
                        ivPlay.setImageResource(R.drawable.ic_video_pause)
                    }
                    isEnded = false
                    updateProgressBar()
                } else if (playbackState == Player.STATE_ENDED) {
                    ivPlay.setImageResource(R.drawable.ic_video_play)
                    isEnded = true
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayerError", "Playback error: " + error.message, error)
            }
        })

//        // Volume control
        // sbVolumeSeek.max = 100
        // sbVolumeSeek.progress = (playerHandler.getVolume() * 100).toInt()
//        sbVolumeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(
//                seekBar: SeekBar?,
//                progress: Int,
//                fromUser: Boolean
//            ) {
////                    playerHandler.setVolume(progress / 100.0f)
//                volumeManager.setVolumePercentage(progress)
//                Log.e("volumetes", "Playback error: " + progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })

        sbVideoSeek.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            Log.e("djhfjd", "shcudh $event")
            toShowBackButton()
            if (event.action == KeyEvent.ACTION_DOWN) {
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
            Log.e("djhfjd", "shcudh $event $volumeCount")
            binding.ivBack.animate().alpha(1f).setDuration(50).setStartDelay(50)
            binding.llTools.animate().alpha(1f).setDuration(50).setStartDelay(50)
            visibilityCount = 0
            clSettingsMenu.gone()

            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        ivVolume.requestFocus()
                        return@OnKeyListener true // Consume the event
                    }

                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        ivSkipBack.requestFocus()
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
                    sbVolumeSeek.requestFocus()
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

        ivBack.remoteKey {
            when (it) {
                StreamEnum.DOWN_DPAD_KEY -> {
                    sbVolumeSeek.requestFocus()
                    visibilityCount = 0
                }

                StreamEnum.UP_DPAD_KEY -> {
                    sbVolumeSeek.requestFocus()
                    visibilityCount = 0
                }

                else -> {}
            }
        }

//        ivSkipBack.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
//            if (event.action == KeyEvent.ACTION_DOWN) {
//
//            }
//            false
//        })
    }

    fun volumeUp() {
        Log.e("hdhhdhds", "volume count UP $volumeCount")
        if (volumeCount <= 99) {
            volumeCount += 1
            playerHandler.setVolume(volumeCount / 100.0f)
            volumeManager.setVolumePercentage(volumeCount)
        }
    }

    fun volumeDown() {
        Log.e("hdhhdhds", "volume count Down $volumeCount")
        if (volumeCount > 1) {
            volumeCount -= 1
            playerHandler.setVolume(volumeCount / 100.0f)
            volumeManager.setVolumePercentage(volumeCount)
        }
    }

    private fun updateDuration() {
//        val position = player.currentPosition
//        HomeFragment.homeFragment.currentVideoDuration = position
//        if (player.playWhenReady) {
//            playerHandler.handler.postDelayed({ updateDuration() }, 500)
//        } else {
//            playerHandler.stopHandler()
//        }
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
        llTools.requestFocus()
        ivSkipBack.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                val params = ivSkipBack.layoutParams as LinearLayoutCompat.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
                ivSkipBack.layoutParams = params
            } else {
                val params = ivSkipBack.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivSkipBack.layoutParams = params
            }
        }
        ivSkipForward.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                val params = ivSkipForward.layoutParams as LinearLayoutCompat.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
                ivSkipForward.layoutParams = params
//                ivSkipForward.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.red))
            } else {
                val params = ivSkipForward.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivSkipForward.layoutParams = params
//                ivSkipForward.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), com.otpview.R.color.transparent))
            }
        }
        ivPlay.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                val params = ivPlay.layoutParams as LinearLayoutCompat.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
                ivPlay.layoutParams = params

                if (playerHandler.isPlaying()!!) {
                    ivPlay.setImageResource(R.drawable.ic_selected_pause)
                } else {
                    ivPlay.setImageResource(R.drawable.ic_seleceted_play)
                }
            } else {
                val params = ivPlay.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivPlay.layoutParams = params
                if (playerHandler.isPlaying()!!) {
                    ivPlay.setImageResource(R.drawable.ic_video_pause)
                } else {
                    ivPlay.setImageResource(R.drawable.ic_video_play)
                }

            }


//            if (playerHandler.isPlaying()!!) {
//                playerHandler.pause()
//                ivPlay.setImageResource(R.drawable.ic_seleceted_play)
//            } else {
//                if (isEnded) {
//                    playerHandler.player?.seekTo(0)
//                } else {
//                    playerHandler.play()
//                }
//                ivPlay.setImageResource(R.drawable.ic_selected_pause)
//                updateProgressBar()
//            }

        }

        ivSetting.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toShowBackButton()
                val params = ivSetting.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._16sdp)
                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
                ivSetting.layoutParams = params

            } else {
                val params = ivSetting.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivSetting.layoutParams = params

            }
        }

        ivVolume.setOnFocusChangeListener { _, hasFocus ->
            Log.e("smclksmc", "$hasFocus snknc ${playerHandler.isMuted()}")
            if (hasFocus) {
                toShowBackButton()
                val params = ivVolume.layoutParams as LinearLayoutCompat.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
                ivVolume.layoutParams = params
                if (playerHandler.player != null) {
                    if (playerHandler.player?.volume == 0f) {
//                if (playerHandler.isMuted()) {
                        ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                    } else {
                        ivVolume.setImageResource(R.drawable.ic_selected_volume)
                    }
                }

            } else {
                val params = ivSetting.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivVolume.layoutParams = params
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
            }
        }

//        sbVideoSeek.setOnClickListener {
//            Log.e("kkdkdkdkd", "clicked")
//        }
//
//        sbVideoSeek.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                Log.e("kkdkdkdkd", "fosus")
//            } else {
//                Log.e("kkdkdkdkd", "disabled")
//            }
//        }

        llTools.setOnClickListener { }
        llTools.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                ivSkipBack.requestFocus()
            }
        }
//        llVolumeSeek.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                llVolumeSeek.setBackgroundColor(ContextCompat.getColor(requireActivity(),R.color.light_gray) )
//            }else{
//                llVolumeSeek.setBackgroundColor(ContextCompat.getColor(requireActivity(),
//                    com.otpview.R.color.transparent) )
//            }
//        }

    }

    private fun bitPlayer() = with(binding) {
        binding.bitplayer.player = BitmovinPlayer().bitPlayer(requireActivity(), bitplayer)

    }


    private fun quality() = with(binding) {
        val qualityButtons = ArrayList<TextView>()
        qualityButtons.clear()
        if (isSmartRevision) {
            qualityButtons.add(tv480p)
            qualityButtons.add(tv1080p)
            qualityButtons.add(tv2080p)
        } else {
            qualityButtons.add(tv480p)
            tv1080p.gone()
            tv2080p.gone()
        }
        ivSetting.setOnClickListener {
            if (clSettingsMenu.isVisible) {
                clSettingsMenu.gone()
            } else {
                clSettingsMenu.visible()

                qualityButtons[videoQualityIndex].requestFocus()
                qualityButtons[videoQualityIndex].setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.light_gray
                    )
                )
            }
        }

        qualityButtons.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                clSettingsMenu.gone()
                playerHandler.setQuality(textView.text.toString())
                ivSetting.requestFocus()
                isOpenSettingFirst = true
                qualityButtons.forEachIndexed { subindex, subText ->
                    if (index == subindex) {
                        subText.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.light_gray
                            )
                        )
                        videoQualityIndex = subindex

                    } else {
                        subText.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.white
                            )
                        )
                    }

                }
            }

            textView.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    textView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.light_gray
                        )
                    )
                } else {
                    textView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.white
                        )
                    )
                    // clSettingsMenu.gone()
                }
            }

            textView.setOnKeyListener { v, keyCode, event ->
                Log.e("hdhhdhdhd", "ddmv ${event}")
                visibilityCount = 0
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (index + 1 < qualityButtons.size) {
                                qualityButtons[index + 1].requestFocus()
                            } else {
                                //qualityButtons[index].requestFocus()
                                ivSetting.requestFocus()
                            }
                            return@setOnKeyListener true
                        }

                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (index - 1 >= 0) {
                                qualityButtons[index - 1].requestFocus()
                            } else {
                                qualityButtons[index].requestFocus()
//                                ivBack.requestFocus()
                            }
                            return@setOnKeyListener true
                        }

                    }
                }
                false
            }
        }

    }

    private fun volume() = with(binding) {


        volumeManager.setOnVolumeChangeListener { volumePercentage ->
            // Update the SeekBar with the volume percentage
            Log.e("hdhhdhds", "$volumeCount volume manager $volumePercentage")

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

    private fun updateProgressBar() {
        val duration = playerHandler.getDuration()
        val currentPosition = playerHandler.getCurrentPosition()
        val progress = (currentPosition * 100 / duration.toDouble()).toInt()
        binding.sbVideoSeek.progress = progress
        // Log.e("ssknckscn", "$currentPosition sbcsjbc ${progress} ${playerHandler.getcurrent()}")
        binding.tvCurrentLenght.setText(playerHandler.getcurrent().toString())

        if (playerHandler.isPlaying()!!) {
            playerHandler.handler.postDelayed({ updateProgressBar() }, 500)
        }
        visibilityCount++
        if (visibilityCount == 15) {
            visibilityCount = 0
            binding.ivBack.animate().alpha(0f).setDuration(1000).setStartDelay(50)
            binding.llTools.animate().alpha(0f).setDuration(1000).setStartDelay(50)
            binding.playerView.requestFocus()
            binding.clSettingsMenu.gone()
//            binding.ivBack.gone()
        }
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
            Log.e("dldfdfd", "ddmv ")
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        toShowBackButton()
                        binding.ivBack.requestFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        toShowBackButton()
                        binding.ivSkipBack.requestFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        toShowBackButton()
                        binding.ivSkipBack.requestFocus()
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        toShowBackButton()
                        binding.ivVolume.requestFocus()
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
        if (playerHandler.isPlaying() != null) {
            if (playerHandler.isPlaying()!!) {
                playerHandler.pause()
            }
        }
        binding.bitplayer.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.pause()
        playerHandler.release()
        volumeManager.stopMonitoring()
        binding.bitplayer.onDestroy()
    }


    override fun onStart() {
        super.onStart()
        binding.bitplayer.onStart()
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.playerView.onResume()
        isOpenSettingFirst = false
    }


    override fun onStop() {
        super.onStop()
        if (playerHandler.player!=null){
            playerHandler.player?.run {
                HomeFragment.videoduraion=currentPosition
            }

            playerHandler.pause()
            playerHandler.release()
            volumeManager.stopMonitoring()
        }

        binding.bitplayer.onStop()
    }


}