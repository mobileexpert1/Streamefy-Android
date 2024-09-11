package com.streamefy.component.ui.dynamicview

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.databinding.FragmentDynamicPlayerBinding
import com.streamefy.utils.ExpirationUtil
import com.streamefy.utils.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DynamicPlayer : BaseFragment<FragmentDynamicPlayerBinding>() {
    override fun bindView(): Int = R.layout.fragment_dynamic_player
    var videoUrl =""
       // "https://iframe.mediadelivery.net/play/292623/06a93993-df8b-44c5-bf95-24d107ff5a95"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val webSettings = webview.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
//            webSettings.userAgentString =
//                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"

            // Set a WebViewClient to handle navigation within the WebView
            webview.webViewClient = WebViewClient()

            val tokenSecurityKey = "6cbb39d2-d451-4393-a3da-e4d33e5090f7"
            val videoId = "06a93993-df8b-44c5-bf95-24d107ff5a95"
            val expirationTimestamp = HashUtil.getExpirationTimestamp()

            // Generate hash
            val hash = HashUtil.generateHash(tokenSecurityKey, videoId, expirationTimestamp)


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

    fun iframe(){
        val url = "https://iframe.mediadelivery.net/embed/292623/06a93993-df8b-44c5-bf95-24d107ff5a95?token=2792dd697ee4b6184c421f65e99ded4a910692f1c84b401b71e6a857e4ce066c&expires=1726143915&autoplay=true&loop=false&muted=false&preload=true&responsive=true"

        // Load the URL into the WebView
        binding.webview.loadUrl(url)
    }


}