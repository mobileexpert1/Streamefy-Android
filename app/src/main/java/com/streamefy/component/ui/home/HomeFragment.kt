package com.streamefy.component.ui.home

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.CircularProgressDialog
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.adapter.CategoryAdapter
import com.streamefy.component.ui.home.adapter.CreatorsAdapter
import com.streamefy.component.ui.home.adapter.DrawerAdapter
import com.streamefy.component.ui.home.model.BackgroundMediaItem
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.component.ui.home.model.MediaItem
import com.streamefy.component.ui.home.model.crewMembers
import com.streamefy.component.ui.home.viewmodel.HomeVm
import com.streamefy.component.ui.video.PlayerHandler
import com.streamefy.component.ui.video.VideoEnum
import com.streamefy.component.ui.video.model.PlayBackRequest
import com.streamefy.component.ui.video.model.QualityModel
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentHomeBinding
import com.streamefy.network.MyResource
import com.streamefy.utils.convertToMillis
import com.streamefy.utils.customAlfa
import com.streamefy.utils.gone
import com.streamefy.utils.invisible
import com.streamefy.utils.remoteKey
import com.streamefy.utils.transition
import com.streamefy.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.floor

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun bindView(): Int = R.layout.fragment_home
    private val viewModel: HomeVm by viewModel()
    lateinit var playerHandler: PlayerHandler

    val images = ArrayList<BackgroundMediaItem>()
    val crewList = ArrayList<crewMembers>()
    var selectedTitle = ""
    var isMenuOpened = false
    var auth_pin = "Z1U5"
    var phone = ""
    var page = 1
    var eventFocusPos = 0
    var drawerItemFocus = 0
    var proTitle = ""
    var proDesc = ""
    var proLogo = ""
    var projectId = "0"
    var videoPlayingIndex = 0

    private val eventList = ArrayList<EventsItem>()
    private val mediaList = ArrayList<MediaItem>()
    lateinit var eventAdapter: CategoryAdapter
    lateinit var mediaAdapter: DrawerAdapter
    lateinit var creatorsAdapter: CreatorsAdapter
    var currentVideoDuration: Long = 0
    var isFirst = true
    var mediaUrl = ""
    var isDrawerOpen = false
    var isFirstVideo = false
    var isEventPagination = false

    var focusView = StreamEnum.BOTTOM_EVENT_VIEW

    var isLastPlay = false
    var lastVideoUrl = ""
    var lastVideoDuration = "0"
    var lastVideoThumb = ""
    var isPlayByPlayButton = false
    var isPrimaryuser = false
    var transitionValue4 = 0f
    var transitionValue3 = 0f
    var transitionValue2 = 0f

    companion object {
        lateinit var homeFragment: HomeFragment
        var videoduraion: Long = 0
        var mediaId: Int = 0
        var eventId: Int = 0
        var videoId: String = ""
        var eventVideoIndex = 0
        var mediaIndex = 0
        var isTrailer = false
    }

    lateinit var progressDialog: CircularProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = CircularProgressDialog(requireContext())
        auth_pin = SharedPref.getString(PrefConstent.AUTH_PIN).toString()
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        isPrimaryuser = SharedPref.getBoolean(PrefConstent.ISPRIMARY_USER)
        projectId = SharedPref.getString(PrefConstent.PROJECT_ID).toString()
        homeFragment = this
        isEventPagination = false
        // screenHeight =getScreenHeight()
        if (isFirst) {
            showProgress()
            getUserData()
            //playbackObserver()
            playerHandler = PlayerHandler(requireActivity(), binding.playerView)
            transitionValue4 = dpToPx(140f)
            transitionValue3 = dpToPx(120f)
            transitionValue2 = dpToPx(85f)
           //
            playerInitialization()

        }

        eventView()
        creatorView()
        foucusView()
        keyMove()
        binding.apply {
//            ivLogout.setOnClickListener {
//                LogoutDialog(requireContext()) {
//                    SharedPref.clearData()
//                    val navOptions = NavOptions.Builder()
//                        .setPopUpTo(R.id.homefragment, true) // Set inclusive to true
//                        .build()
//                    // Navigate to home fragment with the options
//                    findNavController().navigate(R.id.loginFragment, null, navOptions)
//                }.show()
//            }

            ivClose.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.END)
                isMenuOpened = false
            }

            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    // Handle drawer slide if needed
                }

                override fun onDrawerOpened(drawerView: View) {
                    // Set focus to the first item if needed
                    Log.e("dndjvn", "drawer open")
                    focusView = StreamEnum.DRAWER_VIEW
                    isDrawerOpen = true
                    rvDrawer.post { rvDrawer.getChildAt(drawerItemFocus)?.requestFocus() }
                }

                override fun onDrawerClosed(drawerView: View) {
                    Log.e("dndjvn", "drawer close")
                    isDrawerOpen = false
                    if (isTrailer) {
                        ivTrailer.requestFocus()
                    } else {
                        eventVideoFocus()
                    }
                }

                override fun onDrawerStateChanged(newState: Int) {
                }
            })
            ivTrailer.setImageResource(R.drawable.ic_unselect_trailer)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Show the custom dialog when back is pressed
                    showCustomDialog()
                }
            })

    }

    fun keyMove() = with(binding) {
        ivHomeCross.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    tvPlay.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
//                    customIndicator.requestFocus()
//                    eventVideoFocus()
                    tvPlay.requestFocus()
                }

                StreamEnum.LEFT_DPAD_KEY -> {
//                    ivLogout.requestFocus()
                    tvPlay.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
//                    ivLogout.requestFocus()
                    tvPlay.requestFocus()
                }


                else -> {}
            }
        }
