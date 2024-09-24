package com.streamefy.component.ui.video

import VolumeManager
import android.content.res.ColorStateList
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.data.PrefConstent
import com.streamefy.databinding.FragmentVideoBinding
import com.streamefy.utils.gone
import com.streamefy.utils.visible


class VideoFragment : BaseFragment<FragmentVideoBinding>() {
    override fun bindView(): Int = R.layout.fragment_video
    lateinit var playerHandler: PlayerHandler
    var getLengthOnce = true
    var isEnded = false

    private lateinit var volumeManager: VolumeManager

//       var videoUrl="https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
       var videoUrl=""
           //"https://vz-356f31a1-aee.b-cdn.net/bcdn_token=FmAWzCBl7_7DZ4gHHTZ8rg&expires=1727100940&token_path=%2Fa0520395-a4de-40f2-9f7f-48eacbb92b2f%2F/a0520395-a4de-40f2-9f7f-48eacbb92b2f/playlist.m3u8"
// var videoUrl="https://vz-7615d1d2-22b.b-cdn.net/bcdn_token=-ScuHJWB2f7S8SIfnfWbvVgPPSJfm7Otiiy_QsGe6x8&expires=1725706058&token_path=%2Fe66c2d1a-9c6b-4fe1-8ca5-d314704eedc3%2F/e66c2d1a-9c6b-4fe1-8ca5-d314704eedc3/playlist.m3u8"
// var videoUrl="https://vz-4aa86377-b82.b-cdn.net/bcdn_token=ofkhmBJ7r3GaillWe626UsrOeIOXpunm_5r6kGdsu0o&expires=1725714358&token_path=%2F06a93993-df8b-44c5-bf95-24d107ff5a95%2F/06a93993-df8b-44c5-bf95-24d107ff5a95/playlist.m3u8"
// var videoUrl="https://vz-7615d1d2-22b.b-cdn.net/bcdn_token=QqUJlEQQSdWVkLpdT2ESJ8kDAvRx1ZMO8LNbCi5X-do&expires=1725714672&token_path=%2F2b8fc2cf-958d-4c59-8208-f523285b505e%2F/2b8fc2cf-958d-4c59-8208-f523285b505e/playlist.m3u8"
// var videoUrl="https://vz-057e4b99-b5a.b-cdn.net/bcdn_token=Mrrup8-VOHUJLHRp2AMFBw&expires=1725632241&token_path=%2Fa8fd227f-e7b9-4ff3-ae6b-0ee02db9616d%2F/a8fd227f-e7b9-4ff3-ae6b-0ee02db9616d/playlist.m3u8"
// var videoUrl="https://vz-4aa86377-b82.b-cdn.net/bcdn_token=UIKCsoA2Yllz_Q-Yv5nM0GGaXMTuw_Mmi4X-sq_zldQ&expires=1725970468&token_path=%2F06a93993-df8b-44c5-bf95-24d107ff5a95%2F/06a93993-df8b-44c5-bf95-24d107ff5a95/playlist.m3u8"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            videoUrl = getString(PrefConstent.VIDEO_URL).toString()
           Log.e("ckdanmcn","mkadnc $videoUrl")
        }
        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            playerHandler = PlayerHandler(requireActivity(), playerView)
            playerHandler.setMediaUri(videoUrl)
            ivPlay.setOnClickListener {
                val params = ivPlay.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
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
//            sbVideoSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//                override fun onProgressChanged(
//                    seekBar: SeekBar?,
//                    progress: Int,
//                    fromUser: Boolean
//                ) {
//                    if (fromUser) {
//                        playerHandler.setPlaybackProgress(progress)
//                    }
//                }
//
//                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//            })

            // Volume control
            ivVolume.setOnClickListener {
                Log.e("sncsnc", "dndnv ${playerHandler.isMuted()}")
                if (playerHandler.isMuted()) {
                    playerHandler.unmute()
                    ivVolume.setImageResource(R.drawable.ic_selected_volume)
//                    ivVolume.setImageResource(R.drawable.ic_video_volume)
                    sbVolumeSeek.max = 30
                } else {
                    playerHandler.mute()
                    ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)

                    // ivVolume.setImageResource(R.drawable.ic_mute)
                    sbVolumeSeek.max = 0
                }

            }
            sbVolumeSeek.max = 100
            sbVolumeSeek.progress = (playerHandler.getVolume() * 100).toInt()
            sbVolumeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
