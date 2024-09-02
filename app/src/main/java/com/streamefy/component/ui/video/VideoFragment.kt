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
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.Player
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.databinding.FragmentVideoBinding
import com.streamefy.utils.gone
import com.streamefy.utils.visible


class VideoFragment : BaseFragment<FragmentVideoBinding>() {
    override fun bindView(): Int = R.layout.fragment_video
    lateinit var playerHandler: PlayerHandler
    var getLengthOnce = true
    var isEnded = false
    private lateinit var volumeManager: VolumeManager
    var videoUrl =
        "https://vz-4aa86377-b82.b-cdn.net/bcdn_token=39Pr4HZEbPaAI66HI09oZEZExebJ6NK9TE4lcHrtUIg&expires=1725368264&token_path=%2F92a87eaa-7b93-4fc1-a83c-59ac60331112%2F/92a87eaa-7b93-4fc1-a83c-59ac60331112/playlist.m3u8"

    //   var videoUrl="https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            playerHandler = PlayerHandler(requireActivity(), playerView)
            playerHandler.setMediaUri(videoUrl)
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
                        isEnded = false
                        updateProgressBar()
                    } else if (playbackState == Player.STATE_ENDED) {
                        ivPlay.setImageResource(R.drawable.ic_video_play)
                        isEnded = true
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
            quality()
        }
        volume()

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
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.pause()
        playerHandler.release()
        volumeManager.stopMonitoring()

    }

}