//        rvBackgVideo.remoteKey {
//            Log.e("dkvdknv", "ncjxcnbd backvideo")
//            when (it) {
//                StreamEnum.UP_DPAD_KEY -> {
//                    eventVideoFocus()
//                }
//
//                StreamEnum.DOWN_DPAD_KEY -> {
//                    tvPlay.requestFocus()
//                }
//
//                StreamEnum.LEFT_DPAD_KEY -> {
//                    // ivLogout.requestFocus()
//                }
//
//                StreamEnum.RIGHT_DPAD_KEY -> {
//                    // ivLogout.requestFocus()
//                }
//
//                else -> {}
//            }
//        }
//        customIndicator.remoteKey {
//            when (it) {
//                StreamEnum.UP_DPAD_KEY -> {
//
//                    if (rvBackgVideo.targetPosition == 0) {
//                        ivHomeCross.requestFocus()
//                    } else {
//                        val currenPos = rvBackgVideo.targetPosition - 1
//                        binding.rvBackgVideo.backScroll(currenPos)
//
//                    }
//                }
//
//                StreamEnum.DOWN_DPAD_KEY -> {
//                    if (rvBackgVideo.targetPosition == rvBackgVideo.mediaObjects.size - 1) {
////                        tvPlay.requestFocus()
//                    } else {
//                        val currenPos = rvBackgVideo.targetPosition + 1
//                        binding.rvBackgVideo.smoothScrollToPosition(currenPos)
//                    }
//                }
//
//                StreamEnum.LEFT_DPAD_KEY -> {
//                    tvPlay.requestFocus()
//                }
//
//                StreamEnum.RIGHT_DPAD_KEY -> {
//                }
//
//                else -> {}
//            }
//        }
        tvPlay.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    ivHomeCross.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    eventVideoFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
//                    customIndicator.requestFocus()
                    ivTrailer.requestFocus()
                }

                else -> {}
            }
        }
        ivTrailer.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
