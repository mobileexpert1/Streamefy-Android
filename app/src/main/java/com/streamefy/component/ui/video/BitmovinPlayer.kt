package com.streamefy.component.ui.video

import android.content.Context
import com.bitmovin.analytics.api.AnalyticsConfig
import com.bitmovin.analytics.api.SourceMetadata
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.analytics.AnalyticsPlayerConfig
import com.bitmovin.player.api.analytics.AnalyticsSourceConfig
import com.bitmovin.player.api.source.Source
import com.bitmovin.player.api.source.SourceConfig
import com.bitmovin.player.api.source.SourceType

class BitmovinPlayer {
        private var analyticsLicenseKey = "2709f8d0-5849-4f62-9ad7-e1f8dba858b4"
    lateinit var player: Player
    fun bitPlayer(context: Context, mplayer: PlayerView): Player {

        val analyticsConfig = AnalyticsConfig(
            licenseKey = analyticsLicenseKey,
        )
//
//        val url = "https://cdn.bitmovin.com/content/assets/art-of-motion-dash-hls-progressive/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd"
//        val sourceConfig = SourceConfig(url, SourceType.Dash) // or SourceConfig.fromUrl(url) to auto-detect the source type
//        val source = Source(sourceConfig)
//
//         player = mplayer.player!!
//        player.load(source)


        player = Player(
            context = context,
            analyticsConfig = AnalyticsPlayerConfig.Enabled(
                analyticsConfig
            ),
        )
        mplayer.player = player
        val sourceURL =
            "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd"


        val streamTitle = "tvtesting"
        val source = Source(
            SourceConfig(
                sourceURL,
                type = SourceType.Dash,
                title = streamTitle,
            ),
            AnalyticsSourceConfig.Enabled(
                SourceMetadata(
                    videoId = "androidtv-wizard-tvtesting-1726042563644",
                    title = streamTitle,
                )
            ),
        )

        player.load(source)
        return player
    }


    fun onStart() {
        player.onStart()
    }

    fun onResume() {
        player.onResume()
    }

    fun onStop() {
        player.onStop()
    }

}