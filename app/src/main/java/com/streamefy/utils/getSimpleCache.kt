package com.streamefy.utils
import android.content.Context
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import java.io.File
var simpleCache: SimpleCache? = null
fun getSimpleCache(context: Context): SimpleCache {

    if (simpleCache == null) {
        // Create cache only once, and reuse it throughout the app
        val cacheDir = File(context.cacheDir, "exoplayer_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024) // 100MB
        simpleCache = SimpleCache(cacheDir, evictor)
    }
    return simpleCache!!
}