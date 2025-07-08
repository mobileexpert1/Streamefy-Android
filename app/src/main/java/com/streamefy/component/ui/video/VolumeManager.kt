import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

class VolumeManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
    private var maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
    private val handler = Handler(Looper.getMainLooper())
    private val volumeCheckRunnable = object : Runnable {
        override fun run() {
            checkVolume()
            handler.postDelayed(this, 1000) // Poll every second
        }
    }

    // Listener for volume changes
    private var volumeChangeListener: ((Int) -> Unit)? = null
    private var audioStatusListener: ((Boolean) -> Unit)? = null

    fun setOnVolumeChangeListener(listener: (Int) -> Unit) {

        this.volumeChangeListener = listener
    }

    fun startMonitoring() {
        handler.post(volumeCheckRunnable)
    }

    fun stopMonitoring() {
        handler.removeCallbacks(volumeCheckRunnable)
    }

    private fun checkVolume() {
        val newVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        if (newVolume != currentVolume) {
//            val volumePercentage = (newVolume / maxVolume * 100).toInt()
            val volumePercentage = (newVolume / maxVolume * 100).toInt()
            // Notify listeners of the volume change
            onVolumeChanged(volumePercentage, audioManager.isMusicActive)
            currentVolume = newVolume
        }

        // Check if audio is playing and the volume is non-zero
       // Log.e("ksmcksnc","audio  $currentVolume  music ${audioManager.isMusicActive}")

       // Log.e("hdhhdhds", "newVolume $newVolume gdgrgr $currentVolume   volumePercentage " )
    }

    private fun onVolumeChanged(volumePercentage: Int,isAudioActive: Boolean) {
        // Call the listener with the new volume percentage
      //  Log.e("VolumeControl", "$volumePercentage ")

        volumeChangeListener?.invoke(volumePercentage)
        audioStatusListener?.invoke(isAudioActive)
    }

    fun setVolumePercentage(percentage: Int) {
//        val volume = (percentage / 100f * maxVolume).toInt()
//
//        Log.e("hdhhdhds", "percentage $percentage gdgrgr $volume" )
//
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)


        val clampedPercentage = percentage.coerceIn(0, 100)
        var maxvolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volume = (clampedPercentage / 100f * maxvolume).toInt()
       // Log.e("hdhhdhds", "Percentage: $clampedPercentage, Volume: $volume, Max Volume: $maxVolume")

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

    }
    fun setOnAudioStatusListener(listener: (Boolean) -> Unit) {
        this.audioStatusListener = listener
    }



    private fun isStreamMuted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For API level 23 and above, use isStreamMute
            audioManager.isStreamMute(AudioManager.STREAM_MUSIC)
        } else {
            // For API level 21 and 22, use volume check as fallback
            currentVolume == 0f
        }
    }
}