//                    ivLogout.requestFocus()
                    ivHomeCross.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    eventVideoFocus()
                }

                StreamEnum.LEFT_DPAD_KEY -> {
                    tvPlay.requestFocus()
                }

                else -> {}
            }
        }
    }

    private fun foucusView() = with(binding) {
//        ivLogout.remoteKey {
//            when (it) {
//                StreamEnum.UP_DPAD_KEY -> {
//                    eventVideoFocus()
//                }
//
//                StreamEnum.DOWN_DPAD_KEY -> {
//                    //  customIndicator.requestFocus()
//                    tvPlay.requestFocus()
//                }
//
//                StreamEnum.LEFT_DPAD_KEY -> {
//                    eventVideoFocus()
//                }
//
//                StreamEnum.RIGHT_DPAD_KEY -> {
//                    ivHomeCross.requestFocus()
//                }
//
//                else -> {}
//            }
//        }
//        ivLogout.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                focusView = StreamEnum.LOGOUT_VIEW
//                // Change size when focused
//                val params = ivLogout.layoutParams as ConstraintLayout.LayoutParams
//                params.width =
//                    resources.getDimensionPixelSize(R.dimen._20sdp) // Adjust to your desired size
//                params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
//                ivLogout.layoutParams = params
//
//            } else {
//                // Revert size when not focused
//                val params = ivLogout.layoutParams as ConstraintLayout.LayoutParams
//                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
//                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
//                ivLogout.layoutParams = params
//
//            }
//        }
        // ivTrailer.requestFocus()

        ivHomeCross.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusView = StreamEnum.HOME_CLOSE
                // Change size when focused
                val params = ivHomeCross.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._20sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                ivHomeCross.layoutParams = params
            } else {
                // Revert size when not focused
                val params = ivHomeCross.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._16sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
                ivHomeCross.layoutParams = params
            }
        }
        ivHomeCross.setOnClickListener {
            /// navigate to the Project screen
            val bundle = Bundle().apply {
                putBoolean(PrefConstent.ISHOME, true)
            }
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.homefragment, true) // Set inclusive to true
                .build()
            // Navigate to home fragment with the options
            if (isPrimaryuser) {
                findNavController().navigate(R.id.projectfragment, bundle, navOptions)
            } else {
                findNavController().navigate(R.id.pinAuthenticationFragment, bundle, navOptions)
            }

        }
        customIndicator.setOnFocusChangeListener { v, hasFocus ->
            Log.e("backgvideo", "skcsk $hasFocus")
            if (hasFocus) {
                focusView = StreamEnum.INDECATOR_VIEW
                customIndicator.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.gray))
            } else {
                customIndicator.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(), R.color._8b000000
                    )
                )
            }
        }
        // right drawer close
        ivClose.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusView = StreamEnum.DRAWER_VIEW
                // Change size when focused
                val params = ivClose.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._20sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                ivClose.layoutParams = params
            } else {
                // Revert size when not focused
                val params = ivClose.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivClose.layoutParams = params
            }
        }
        tvPlay.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusView = StreamEnum.PLAY_RESUME
            }
        }

        tvPlay.setOnClickListener {
            focusView = StreamEnum.PLAY_RESUME
            Log.e("lcsdwdw", "clicked ${tvPlay.text.toString()}")
            if (tvPlay.text.toString().contains("play")) {
                playEventVideo()
            } else {
                playEventVideo()
            }

        }
        ivTrailer.setOnFocusChangeListener { _, hasFocus ->
            Log.e("lcsdwdw", "scnsivn $hasFocus")
            if (hasFocus) {
                focusView = StreamEnum.TRAILER
                ivTrailer.setImageResource(R.drawable.ic_selected_trailer)
//                val params = ivTrailer.layoutParams as ConstraintLayout.LayoutParams
//                params.width =
//                    resources.getDimensionPixelSize(R.dimen._20sdp) // Adjust to your desired size
//                params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
//                ivTrailer.layoutParams = params
            } else {
//                val params = ivTrailer.layoutParams as ConstraintLayout.LayoutParams
//                params.width = resources.getDimensionPixelSize(R.dimen._17sdp) // Original size
//                params.height = resources.getDimensionPixelSize(R.dimen._17sdp)
//                ivTrailer.layoutParams = params
                ivTrailer.requestLayout()
                ivTrailer.invalidate()
                ivTrailer.setImageResource(R.drawable.ic_unselect_trailer)
            }
        }
        ivTrailer.setOnClickListener {
            isTrailer = true
            drawerLayout.openDrawer(GravityCompat.END)
            trailerDrawer()
        }

    }

    fun playEventVideo() {
        Log.e(
            "playresumesd",
            "play by button $isPlayByPlayButton last video data $isLastPlay $lastVideoDuration video url $lastVideoUrl thumb $lastVideoThumb"
        )
        isPlayByPlayButton = true
        toGotoVideo(
            lastVideoDuration,
            lastVideoThumb,
            videoId,
            mediaId
        )
    }

    private fun creatorView() = with(binding) {
        rvCreators.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            creatorsAdapter = CreatorsAdapter(requireActivity(), crewList) {}
            adapter = creatorsAdapter
        }
    }

    var toolsCount: Long = 0
    fun sliderInit() = with(binding) {

//        rvBackgVideo.apply {
//            gone()
//            alpha = 0f
//            setHasFixedSize(true)
//            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
//            setList(images)
//            var backgadapter = BackgroundAdpater(requireActivity(), images) { index -> }
//            adapter = backgadapter
//
//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                        var newPos =
//                            (rvBackgVideo.recyclerview?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//                        Log.e(
//                            "skncksnc",
//                            "current ${rvBackgVideo.targetPosition} new index $newPos skcks ${mediaObjects.size} "
//                        )
//                    }
//                }
//            })
//        }
        thumbShow()
        //  customIndicator.setIndicatorCount(images.size, 0)
//        if (images.size > 1) {
//            binding.customIndicator.visible()
//        }
    }

    fun thumbShow() = with(binding) {
//        rvBackgVideo.run {
//            visible()
//            animate()
//                .alpha(1f)
//                .scaleX(1f)
//                .scaleY(1f)
//                .setInterpolator(DecelerateInterpolator())
//                .setDuration(5000)
//                .start()
//        }

    }

    fun showTools() = with(binding) {
        clOpecity.visible()
        tvProjectDesc.visible()
        rvCreators.visible()
        tvProjectTitle.visible()

        tvProjectDesc.transition(transitionValue4, 0f)
        rvCreators.transition(transitionValue4, 0f)
        projectlogo.transition(transitionValue4, 0f)
        tvProjectTitle.transition(transitionValue4, 0f)

        tvProjectDesc.customAlfa(0f, 1f)
        rvCreators.customAlfa(0f, 1f)

    }

    fun dpToPx(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }

    fun hideTools() = with(binding) {
        if (rvCreators.isVisible) {
            tvProjectDesc.transition(0f, transitionValue4)
            rvCreators.transition(0f, transitionValue4)
            projectlogo.transition(0f, transitionValue4)
            tvProjectTitle.transition(0f, transitionValue4)
            tvProjectDesc.customAlfa(1f, 0f)
            rvCreators.customAlfa(1f, 0f)
        }
    }

    private fun getUserData() {
        dismissProgress()
        viewModel.getUserVideos(requireActivity(), page, 10, auth_pin, projectId.toInt(), phone)
        observe()
    }

    private fun showCustomDialog() {
        if (isDrawerOpen) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            //ExitDialog(requireActivity()).show()
            LogoutDialog(requireContext()) {
//                SharedPref.clearData()
                SharedPref.setBoolean(PrefConstent.ISLOGIN, false)
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.homefragment, true) // Set inclusive to true
                    .build()
                // Navigate to home fragment with the options
                findNavController().navigate(R.id.loginFragment, null, navOptions)
            }.show()
        }
    }

    private fun drawerView() = with(binding) {
        tvTitle.setText(selectedTitle)
        isTrailer = false
        focusView = StreamEnum.DRAWER_VIEW
        rvDrawer.apply {
            isTrailer = false
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            mediaAdapter = DrawerAdapter(requireActivity(), mediaList as ArrayList<Any>) {
                drawerItemFocus = it
                mediaIndex = it
                lifecycleScope.launch(Dispatchers.IO) {
                    mediaList[it].run {
                        if (this.playbackDuration.isNotEmpty()) {
                            val number: Double = this.playbackDuration.toDouble()
                            val intValue = floor(number).toInt()

                            var newDuration = intValue.toString()
                            var totalDuration: Long =
                                convertToMillis(this.totalVideoDuration)

//                            totalDuration = if (totalDuration > 10000L) {
//                                totalDuration - 11000
//                            } else {
//                                totalDuration - 4000
//                            }

                            if (newDuration.toLong() >= totalDuration) {
                                newDuration = "0"
                            }

                            mediaId = this.id
                            videoId = this.bunnyId
                            isPlayByPlayButton = false
                            withContext(Dispatchers.Main) {
                                toGotoVideo(
                                    newDuration,
                                    thumbnailS3bucketId,
                                    videoId,
                                    mediaId
                                )
                            }
                        }
                    }
                }
            }
            adapter = mediaAdapter
        }
    }

    private fun trailerDrawer() = with(binding) {
        tvTitle.setText("Trailer")
        focusView = StreamEnum.DRAWER_VIEW
        rvDrawer.apply {
            getChildAt(0)?.requestFocus()
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            mediaAdapter = DrawerAdapter(requireActivity(), images as ArrayList<Any>) {
                Log.e("sjncsjncb", "trailer video $it")
                lifecycleScope.launch(Dispatchers.IO) {
                    images[it].run {
                        var thumbnailS3bucketId = this.thumbnailSBucketId
                        var newvideoId = this.bunnyId
                        var newmediaId = this.id
                        withContext(Dispatchers.Main) {
                            toGotoVideo(
                                "0",
                                thumbnailS3bucketId,
                                newvideoId,
                                newmediaId
                            )
                        }

                    }
                }
            }
            adapter = mediaAdapter
        }
    }

    private fun eventView() = with(binding) {
        rvCategory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            eventAdapter = CategoryAdapter(requireActivity(), eventList) { pos, type ->
                selectedTitle = eventList[pos].eventTitle
                Log.e("feffefef", "fnsdsnfs $type ")
                eventFocusPos = pos
                eventVideoIndex = pos
                mediaIndex = 0
                when (type) {
                    StreamEnum.SINGLE -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (eventList[pos].media != null) {
                                if (eventList[pos].media?.size!! >= 1) {
                                    eventList[pos].media?.get(0)?.run {
                                        val number: Double = this.playbackDuration.toDouble()
                                        val intValue = floor(number).toInt()
                                        var newDuration = intValue.toString()

                                        var totalDuration: Long =
                                            convertToMillis(this.totalVideoDuration)
//                                        totalDuration = if (totalDuration > 10000L) {
//                                            totalDuration - 11000
//                                        } else {
//                                            totalDuration - 4000
//                                        }
                                        if (newDuration.toLong() >= totalDuration) {
                                            newDuration = "0"
                                        }
                                        isPlayByPlayButton = false
                                        mediaId = this.id
                                        videoId = this.bunnyId
                                        withContext(Dispatchers.Main) {
                                            toGotoVideo(
                                                newDuration,
                                                thumbnailS3bucketId,
                                                videoId,
                                                mediaId
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }

                    StreamEnum.MORE -> {
                        drawerLayout.openDrawer(GravityCompat.END)
                        eventList[pos].media?.run {
                            if (isNotEmpty()) {
                                mediaList.clear()
                                mediaList.addAll(eventList[pos].media as ArrayList<MediaItem>)
                                drawerView()
                                getChildAt(0)?.requestFocus()
                            }
                        }
                    }

                    StreamEnum.UP_DPAD_KEY -> {
                       // rvBackgVideo.clearFocus()
                        tvPlay.isFocusable = true
                        tvPlay.isFocusableInTouchMode = true
                        tvPlay.post {
                            tvPlay.requestFocus()
                        }

                    }

                    StreamEnum.PAGINATION -> {
                        if (eventList.size >= 10) {
                            eventVideosMore()
                        }
                    }

                    else -> {}
                }


                isMenuOpened = true
            }

            adapter = eventAdapter
        }
    }

    fun toGotoVideo(duration: String, thumb: String, bunneId: String, mediaId: Int) {
        val bundle = Bundle()
        bundle.putString(PrefConstent.PLAY_BACK_DURATION, duration)
        bundle.putString(PrefConstent.VIDEO_THUMB, thumb)
        bundle.putString(PrefConstent.VIDEO_ID, bunneId)
        bundle.putString(PrefConstent.MEDIA_ID, mediaId.toString())
        findNavController().navigate(R.id.videofragment, bundle)
    }

    fun eventVideosMore() {
        viewModel.getUserVideos(requireActivity(), page, 10, auth_pin, projectId.toInt(), phone)
        observe()
    }

    fun getScreenHeight(): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager =
            requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        // screenHeight = displayMetrics.heightPixels
        return displayMetrics.heightPixels
    }

    private fun observe() {
        viewModel._homeLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
//                    showProgress()
                    progressDialog.show()
                }

                is MyResource.isSuccess -> {
                    dismissProgress()
//                    showProgress()
                    Log.e(
                        "hfhddnub",
                        "$isEventPagination page $page pagination ${it.data?.data?.events?.size}" + it.data?.data.toString()
                    )
                    binding.ivHomeCross.visible()
                    it.data?.data?.run {
                        var data = this
                        if (isEventPagination) {
                            if (events != null && events.isNotEmpty()) {
                                eventAdapter.pagination(events as ArrayList<EventsItem>)
                                isEventPagination = true
                                page++
                            } else {
                                isEventPagination = false
                            }
                            dismissProgress()
                        } else {
                            page++
                            isEventPagination = true
                            eventList.clear()
                           var projectData=project?.get(0)
                            // event video
                            lifecycleScope.launch(Dispatchers.IO) {
                                data.backgroundMedia?.run {
                                    if (this.isNotEmpty()) {
                                        images.addAll(this as ArrayList<BackgroundMediaItem>)
                                        withContext(Dispatchers.Main) {
                                            //sliderInit()
                                            if (images.isNotEmpty()) {
                                                if (images[0].hlsPlaylistUrl.isNotEmpty()) {
                                                    play(images[0].hlsPlaylistUrl)
                                                    showRemainsTime(30000)
                                                    countDownTimer.start()
                                                    isTimerRunning = true
                                                }
                                            }

                                        }
                                    }
                                }
                                var found = false
                                if (events != null && events.size > 0) {
                                    var reverseList = events.reversed()
                                    for (i in 0 until reverseList.size) {
                                        var mediaReverseList = reverseList[i].media?.reversed()
                                        if (mediaReverseList != null) {
                                            for (j in 0 until mediaReverseList.size) {
                                                var media = mediaReverseList[j]
                                                if (media.isLastPlayed) {

                                                    lastVideoThumb = media.thumbnailS3bucketId
                                                    videoId = media.bunnyId
                                                    lastVideoUrl = ""
                                                    mediaId = media.id
                                                    isLastPlay = true
                                                    val number: Double =
                                                        media.playbackDuration.toDouble()
                                                    val intValue = floor(number).toInt()
                                                    lastVideoDuration = intValue.toString()

                                                    var totalDuration: Long =
                                                        convertToMillis(media.totalVideoDuration)

//                                                    totalDuration = if (totalDuration > 10000L) {
//                                                        totalDuration - 11000
//                                                    } else {
//                                                        totalDuration - 4000
//                                                    }

                                                    lastVideoDuration =
                                                        if (lastVideoDuration.toLong() >= totalDuration) {
                                                            "0"
                                                        } else {
                                                            media.playbackDuration
                                                        }

                                                    // Log the captured media details
                                                    Log.e(
                                                        "loglisrtss",
                                                        "id ${media.id} Duration: $lastVideoDuration, Thumb: $lastVideoThumb, VideoID: $videoId"
                                                    )

                                                    found = true
                                                    break
                                                }
                                            }
                                        }
                                        if (found) {
                                            break
                                        }
                                    }
                                    if (!found) {
                                        var media = events?.get(0)?.media
                                        media?.get(0)?.run {
                                            isLastPlay = false
                                            lastVideoUrl = ""
                                            lastVideoDuration = "0"
                                            lastVideoThumb = thumbnailS3bucketId
                                            mediaId = id
                                            videoId = this.bunnyId
                                            lifecycleScope.launch(Dispatchers.Main) {
                                                binding.tvPlay.setText("play")
                                            }
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) { binding.tvPlay.setText("resume") }
                                    }
                                }
                                withContext(Dispatchers.Main){
                                    binding.let {
                                        projectData?.run {
                                            proTitle = projectTitle.toString()
                                            proDesc = projectDescription.toString()
                                            it.tvProjectTitle.text = proTitle.toString()
                                            it.tvProjectDesc.text = proDesc.toString()
                                            it.clTitle.visible()
                                            it.ivTrailer.visible()
                                            it.tvPlay.visible()
                                            showTools()
                                        }
                                        proLogo = data.logo
                                    }
                                    eventAdapter.update(events as ArrayList<EventsItem>)
                                }
                            }

                            crewList.clear()
                            //  this.crewMembers?.let { it1 -> crewList.addAll(it1) }

                            if (crewMembers != null && crewMembers.isNotEmpty()) {
                                // crewList.addAll(crewMembers)
                                binding.rvCreators.visible()
                                creatorsAdapter.update(crewMembers as ArrayList<crewMembers>)
                            }
                            isFirst = false

                            // dismissProgress()
                            binding.apply {
                                lifecycleScope.launch {
                                    if (crewMembers != null) {
                                        if (crewMembers.size == 3) {
                                            transitionValue4 =
                                                transitionValue3//tvPlay.top.toFloat()-tvProjectTitle.bottom.toFloat()+70
                                        } else if (crewMembers.size == 2) {
                                            transitionValue4 = transitionValue2
                                            // transitionValue= //tvPlay.top.toFloat()-tvProjectTitle.bottom.toFloat()+60
                                        }
                                    }

//                                    delay(300)
//                                    val location = IntArray(2)
//                                    location[1]
                                    // tvPlay.getLocationOnScreen(location)
                                    Log.e(
                                        "dbchdbv",
                                        "crewMembers.size ${crewMembers?.size} four $transitionValue4 dbjdbv $transitionValue3, $transitionValue2 tvPlay ${tvPlay.top} tvProjectDesc ${tvProjectTitle.bottom}  tvProjectDesc top ${tvProjectTitle.top}"
                                    )
                                }

                            }
                        }
                    }
                    progressDialog.dismiss()
                    dismissProgress()
                }

                is MyResource.isError -> {
                    progressDialog.dismiss()
                    dismissProgress()
                    binding.ivHomeCross.visible()
                    if (it.error == "Incorrect PIN") {
                        SharedPref.setBoolean(PrefConstent.ISLOGIN, false)
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.homefragment, true)
                            .build()
                        findNavController().navigate(R.id.loginFragment, null, navOptions)
                    }
                }

                else -> {}
            }
        }

    }

    fun saveDuration(request: PlayBackRequest) {
        viewModel.saveDuration(requireActivity(), request)
        durationObserve()
    }

    fun durationObserve() {
        viewModel._videoduraion.observe(requireActivity()) {
            when (it) {
                is MyResource.isLoading -> {
                }

                is MyResource.isSuccess -> {
//                    if (isDrawerOpen) {
//                        eventAdapter.updateDuration(eventVideoIndex, mediaIndex, videoduraion)
//                        mediaAdapter.updateDuration(mediaIndex, videoduraion)
//                    } else {
//                        eventAdapter.updateDuration(eventVideoIndex, mediaIndex, videoduraion)
//                    }
                    //filterItem(event, mediaId, videoDuration)


                    Log.e(
                        "homesavevideo",
                        "isDrawerOpen $isDrawerOpen  $eventVideoIndex mediaIndex $mediaIndex videoduraion $videoduraion data ${it.data}"
                    )
                    videoduraion = 0
                }

                is MyResource.isError -> {
                }

                else -> {}
            }
        }
    }

    fun filterItem(
        eventId: Int,
        mediaId: Int,
        duration: Long,
        bunneyId: String,
        thumb: String,
        isEnded: Boolean
    ) {
        showProgress()
        lifecycleScope.launch(Dispatchers.IO) {
            var newList = homeFragment.eventAdapter.getList()
            videoId = bunneyId
            lastVideoThumb = thumb
            lastVideoDuration = if (isEnded) {
                "0"
            } else {
                duration.toString()
            }
            isLastPlay = true
            val eventIndex = newList.indexOfFirst { it.eventId == eventId }
            Log.e(
                "filteridwith",
                "isEnded $isEnded duration $duration media id $mediaId eventId $eventId before event index $eventIndex update $newList"
            )
            try {
                var newMedia = newList[eventIndex].media
                if (newMedia != null && newMedia.isNotEmpty()) {
                    var mediaIndex = newList[eventIndex].media?.indexOfFirst { it.id == mediaId }
                    delay(1000)
                    if (mediaIndex != null) {
                        Log.e(
                            "drawerindex",
                            "mediaId $mediaId mediaIndex $mediaIndex eventIndex $eventIndex"
                        )
                        homeFragment.eventAdapter.updateDuration(eventIndex, mediaIndex, duration)
                        if (isDrawerOpen) {
                            if (!isTrailer) {
                                // mediaAdapter.updateDuration(mediaIndex!!, duration)
                                //
                                withContext(Dispatchers.Main) {
                                    binding.rvDrawer.adapter = mediaAdapter
                                }
                                viewFocus()
                            }
                        }

                    }
//                    var after = homeFragment.eventAdapter.getList()
//                    Log.e("filteridwith", "$mediaId after media index $mediaIndex update$after")
                }
                withContext(Dispatchers.Main) {
                    dismissProgress()
                }
            } catch (e: Exception) {
                Log.e("filteridwith", "crashed $e")
                withContext(Dispatchers.Main) {
                    dismissProgress()
                }
            }
        }
    }


    override fun netStatus() {
        //  bindView()
    }


    fun eventVideoFocus() = with(binding) {
        rvCategory.apply {
            post {
                getChildAt(eventFocusPos)?.requestFocus()
            }
        }
    }

    fun drawerVideoFocus() = with(binding) {
        rvDrawer.post {
            rvDrawer.getChildAt(drawerItemFocus)?.requestFocus()
        }
    }

    fun viewFocus() = with(binding) {
        Log.e("homefocus", " on resume focus $focusView")
        when (focusView) {
            StreamEnum.LOGOUT_VIEW -> {
                ivLogout.requestFocus()
            }

            StreamEnum.HOME_CLOSE -> {
                ivHomeCross.requestFocus()
            }

            StreamEnum.PLAY_RESUME -> {
                tvPlay.isFocusable = true
                tvPlay.isFocusableInTouchMode = true
                tvPlay.post {
                    tvPlay.requestFocus()
                }
            }

            StreamEnum.TRAILER -> {
                if (isDrawerOpen) {
                    rvDrawer.isFocusable = true
                    rvDrawer.isFocusableInTouchMode = true
                    rvDrawer.post { rvDrawer.getChildAt(drawerItemFocus)?.requestFocus() }
                } else {
                    ivTrailer.requestFocus()
                }
            }

            StreamEnum.BOTTOM_EVENT_VIEW -> {
                eventVideoFocus()
            }

            StreamEnum.DRAWER_VIEW -> {
                drawerVideoFocus()
            }

            else -> {}
        }

    }

