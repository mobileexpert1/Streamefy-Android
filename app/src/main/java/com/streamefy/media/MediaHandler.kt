package com.streamefy.media

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.session.MediaSession
import android.media.session.MediaSession.Callback
import android.util.Log
import android.view.KeyEvent

class MediaHandler(private val context: Context) {

    private var mediaSession: MediaSession? = null
    private var isPlaying = false

    init {
        // Initialize MediaSession
        mediaSession = MediaSession(context, "MediaHandlerSession")
        mediaSession?.setFlags(
            MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
            MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
        )

        // Set the callback for media button actions
        mediaSession?.setCallback(object : Callback() {
            override fun onPlay() {
                super.onPlay()
                onPlay()
            }

            override fun onPause() {
                super.onPause()
                onPause()
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                onSkipToNext()  // Forward / Skip to next media
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                onSkipToPrevious()  // Backward / Skip to previous media
            }

            override fun onFastForward() {
                super.onFastForward()
                onFastForward()  // Fast forward media
            }

            override fun onRewind() {
                super.onRewind()
                onRewind()  // Rewind media
            }
        })

        // Activate the session
        mediaSession?.isActive = true
    }

    // Play action
    private fun onPlay() {
        if (!isPlaying) {
            isPlaying = true
            Log.d("MediaHandler", "Play media")
            // Start video playback or unpause video (implement your play functionality here)
        }
    }

    // Pause action
    private fun onPause() {
        if (isPlaying) {
            isPlaying = false
            Log.d("MediaHandler", "Pause media")
            // Pause video playback (implement your pause functionality here)
        }
    }

    // Skip to next (forward) action
    private fun onSkipToNext() {
        Log.d("MediaHandler", "Skip to next")
        // Implement functionality to skip to the next media (forward)
    }

    // Skip to previous (backward) action
    private fun onSkipToPrevious() {
        Log.d("MediaHandler", "Skip to previous")
        // Implement functionality to skip to the previous media (backward)
    }

    // Fast forward action
    private fun onFastForward() {
        Log.d("MediaHandler", "Fast forward media")
        // Implement fast-forward functionality
    }

    // Rewind action
    private fun onRewind() {
        Log.d("MediaHandler", "Rewind media")
        // Implement rewind functionality
    }

    // Handle Key Events (Play/Pause, Forward, Backward)
    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (isPlaying) onPause() else onPlay()
                true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                onSkipToNext() // Forward
                true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                onSkipToPrevious() // Backward
                true
            }
            else -> false
        }
    }

    // Clean up MediaSession
    fun release() {
        mediaSession?.release() // Release the media session when done
    }
}
