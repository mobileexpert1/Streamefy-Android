package com.streamefy.component.ui.dynamicview

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.databinding.FragmentDynamicPlayerBinding

class DynamicPlayer : BaseFragment<FragmentDynamicPlayerBinding>() {
    override fun bindView(): Int = R.layout.fragment_dynamic_player
    override fun netStatus() {
    }

    var videoUrl =""
       // "https://iframe.mediadelivery.net/play/292623/06a93993-df8b-44c5-bf95-24d107ff5a95"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val webSettings = webview.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            webview.webViewClient = WebViewClient()
            webview.webChromeClient = WebChromeClient()
           // webview.isFocusable = true
           // webview.isFocusableInTouchMode = true
           // webview.requestFocus()
//            videoUrl =
//                "https://iframe.mediadelivery.net/embed/292623/06a93993-df8b-44c5-bf95-24d107ff5a95?token=$hash&expires=$expirationTimestamp"
//            Log.e("sjncsjcnsj", "$expirationTimestamp sjncsjc $hash\n$videoUrl")
//
//            lifecycleScope.launch(Dispatchers.Main) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//                }
//
//                delay(200)
//                val iframeHtml = """
//            <!DOCTYPE html>
//            <html>
//            <head>
//                <title>Video Player</title>
//                <style>
//                    body { margin: 0; padding: 0; }
//                    iframe { width: 100%; height: 100%; border: none; }
//                </style>
//            </head>
//            <body>
//                <iframe src="$videoUrl" allowfullscreen></iframe>
//            </body>
//            </html>
//        """
//                            //   var videoUrl="https://iframe.mediadelivery.net/embed/280659/22796632-8018-4073-9b19-8cd5c74a6fdc?token=6e69240e7a31ffdd0cb3e1d6c4f896bc0db1dfcf323d5d915af12091b134bb1f&expires=1724316142"
//
//            webview.loadData(iframeHtml, "text/html", "UTF-8")
//                webview.webViewClient = object : WebViewClient() {
//                    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
//                        Log.e("WebViewError", "Error $errorCode: $description")
//                    }
//
//                    override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
//                        Log.e("WebViewError", "HTTP error ${errorResponse.statusCode}: ${errorResponse.reasonPhrase}")
//                    }
//                }
//            }

//            old
            // Load the HTML content containing the iframe
//               var videoUrl="https://iframe.mediadelivery.net/embed/280659/22796632-8018-4073-9b19-8cd5c74a6fdc?token=6e69240e7a31ffdd0cb3e1d6c4f896bc0db1dfcf323d5d915af12091b134bb1f&expires=1724316142"
//
//            val iframeHtml = """
//            <!DOCTYPE html>
//            <html>
//            <head>
//                <title>Video Player</title>
//                <style>
//                    body { margin: 0; padding: 0; }
//                    iframe { width: 100%; height: 100%; border: none; }
//                </style>
//            </head>
//            <body>
//                <iframe src="$videoUrl" allowfullscreen></iframe>
//            </body>
//            </html>
//        """
//
//            webview.loadData(iframeHtml, "text/html", "UTF-8")
////            webview.loadUrl(videoUrl)
////

//            javascripts()
            iframe()
        }
    }

    fun javascripts()= with(binding){
        webview.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        WebView.setWebContentsDebuggingEnabled(true)

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <script src="https://cdn.example.com/playerjs"></script> <!-- Ensure this URL is correct -->
                <script type="text/javascript">
                    document.addEventListener('DOMContentLoaded', function() {
                        var player = new playerjs.Player('iframe');

                        player.on('ready', function() {
                            console.log('Player is ready');
                            player.on('play', function() {
                                console.log('play event triggered');
                            });

                            player.getDuration(function(duration) {
                                console.log('Video duration:', duration);
                            });

                            if (player.supports('method', 'mute')) {
                                player.mute();
                            }

                            player.play();
                        });

                        player.on('error', function(error) {
                            console.error('Player error:', error);
                        });
                    });
                </script>
            </head>
            <body>
                <iframe id="iframe" src="https://iframe.mediadelivery.net/play/292623/06a93993-df8b-44c5-bf95-24d107ff5a95" width="640" height="360" frameborder="0" allowfullscreen></iframe>
            </body>
            </html>
        """

        // Load the HTML content into the WebView
        webview.loadData(htmlContent, "text/html", "UTF-8")
    }

    fun iframe()= with(binding){
        val url="https://iframe.mediadelivery.net/embed/348613/73011535-0341-4e4f-8f7f-42a1a6246e88?token=efadd6587f7383f8f194cdb3fee8cb1d20ad30c4683a009b92b7a9608c9a6364&expires=1737122553&autoplay=true&loop=true&muted=true&preload=true&responsive=true"

        webview.isFocusable = true
        webview.isFocusableInTouchMode = true
        webview.requestFocus()

        webview.setOnKeyListener { v, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_DPAD_RIGHT,
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // Handle D-Pad navigation inside WebView content
                        return@setOnKeyListener true
                    }
                    KeyEvent.KEYCODE_ENTER -> {
                        // Handle the 'Enter' key to trigger actions in the WebView
                        return@setOnKeyListener true
                    }
                    else -> {
                        return@setOnKeyListener false
                    }
                }
            }
            return@setOnKeyListener false
        }
        webview.evaluateJavascript("""
     document.addEventListener('DOMContentLoaded', function() {
        // Select buttons like fullscreen, forward, backward, settings
        var fullscreenButton = document.querySelector('button.fullscreen');  // Adjust selector
        var forwardButton = document.querySelector('button.forward');  // Adjust selector
        var backwardButton = document.querySelector('button.backward');  // Adjust selector
        var settingsButton = document.querySelector('button.settings');  // Adjust selector

        // Focus on specific buttons
        if (fullscreenButton) {
            fullscreenButton.focus();  // Focus on fullscreen button
        }
        if (forwardButton) {
            forwardButton.focus();  // Focus on forward button
        }
        if (backwardButton) {
            backwardButton.focus();  // Focus on backward button
        }
        if (settingsButton) {
            settingsButton.focus();  // Focus on settings button
        }
    });
""", null)

        // Load the URL into the WebView
        webview.loadUrl(url)
    }


}