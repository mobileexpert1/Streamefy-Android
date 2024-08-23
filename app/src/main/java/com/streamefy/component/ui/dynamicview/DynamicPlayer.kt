package com.streamefy.component.ui.dynamicview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.databinding.FragmentDynamicPlayerBinding

class DynamicPlayer : BaseFragment<FragmentDynamicPlayerBinding>(){
    override fun bindView(): Int =R.layout.fragment_dynamic_player
    var videoUrl="https://vz-4aa86377-b82.b-cdn.net/bcdn_token=LIwRhyMrf4BE0X-TRKQ3OgXL_sA23mLtd-vlXQsQoGQ&expires=1724312963&token_path=%2F06a93993-df8b-44c5-bf95-24d107ff5a95%2F/06a93993-df8b-44c5-bf95-24d107ff5a95/playlist.m3u8"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val webSettings: WebSettings = binding.webview.settings
            webSettings.javaScriptEnabled = true

            // Set a WebViewClient to handle navigation within the WebView
            webview.webViewClient = WebViewClient()

            // Load the HTML content containing the iframe
            val iframeHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Video Player</title>
                <style>
                    body { margin: 0; padding: 0; }
                    iframe { width: 100%; height: 100%; border: none; }
                </style>
            </head>
            <body>
                <iframe src="$videoUrl" allowfullscreen></iframe>
            </body>
            </html>
        """
            var videoUrl="https://iframe.mediadelivery.net/embed/280659/22796632-8018-4073-9b19-8cd5c74a6fdc?token=6e69240e7a31ffdd0cb3e1d6c4f896bc0db1dfcf323d5d915af12091b134bb1f&expires=1724316142"

//            webview.loadData(iframeHtml, "text/html", "UTF-8")
            webview.loadUrl(videoUrl)

        }
    }
}