//    handle video play functionality

    fun playerInitialization()  {
       if (::playerHandler.isInitialized) {
           playerHandler.player?.addListener(object : Player.Listener {
               override fun onPlaybackStateChanged(playbackState: Int) {
                   if (playbackState == Player.STATE_BUFFERING) {
                   } else if (playbackState == Player.STATE_READY) {
                   } else if (playbackState == Player.STATE_ENDED) {
                       Log.e("filteridwith", "video ended ")
                   }
               }

               override fun onTracksChanged(tracks: Tracks) {
               }

               override fun onPlayerError(error: PlaybackException) {
                   Log.e(
                       "ExoPlayerError",
                       "by video fragment Playback error: " + error.message,
                       error
                   )
               }

               override fun onPlayerErrorChanged(error: PlaybackException?) {
                   super.onPlayerErrorChanged(error)
                   Log.e("ExoPlayerError", "onPlayerErrorChanged " + error?.message, error)
               }
           })
       }

    }

    fun play(videoUrl: String) {
        if (::playerHandler.isInitialized) {
            mediaUrl=videoUrl
            playerHandler.setMediaUri(videoUrl, 0)
        }
    }

    fun playNextVideo() = with(binding) {
        if (playerHandler.player != null) {
            playerHandler.player?.run {
                playerHandler.pause()
                this.stop()
               // this.release()
            }
        }
        toolsCount=0
        playerView.requestLayout()
        playerView.invalidate()
        playerHandler.stopHandler()

        if (images.isNotEmpty()) {
            if (videoPlayingIndex < images.size) {
                if (videoPlayingIndex == images.size - 1) {
                    videoPlayingIndex = 0
                } else {
                    videoPlayingIndex++
                }
                if (images[videoPlayingIndex].hlsPlaylistUrl.isNotEmpty()) {
                    play(images[videoPlayingIndex].hlsPlaylistUrl)
                   // if (::countDownTimer.isInitialized) {
                        showRemainsTime(30000)
                        countDownTimer.start()
                        isTimerRunning = true
                   // }
                }
            }

        }

    }

    private fun updateDuration() {
        if (playerHandler.player != null) {
            playerHandler.player?.run {
                // val position = player.currentPosition
                homeFragment.currentVideoDuration = this.currentPosition
                if (this.playWhenReady) {
                    playerHandler.handler.postDelayed({ updateDuration() }, 500)
                } else {
                    playerHandler.stopHandler()
                }

                toolsCount += 500
                Log.e("homevideotest", "videoPlayingIndex $videoPlayingIndex toolsCount $toolsCount currentPosition ${this.currentPosition} total duration ${this.duration} playWhenReady ${this.playWhenReady}")
                if (toolsCount == 5000L) {
                    hideTools()
                }
                if (this.currentPosition >= 30000) {
                    playerHandler.stopHandler()
                    playNextVideo()
                    playerHandler = PlayerHandler(requireActivity(), binding.playerView)
                    if (images.isNotEmpty()) {
                        if (videoPlayingIndex < images.size) {
                            if (videoPlayingIndex == images.size - 1) {
                                videoPlayingIndex = 0
                            } else {
                                videoPlayingIndex++
                            }
                            if (images[videoPlayingIndex].hlsPlaylistUrl.isNotEmpty()) {
                                lifecycleScope.launch {
                                    delay(500)
                                    withContext(Dispatchers.Main){
                                        play(images[videoPlayingIndex].hlsPlaylistUrl)
                                    }
                                }

                            }
                        }

                    }
                }
            }
        }
    }

    lateinit var countDownTimer: CountDownTimer
    private var millisRemaining: Long = 30000
    private var isTimerRunning = false

    fun showRemainsTime(timeInMillis: Long) {
        toolsCount=0
        Log.e("homevideotest", " start timer $timeInMillis")
       // countDownTimer.cancel()
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
               millisRemaining = millisUntilFinished
                Log.e("homevideotest", "videoPlayingIndex $videoPlayingIndex millisUntilFinished $millisUntilFinished ")
                toolsCount++
                if (millisUntilFinished <= 24000L) {
                    if (toolsCount==10L) {
                        hideTools()
                    }
                }
                else if(millisUntilFinished <= 1000L){
                    onFinish()
                }

            }

            override fun onFinish() {
                millisRemaining = 0
                toolsCount=0L
                playNextVideo()
                playerInitialization()
                showTools()
                toolsCount=0L
//                onFinish()
            }
        }
       // countDownTimer.start()

    }
    fun pauseCountdown() {
        if (isTimerRunning) {
           // if (::countDownTimer.isInitialized) {
                countDownTimer.cancel()  // Cancel the timer
//            countDownTimer.onFinish()
                isTimerRunning = false
          //  }
            Log.e("homevideotest", "Timer Paused")
        }
    }

    // Function to resume the countdown timer from where it was paused
    fun resumeCountdown() {
        if (!isTimerRunning) {
            if (::countDownTimer.isInitialized) {
                showRemainsTime(millisRemaining)
                countDownTimer.start()// Start from the remaining time
                isTimerRunning = true
            }
            Log.e("homevideotest", "Timer Resumed")
        }
    }

    override fun onResume() {
        super.onResume()
        if (isNetworkAvailable) {
            binding.apply {
                Log.e(
                    "resumehandle",
                    " $isLastPlay videoduraion $videoduraion isFirstVideo $isFirstVideo  hfhh $eventFocusPos ncdjknv ${isDrawerOpen}"
                )
                isFirstVideo = true
                if (isLastPlay) {
                    tvPlay.setText("resume")
                } else {
                    tvPlay.setText("play")
                }
                showTools()
                //  rvBackgVideo.resumeVideo()
                if (playerHandler.player != null) {
                    if (::playerHandler.isInitialized){
                    playerHandler.player?.run {
                        if (isTimerRunning) {
                            playerHandler.setMediaUri(mediaUrl, this.currentPosition)
                        }
                        resumeCountdown()
                    }
                }}
                viewFocus()
            }
        }
    }

    override fun onPause() {
//        binding.rvBackgVideo.apply {
//            pauseVideo()
//            toolsCount = 0
//            // playerHandler.release()
//        }
//        Log.e("homefocus", "onpause home $focusView")
//        binding.rvBackgVideo.isfirst = true

        if (playerHandler.player != null) {
            playerHandler.player?.run {
                playerHandler.pause()
                pauseCountdown()
            }
        }

        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity()?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (playerHandler.player != null) {
            pauseCountdown()
            playerHandler.pause()
            playerHandler.release()
        }
//        binding.rvBackgVideo.apply {
//            pauseVideo()
//            playerHandler.release()
//        }
    }

}