//                    playerHandler.setVolume(progress / 100.0f)
                    volumeManager.setVolumePercentage(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            quality()
        }
        volume()

   // bitPlayer()
    selectorFocus()
    }

    fun selectorFocus()= with(binding){
        ivSkipForward.requestFocus()
        ivSkipBack.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val params = ivSkipBack.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
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
                val params = ivSkipForward.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
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
                val params = ivPlay.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
                ivPlay.layoutParams = params

                if (playerHandler.isPlaying()!!) {
                    ivPlay.setImageResource(R.drawable.ic_selected_pause)
                }else{
                    ivPlay.setImageResource(R.drawable.ic_seleceted_play)
                }
            } else {
                val params = ivPlay.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivPlay.layoutParams = params
                if (playerHandler.isPlaying()!!) {
                    ivPlay.setImageResource(R.drawable.ic_video_pause)
                }else{
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
                val params = ivSetting.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
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
            if (hasFocus) {
                val params = ivVolume.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
                ivVolume.layoutParams = params

                if (playerHandler.isMuted()) {
                    playerHandler.unmute()
                    ivVolume.setImageResource(R.drawable.ic_selected_volume)
                    sbVolumeSeek.max = 30
                } else {
                    playerHandler.mute()
                    ivVolume.setImageResource(R.drawable.ic_volume_selected_muted)
                    sbVolumeSeek.max = 0
                }

            } else {
                val params = ivSetting.layoutParams as LinearLayoutCompat.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivVolume.layoutParams = params

                if (playerHandler.isMuted()) {
                    playerHandler.unmute()
                    ivVolume.setImageResource(R.drawable.ic_video_volume)
                    sbVolumeSeek.max = 30
                } else {
                    playerHandler.mute()
                    ivVolume.setImageResource(R.drawable.ic_mute)
                    sbVolumeSeek.max = 0
                }

            }
        }

    }

    private fun bitPlayer()= with(binding) {
        binding.bitplayer.player=  BitmovinPlayer().bitPlayer(requireActivity(),bitplayer)

    }

    private fun quality() = with(binding) {
        val qualityButtons: List<TextView> = listOf(
            tv360p,
            tv480p,
            tv720p,
            tv1080p,
            tv1440p,
            tv4K
        )
        ivSetting.setOnClickListener {
            if (clSettingsMenu.isVisible) {
                clSettingsMenu.gone()
            } else {
                clSettingsMenu.visible()
            }
        }

        qualityButtons.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                clSettingsMenu.gone()
                playerHandler.setQuality(textView.text.toString())

                qualityButtons.forEachIndexed { subindex, subText ->
                    if (index == subindex) {
                        subText.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.light_gray
                            )
                        )
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
                }
            }
        }

    }

    private fun volume() = with(binding) {
        volumeManager = VolumeManager(requireActivity())

        volumeManager.setOnVolumeChangeListener { volumePercentage ->
            // Update the SeekBar with the volume percentage
            sbVolumeSeek.progress = volumePercentage
        }

        volumeManager.startMonitoring()
    }

    private fun updateProgressBar() {
        val duration = playerHandler.getDuration()
        val currentPosition = playerHandler.getCurrentPosition()
        val progress = (currentPosition * 100 / duration.toDouble()).toInt()
        binding.sbVideoSeek.progress = progress
        Log.e("ssknckscn", "$currentPosition sbcsjbc ${progress} ${playerHandler.getcurrent()}")
        binding.tvCurrentLenght.setText(playerHandler.getcurrent().toString())

        if (playerHandler.isPlaying()!!) {
            playerHandler.handler.postDelayed({ updateProgressBar() }, 500)
        }

    }




    private lateinit var audioManager: AudioManager

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
        binding. bitplayer.onDestroy()
    }



    override fun onStart() {
        super.onStart()
        binding.bitplayer.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding. playerView.onResume()
    }


    override fun onStop() {
        super.onStop()
        binding.bitplayer.onStop()
    }



}