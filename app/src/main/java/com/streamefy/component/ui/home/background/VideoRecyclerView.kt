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
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.HomeFragment.Companion.homeFragment
import com.streamefy.component.ui.home.model.BackgroundMediaItem
import com.streamefy.component.ui.video.PlayerHandler
import com.streamefy.data.SharedPref
import com.streamefy.utils.gone
import com.streamefy.utils.goneAnimate
import com.streamefy.utils.remoteKey
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

    // vars
    var mediaObjects = ArrayList<BackgroundMediaItem>()
    private var viewContext: Context? = null
    var playPosition = 0
    private var isVideoViewAdded = false
    lateinit var playerHandler: PlayerHandler

    // controlling playback state
    var volumeState: VolumeState? = null
    var recyclerview: VideoRecyclerView? = null

    lateinit var holder: BackgroundHolder
    var currentVideo = 0
    lateinit var player: Player
    var isfirst = true


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
        videoSurfaceView = PlayerView(viewContext!!)
        playerHandler = PlayerHandler(viewContext!!, videoSurfaceView!!)

        // Bind the player to the view.
        player = playerHandler.getPLayer()!!
        videoSurfaceView?.useController=false
        addOnScrollListener(object : OnScrollListener() {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollHorizontally(1)) {
                        if (mediaObjects.size > 1) {
                            play() {}
                        }
                    } else {
                        play() {}
                    }
                }
            }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isfirst) {
                    Log.e("scsoddsvs", "is frst ")
                    play() {}
                    isfirst = false
                }
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

        // add listener for player
        videoSurfaceView!!.player?.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.e("jnjddc", " STATE_BUFFERING")
                    }

                    Player.STATE_ENDED -> {
                        Log.e("jnjddc", " STATE_ENDED")
                       // playerHandler.release()
                        scrollMe(currentVideo)
                    }

                    Player.STATE_IDLE -> {
                        Log.e("jnjddc", " STATE_IDLE")

                    }

                    Player.STATE_READY -> {
                        Log.e("jnjddc", " STATE_READY $isVideoViewAdded")

                        if (!isVideoViewAdded) {
                            try {
                                addVideoView()
                                updateDuration()
                            } catch (e: Exception) {
                                Log.e("jnjddc", " exception $e")
                            }
                        }

                    }

                    else -> {
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.e("skcjnakjbc", "kjcdabcv $error")
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
            }

        })

    }

    fun initializer() {
        // pauseVideo()
        targetPosition =
            (recyclerview?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        currentPose = targetPosition
        Log.e("sggdgdg", "targetPosition $targetPosition size ${mediaObjects.size}")
        if (mediaObjects.size == 0) {
            return
        }
        if (targetPosition == -1) {
            return
        }
        if (videoSurfaceView == null) {
            return
        }
        Log.e("sggdgdg", "skck surface")

        removeVideoView(videoSurfaceView)

        val currentPosition =
            targetPosition - (Objects.requireNonNull(layoutManager) as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return
        holder = child.tag as BackgroundHolder
        if (holder == null) {
            playPosition = -1
            return
        }
        thumbnail = holder.binding.imageView
        holder.binding.videoview.gone()
        holder.binding.apply {
            viewHolderParent = holder.itemView
            frameLayout = videoContainer
        }
        playerHandler = PlayerHandler(viewContext!!, videoSurfaceView!!)
        player = playerHandler.getPLayer()!!
        if (homeFragment.mediaUrl.isEmpty()) {
            homeFragment.mediaUrl = mediaObjects[targetPosition].hlsPlaylistUrl
        }

//        playerHandler.seekWithInitialise(homeFragment.mediaUrl, homeFragment.currentVideoDuration)
        resumeVideo(homeFragment.currentVideoDuration)
        //  playerHandler = holder.playerHandler
    }

    fun scrollMe(pos: Int) {
        var mNewPos = pos + 1
        Log.e("smcskmc", "up $pos new $mNewPos size ${mediaObjects.size}")
        if (mNewPos < mediaObjects.size) {
            homeFragment.binding.rvBackgVideo.smoothScrollToPosition(mNewPos)
            scrollPlay {}
//            homeFragment.binding.rvBackgVideo.scrollTo(pos,mNewPos)
        } else {
          //  recyclerview?.scrollToPosition(0)
            homeFragment.binding.rvBackgVideo.smoothScrollToPosition(0)
        }
    }

    fun backScroll(pos: Int) {
        var mNewPos = 2
        Log.e("smcskmc", "back $pos new $mNewPos size ${mediaObjects.size}")
        if (mNewPos > 0) {
            recyclerview?.scrollToPosition(mNewPos)
            scrollPlay {}
        }
    }

    var targetPosition = 0
    var currentPose = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun play(callBack: (String) -> Unit) {

       // pauseVideo()
        targetPosition =
            (recyclerview?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        currentPose = targetPosition
        Log.e("skncksnc", "targetPosition $targetPosition size ${mediaObjects.size}")
        if (mediaObjects.size == 0) {
            return
        }
        if (targetPosition == -1) {
            return
        }
        if (videoSurfaceView == null) {
            return
        }
        Log.e("skncksnc", "skck surface")

        removeVideoView(videoSurfaceView)

        val currentPosition =
            targetPosition - (Objects.requireNonNull(layoutManager) as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return
        holder = child.tag as BackgroundHolder
        if (holder == null) {
            playPosition = -1
            return
        }
        thumbnail = holder.binding.imageView
        holder.binding.videoview.gone()
        holder.binding.apply {
            viewHolderParent = holder.itemView
            frameLayout = videoContainer
        }
        homeFragment.mediaUrl = mediaObjects[targetPosition].hlsPlaylistUrl
        currentVideo = targetPosition
        var videoDuration = homeFragment.currentVideoDuration

//        videoSurfaceView!!.player = playerHandler.getPLayer()
        player = playerHandler.getPLayer()!!
        Log.e("skncksnc", "skcks ${homeFragment.mediaUrl}")
        if (homeFragment.mediaUrl.isNotEmpty()) {
            playerHandler.setMediaUri(homeFragment.mediaUrl,0)
//            playerHandler.seekWithInitialise(mediaUrl,videoDuration)
            // thumbnail?.gone()
        } else {
            pauseVideo()
        }
        //playerHandler.mute()
//        homeFragment.binding.rvCategory.requestFocus()
    }

    fun scrollPlay(callBack: (String) -> Unit) {
        pauseVideo()
        Log.e("skncksnc", "scrollPlay surface $targetPosition")

        removeVideoView(videoSurfaceView)

//        val currentPosition =
//            targetPosition - (Objects.requireNonNull(layoutManager) as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(targetPosition) ?: return
        holder = child.tag as BackgroundHolder
        if (holder == null) {
            playPosition = -1
            return
        }
        thumbnail = holder.binding.imageView
        holder.binding.videoview.gone()
        holder.binding.apply {
            viewHolderParent = holder.itemView
            frameLayout = videoContainer
        }
        homeFragment.mediaUrl = mediaObjects[targetPosition].hlsPlaylistUrl
        currentVideo = targetPosition
        var videoDuration = homeFragment.currentVideoDuration
        player = playerHandler.getPLayer()!!
        Log.e("skncksnc", "skcks ${homeFragment.mediaUrl}")
        if (homeFragment.mediaUrl.isNotEmpty()) {
            playerHandler.setMediaUri(homeFragment.mediaUrl,0)
        } else {
            pauseVideo()
        }
        //  playerHandler.mute()
        // homeFragment.binding.rvBackgVideo.requestFocus()

    }

    fun pauseVideo() {
        if (::playerHandler.isInitialized)
            playerHandler.pause()
        homeFragment.currentVideoDuration = player.currentPosition
    }

    private fun updateDuration() {
        val position = player.currentPosition
        homeFragment.currentVideoDuration = position
        if (player.playWhenReady) {
            playerHandler.handler.postDelayed({ updateDuration() }, 500)
        } else {
            playerHandler.stopHandler()
        }
    }

    fun resumeVideo(pos: Long) {
        Log.e("bxbxb", "$pos scmlcm ${homeFragment.mediaUrl}")
//        playerHandler.seekWithInitialise(homeFragment.mediaUrl, pos)
        playerHandler.seekTo(pos)
    }

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

    //
    private fun addVideoView() {
        if (videoSurfaceView != null) {
            frameLayout!!.addView(videoSurfaceView)
            isVideoViewAdded = true
            videoSurfaceView!!.requestFocus()
            frameLayout!!.viewAnimate()
            videoSurfaceView!!.viewAnimate()
            thumbnail?.goneAnimate()

//            videoSurfaceView?.isFocusable = false
//            videoSurfaceView?.isFocusableInTouchMode = false
            homeFragment.apply {
                // focusView = StreamEnum.INDECATOR_VIEW
                binding.rvCategory.clearFocus()
                if (focusView == StreamEnum.INDECATOR_VIEW) {
                    binding.customIndicator.requestFocus()
                } else {
                    eventFocus()
                }
            }
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

    fun setList(
        mediaObjects: ArrayList<BackgroundMediaItem>,
    ) {
        this.mediaObjects = mediaObjects
    }
}


