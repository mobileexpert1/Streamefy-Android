package com.streamefy.component.ui.video

import VolumeManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.Player
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.databinding.FragmentVideoBinding


class VideoFragment : BaseFragment<FragmentVideoBinding>() {
    override fun bindView(): Int = R.layout.fragment_video
    lateinit var playerHandler: PlayerHandler
    var getLengthOnce = true
    var isEnded = false
    private lateinit var volumeManager: VolumeManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            playerHandler = PlayerHandler(requireActivity(), playerView)
            playerHandler.setMediaUri("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4")
            ivPlay.setOnClickListener {
                if (playerHandler.isPlaying()!!) {
                    playerHandler.pause()
                    ivPlay.setImageResource(R.drawable.ic_video_play)
                } else {
                    if (isEnded) {
                        playerHandler.player?.seekTo(0)
                    } else {
                        playerHandler.play()
                    }
                    ivPlay.setImageResource(R.drawable.ic_video_pause)
                    updateProgressBar()
                }

            }
            ivSkipForward.setOnClickListener {
                var current=playerHandler.player?.currentPosition
                var duration=playerHandler.player?.duration
                var count= current!! + 10000

                if (duration!! > count){
                    playerHandler.seekTo(count)
                }else{
                    playerHandler.seekTo(duration)
                }

            }
            ivSkipBack.setOnClickListener {

                playerHandler.seekBackward(10)

            }


            ivZoom.setOnClickListener {
                playerHandler.toggleFullScreen()
            }



            playerHandler.player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        binding.sbVideoSeek.max = 100
                        if (getLengthOnce) {
                            tvDuration.setText(playerHandler.getTotalLength())
                            getLengthOnce = false
                        }
                        isEnded=false
                        updateProgressBar()
                    } else if (playbackState == Player.STATE_ENDED) {
                        ivPlay.setImageResource(R.drawable.ic_video_play)
                        isEnded=true
                    }
                }
            })

            sbVideoSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        playerHandler.setPlaybackProgress(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            // Volume control
            ivVolume.setOnClickListener {
                Log.e("sncsnc", "dndnv ${playerHandler.isMuted()}")
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

        }
        volume()

    }

    private fun volume()= with(binding) {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.pause()
        playerHandler.release()
        volumeManager.stopMonitoring()

    }

}