package com.streamefy.component.ui.home.background


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.ResizeMode
import com.google.android.exoplayer2.ui.PlayerView
import com.streamefy.R
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.HomeFragment.Companion.homeFragment
import com.streamefy.component.ui.home.model.BackgroundMediaItem
import com.streamefy.component.ui.video.PlayerHandler
import com.streamefy.utils.gone
import com.streamefy.utils.loadPicaso
import com.streamefy.utils.loadUrl
import com.streamefy.utils.viewAnimate
import com.streamefy.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Objects
import kotlin.coroutines.coroutineContext


class VideoRecyclerView : RecyclerView {
    enum class VolumeState {
        ON, OFF
    }

    // ui
    private var thumbnail: ImageView? = null
    private var viewHolderParent: View? = null
    private var tvTitle: TextView? = null
    private var frameLayout: ConstraintLayout? = null
    private var clParant: ConstraintLayout? = null
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
        videoSurfaceView?.useController = false
//        videoSurfaceView?.resizeMode= RESIZE_MODE_FIT
        addOnScrollListener(object : OnScrollListener() {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.e("scsoddsvs", "onScrollStateChanged $currentVideo target $targetPosition")

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
                // Log.e("scsoddsvs", "is frst $currentVideo target $targetPosition")

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
                        homeFragment.apply {
                            toolsCount = 0
                            showTools()
                        }
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
                                thumbHide()

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
                Log.e("skcjnakjbc", "by home fragment kjcdabcv $error")
                if (error.cause is MediaCodecRenderer.DecoderInitializationException) {
                    // Handle decoder initialization failure
                    Log.e("ExoPlayerError", "by home fragment Decoder initialization failed: ${error.message}")
                }
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
//            removeVideoView(videoSurfaceView)
            scrollPlay {}
//            homeFragment.binding.rvBackgVideo.scrollTo(pos,mNewPos)
        } else {
            //  recyclerview?.scrollToPosition(0)
            homeFragment.binding.rvBackgVideo.smoothScrollToPosition(0)
        }
    }

    fun backScroll(pos: Int) {
        Log.e("smcskmc", "back $pos new $pos size ${mediaObjects.size}")
        homeFragment.binding.rvBackgVideo.smoothScrollToPosition(pos)
        removeVideoView(videoSurfaceView)


    }

    var targetPosition = 0
    var currentPose = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun play(callBack: (String) -> Unit) {
        thumbShow()
        playerHandler.stopHandler()
        homeFragment.apply {
            toolsCount = 0
            showTools()
        }
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
        Log.e("skncksnc", "skck surface $playPosition")

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
        clParant = holder.binding.clParant
        holder.binding.apply {
            viewHolderParent = holder.itemView
            frameLayout = videoContainer
        }

        Log.e(
            "skcnsknc",
            "second video $targetPosition thumb ${mediaObjects[targetPosition].thumbnailSBucketId}"
        )
        if (mediaObjects[targetPosition].thumbnailSBucketId.isNotEmpty()) {
//            thumbnail?.loadPicaso(mediaObjects[targetPosition].thumbnailSBucketId)
            thumbnail?.loadUrl(mediaObjects[targetPosition].thumbnailSBucketId)
        }

        homeFragment.mediaUrl = mediaObjects[targetPosition].hlsPlaylistUrl
        currentVideo = targetPosition
        player = playerHandler.getPLayer()!!
        Log.e(
            "skncksnc",
            "skcks thumb ${mediaObjects[targetPosition].thumbnailSBucketId}\n video ${homeFragment.mediaUrl}"
        )
        if (homeFragment.mediaUrl.isNotEmpty()) {
            playerHandler.setMediaUri(homeFragment.mediaUrl, 0)
        } else {
            pauseVideo()
        }
        //playerHandler.mute()
    }

    fun thumbShow() {
        thumbnail?.visible()
        videoSurfaceView?.visible()
        thumbnail?.run {
            visible()
            animate()
                .alpha(1f) // Animate to full visibility
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(DecelerateInterpolator()) // Smooth transition
                .setDuration(20) // Duration of the animation
                .withEndAction {
                    videoSurfaceView?.run {
                        gone()
                        animate()
                            .alpha(0f) // Animate to full visibility
                            .scaleX(1f)
                            .scaleY(1f)
                            .setInterpolator(DecelerateInterpolator()) // Smooth transition
                            .setDuration(20) // Duration of the animation
                            .start()
                    }
                }
                .start()
        }

    }

