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
}


