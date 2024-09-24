package com.streamefy.component.ui.home.background


import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.streamefy.component.ui.home.model.BackgroundMediaItem
import com.streamefy.component.ui.video.PlayerHandler
import com.streamefy.data.SharedPref
import com.streamefy.utils.gone
import com.streamefy.utils.goneAnimate
import com.streamefy.utils.viewAnimate
import java.util.Objects


class VideoRecyclerView : RecyclerView {
    enum class VolumeState {
        ON, OFF
    }

    // ui
    private var thumbnail: ImageView? = null
    private var viewHolderParent: View? = null
    private var frameLayout: ConstraintLayout? = null
    private var videoSurfaceView: PlayerView? = null
    var videoPlayer: SimpleExoPlayer? = null

    // vars
    var mediaObjects = ArrayList<BackgroundMediaItem>()
    private var viewContext: Context? = null
    var playPosition = 0
    private var isVideoViewAdded = false
    private var postTypeId: String = ""
    var subType: String = ""
    lateinit var playerHandler: PlayerHandler

    // controlling playback state
    var volumeState: VolumeState? = null
    private var recyclerview: RecyclerView? = null

    lateinit var holder: BackgroundHolder
    var mediaUrl = ""


    constructor(context: Context) : super(context) {
        init(context)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        // Custom measure logic if necessary
        super.onMeasure(widthSpec, heightSpec)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        this.viewContext = context.applicationContext
        recyclerview = this
      //  videoSurfaceView = PlayerView(viewContext!!)
       // playerHandler = PlayerHandler(viewContext!!, videoSurfaceView!!)

//        videoPlayer = ExoplayerHandler.initPlayer(viewContext!!)
        // Bind the player to the view.
     //   videoSurfaceView!!.useController = false
//videoSurfaceView!!.player = videoPlayer

        addOnScrollListener(object : OnScrollListener() {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    Log.e("skncksnc","skcks ${mediaObjects.size} data $mediaObjects")
                    if (!recyclerView.canScrollHorizontally(1)) {
                        if (mediaObjects.size > 1) {
                            playTest(true) {}
                        }
                    } else {
                        playTest(false) {}
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            }
        })

        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent != null && viewHolderParent == view) {
                    resetVideoView()
                }
            }
        })
        //// item styles
        addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: State
            ) {


            }
        })
        /// for snap the view left-right and vise-versa
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(this)
    }

    //// update seekBar while playing the video

    var currentPose = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun playTest(isEndOfList: Boolean, callBack: (String) -> Unit) {
//        viewInitialize{}
        Log.e("skncksnc","playTest")
     //   pauseVideo()
        targetPosition =
            (recyclerview?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        currentPose = targetPosition
        Log.e("skncksnc","targetPosition $targetPosition size ${mediaObjects.size}")
        if (mediaObjects.size == 0) {
            return
        }
        if (targetPosition == -1) {
            return
        }
//        if (videoSurfaceView == null) {
//            return
//        }
        Log.e("skncksnc","skck surface")

        // if (subType == "video") {
            removeVideoView(videoSurfaceView)
      //  }

        val currentPosition =
            targetPosition - (Objects.requireNonNull(layoutManager) as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return
        holder = child.tag as BackgroundHolder
        if (holder == null) {
            playPosition = -1
            return
        }
        thumbnail = holder.binding.imageView
        playerHandler = PlayerHandler(viewContext!!, holder.binding.videoview)
// save next video in cache
        holder.binding.apply {
            // progressBar = progressbar
            viewHolderParent = holder.itemView
            frameLayout = videoContainer
        }
        mediaUrl=mediaObjects[targetPosition].hlsPlaylistUrl


        Log.e("skncksnc","skcks $mediaUrl")
        if (mediaUrl.isNotEmpty()) {
            playerHandler.setMediaUri(mediaUrl)
            thumbnail?.gone()
        } else {
            pauseVideo()
        }
        callBack.invoke(subType)
    }

    //*** load answer video


    var targetPosition = 0
    fun viewInitialize(callBack: (String) -> Unit) {
        pauseVideo()
        targetPosition =
            (recyclerview?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        currentPose = targetPosition

        if (mediaObjects.size == 0) {
            return
        }
        if (targetPosition == -1) {
            return
        }
        if (videoSurfaceView == null) {
            return
        }

        //    var mediaUrl = ""


        //  videoSurfaceView!!.invisible()
       // if (subType == "video") {
           // removeVideoView(videoSurfaceView)
      //  }

        val currentPosition =
            targetPosition - (Objects.requireNonNull(layoutManager) as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return
        holder = child.tag as BackgroundHolder
        if (holder == null) {
            playPosition = -1
            return
        }
        thumbnail = holder.binding.imageView
        playerHandler = PlayerHandler(viewContext!!, holder.binding.videoview)
// save next video in cache
        holder.binding.apply {
            // progressBar = progressbar
            viewHolderParent = holder.itemView
            frameLayout = videoContainer
        }
        mediaUrl=mediaObjects[targetPosition].hlsPlaylistUrl
        Log.e("tstinghhj", "sncjsnjs $subType $mediaUrl")

        callBack.invoke(subType)
    }


    fun playPause() {
        if (videoPlayer != null) {
            if (videoPlayer?.isPlaying!!) {
//                ExoplayerHandler.pauseVideo()
            } else {
//                ExoplayerHandler.resume()
            }
        }
    }


    fun volumeUpdates(callBack: (VolumeState) -> Unit) {
        toggleVolume()
        volumeState?.let { callBack.invoke(it) }

    }

    fun pauseVideo() {
        playerHandler.pause()
//        ExoplayerHandler.pauseVideo()
    }


    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     * @param playPosition
     * @return
     */


    // Remove the old player
    private fun removeVideoView(videoView: PlayerView?) {
        if (videoView?.parent == null) {
            return
        }
        val parent = videoView.parent as ViewGroup
        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
            viewHolderParent!!.setOnClickListener(null)
        }
    }

    private fun addVideoView() {
        if (videoSurfaceView != null) {
            frameLayout!!.addView(videoSurfaceView)
            isVideoViewAdded = true
            videoSurfaceView!!.requestFocus()
            frameLayout!!.viewAnimate()
            videoSurfaceView!!.viewAnimate()
//            videoSurfaceView!!.alpha = 1f
            // thumbnail!!.visibility = View.GONE
            thumbnail?.goneAnimate()
        }
    }


    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView)
            playPosition = -1
            videoSurfaceView!!.visibility = View.INVISIBLE
            thumbnail!!.visibility = View.GONE
        }
    }

    fun releasePlayer() {
//        ExoplayerHandler.releasePlayer()
        viewHolderParent = null
    }

