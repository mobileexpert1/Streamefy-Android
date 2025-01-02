package com.streamefy.utils

import android.media.MediaDataSource
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

@RequiresApi(Build.VERSION_CODES.M)
class StreamingMediaDataSource(private val url: String) : MediaDataSource() {
    private var inputStream: InputStream? = null
    private var connection: HttpURLConnection? = null

    init {
        openConnection()
    }

    private fun openConnection() {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.requestMethod = "GET"
        urlConnection.connect()
        inputStream = urlConnection.inputStream
        connection = urlConnection
    }

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        inputStream?.let {
            it.skip(position) // Skip to the requested position
            return it.read(buffer, offset, size) // Read data into the buffer
        }
        return -1 // End of stream or error
    }

    override fun getSize(): Long {
        return connection?.contentLength?.toLong() ?: -1 // Return the size of the content
    }

    override fun close() {
        inputStream?.close()
        connection?.disconnect()
    }
}