    fun thumbHide() {

        videoSurfaceView?.run {
            alpha = 0f
            visible()
            animate()
                .alpha(1f) // Animate to full visibility
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(DecelerateInterpolator()) // Smooth transition
                .setDuration(50) // Duration of the animation
                .start()
        }

        thumbnail?.run {
            animate()
                .alpha(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(1500)
                .withEndAction {

                }
                .start()
        }


    }

    private fun updateAll() {
        //   CoroutineScope(Dispatchers.IO).launch {
        //  delay(200)
        val lastVisibleItemPosition =
            (layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition() ?: return

        mediaObjects.forEachIndexed { index, backgroundMediaItem ->
            val currentPosition =
                index - (layoutManager as LinearLayoutManager)?.findFirstVisibleItemPosition()!!
                    ?: return
            val child = getChildAt(currentPosition) ?: return
            val holder = child.tag as? BackgroundHolder

            if (index == lastVisibleItemPosition) {
                // Show title for the last visible item
                Log.e("sjncsjk", "Showing last visible item at position: $currentPosition")
            } else {
                // Hide titles for all other items
                Log.e("sjncsjk", "Hiding item at position: $currentPosition")
            }
        }


    }

    fun scrollPlay(callBack: (String) -> Unit) {
        pauseVideo()
        Log.e("skncksnc", "scrollPlay surface $targetPosition")

        removeVideoView(videoSurfaceView)

        val child = getChildAt(targetPosition) ?: return
        holder = child.tag as BackgroundHolder
        if (holder == null) {
            playPosition = -1
            return
        }
        thumbnail = holder.binding.imageView
        holder.binding.apply {
            viewHolderParent = holder.itemView
            frameLayout = videoContainer
        }
        homeFragment.mediaUrl = mediaObjects[targetPosition].hlsPlaylistUrl
        currentVideo = targetPosition
        player = playerHandler.getPLayer()!!
        Log.e("skncksnc", "skcks ${homeFragment.mediaUrl}")
        if (homeFragment.mediaUrl.isNotEmpty()) {
            playerHandler.setMediaUri(homeFragment.mediaUrl, 0)
        } else {
            pauseVideo()
        }

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

        homeFragment.apply {
            toolsCount += 500
            Log.e("skmcks", "sbjcbs $toolsCount")
            if (toolsCount == 5000L) {
                hideTools()
            }
        }
//        Log.e("updates", " duration $position")
        if (position >= 30000) {
            homeFragment?.run {
                if (targetPosition == mediaObjects.size - 1) {
                    binding.rvBackgVideo.smoothScrollToPosition(0)
                    scrollPlay{}
                } else {
                    binding.rvBackgVideo.smoothScrollToPosition(targetPosition + 1)
                    scrollPlay{}
                }
            }
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
            // frameLayout!!.viewAnimate()
            videoSurfaceView!!.viewAnimate()
            //  thumbnail?.goneAnimate()

            homeFragment.apply {
                // focusView = StreamEnum.INDECATOR_VIEW
//                binding.rvCategory.clearFocus()
                Log.e("testingdhfht", "$eventFocusPos dmvdmv $focusView ")
                viewFocus()

//                if (focusView == StreamEnum.INDECATOR_VIEW) {
//                    binding.customIndicator.requestFocus()
//                } else if (focusView == StreamEnum.BACKGROUND_VIDEO) {
////                    binding.apply {
////                        tvPlay.requestFocus()
////                        tvPlay.setText("pause")
////                        tvPlay.setCompoundDrawablesWithIntrinsicBounds(
////                            ContextCompat.getDrawable(
////                                requireActivity(),
////                                R.drawable.ic_backg_pause
////                            ), null, null, null
////                        )
////                    }
//                } else {
//                    eventFocus()
//                }
            }
        }
    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView)
            playPosition = -1
            videoSurfaceView!!.visibility = View.INVISIBLE
            // thumbnail!!.visibility = View.GONE
        }
    }

    fun setList(
        mediaObjects: ArrayList<BackgroundMediaItem>,
    ) {
        this.mediaObjects = mediaObjects
    }
}