//    fun volumeVisi() {
//        if (videoPlayer != null) {
//            if (volumeState == VolumeState.OFF) {
//                volumeControl?.setImageResource(R.drawable.ic_volume_off_grey_24dp)
//            } else if (volumeState == VolumeState.ON) {
//                volumeControl?.setImageResource(R.drawable.ic_volume_up_grey_24dp)
//            }
//        }
//    }


//    @RequiresApi(Build.VERSION_CODES.O)
//    fun setVolume(context: Context) {
//        // Initialize the ContentObserver
//        Log.e("smcklsamc","kackdsnc")
//        volumeObserver = object : ContentObserver(Handler()) {
//            override fun onChange(selfChange: Boolean) {
//
//                Log.e("smcklsamc","kackdsnc $selfChange")
//                super.onChange(selfChange)
//                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
//                if (currentVolume > 0) {
//                    Log.d("Volume Controller", "Volume turned up")
//                    setVolumeControl(VolumeState.ON)
//                   // requestManager!!.load(R.drawable.ic_volume_up_grey_24dp).into(volumeControl!!)
//                } else {
//                    Log.d("Volume Controller", "Volume turned down or muted")
//                    setVolumeControl(VolumeState.OFF)
//                   // requestManager!!.load(R.drawable.ic_volume_off_grey_24dp).into(volumeControl!!)
//                }
//            }
//        }


//        volumeObserver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                Log.e("smcklsamc","broadcast")
//
//                if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
//                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
//                    if (currentVolume > 0) {
//                        Log.d("Volume Controller", "Volume turned up")
//                        setVolumeControl(VolumeState.ON)
////                        requestManager.load(R.drawable.ic_volume_up_grey_24dp).into(volumeControl)
//                    } else {
//                        Log.d("Volume Controller", "Volume turned down or muted")
//                        setVolumeControl(VolumeState.OFF)
////                        requestManager.load(R.drawable.ic_volume_off_grey_24dp).into(volumeControl)
//                    }
//                }
//            }
//        }
//
//        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
//        viewContext!!.registerReceiver(volumeObserver, filter)
        // Register the observer
//        val resolver = viewContext!!.contentResolver
//        val volumeUri = android.provider.Settings.System.CONTENT_URI
//        resolver.registerContentObserver(volumeUri, true, volumeObserver!!)



    private fun toggleVolume() {
      //  if (videoPlayer != null) {
            if (volumeState == VolumeState.OFF) {
                setVolumeControl(VolumeState.ON)
            } else if (volumeState == VolumeState.ON) {
                setVolumeControl(VolumeState.OFF)
            }
       // }
    }

    var isVolumeStateChanged = false

    //// volume control
    private fun setVolumeControl(state: VolumeState) {
        volumeState = state
        ///save volume state
      //  if (videoPlayer != null) {
            if (state == VolumeState.OFF) {
                videoPlayer!!.volume = 0f
//                pref?.setString(PrefConstents.volumeState, "0f")
            } else if (state == VolumeState.ON) {
                videoPlayer!!.volume = 1f
//                pref?.setString(PrefConstents.volumeState, "1f")
            }
            animateVolumeControl() {}
      //  }

    }

    ///// volume visibility
    fun animateVolumeControl(callback: (VolumeState?) -> Unit) {
//        if (volumeControl != null) {
//            volumeControl!!.bringToFront()
//            isVolumeStateChanged = false
//            callback.invoke(volumeState)
//        }
    }

    fun setList(
        mediaObjects: ArrayList<BackgroundMediaItem>,
    ) {
        this.mediaObjects = mediaObjects
    }

    var responseType = 0

}


