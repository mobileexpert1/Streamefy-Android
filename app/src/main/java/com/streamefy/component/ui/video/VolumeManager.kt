import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log

class VolumeManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
    private val handler = Handler(Looper.getMainLooper())
    private val volumeCheckRunnable = object : Runnable {
        override fun run() {
            checkVolume()
            handler.postDelayed(this, 1000) // Poll every second
        }
    }

    // Listener for volume changes
    private var volumeChangeListener: ((Int) -> Unit)? = null

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
            val volumePercentage = (newVolume / maxVolume * 100).toInt()
            // Notify listeners of the volume change
            onVolumeChanged(volumePercentage)
            currentVolume = newVolume
        }

     //   Log.e("hdhhdhds", "newVolume $newVolume gdgrgr $currentVolume   volumePercentage" )
    }

    private fun onVolumeChanged(volumePercentage: Int) {
        // Call the listener with the new volume percentage
        volumeChangeListener?.invoke(volumePercentage)
    }

    fun setVolumePercentage(percentage: Int) {
        val volume = (percentage / 100f * maxVolume).toInt()

        Log.e("hdhhdhds", "percentage $percentage gdgrgr $volume" )

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }
}
