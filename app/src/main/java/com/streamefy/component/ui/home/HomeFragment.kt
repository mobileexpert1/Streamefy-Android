package com.streamefy.component.ui.home

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.CircularProgressDialog
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.adapter.CategoryAdapter
import com.streamefy.component.ui.home.adapter.CreatorsAdapter
import com.streamefy.component.ui.home.adapter.DrawerAdapter
import com.streamefy.component.ui.home.background.BackgroundAdpater
import com.streamefy.component.ui.home.model.BackgroundMediaItem
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.component.ui.home.model.MediaItem
import com.streamefy.component.ui.home.model.crewMembers
import com.streamefy.component.ui.home.viewmodel.HomeVm
import com.streamefy.component.ui.video.model.PlayBackRequest
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentHomeBinding
import com.streamefy.network.MyResource
import com.streamefy.utils.convertToMillis
import com.streamefy.utils.customAlfa
import com.streamefy.utils.gone
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
    var transitionValue = 0f

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
    lateinit var progressDialog:CircularProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog= CircularProgressDialog(requireContext())
        auth_pin = SharedPref.getString(PrefConstent.AUTH_PIN).toString()
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        isPrimaryuser = SharedPref.getBoolean(PrefConstent.ISPRIMARY_USER)
        projectId = SharedPref.getString(PrefConstent.PROJECT_ID).toString()
        homeFragment = this
        isEventPagination = false
        if (isFirst) {
            showProgress()
            getUserData()
            //playbackObserver()
            transitionValue = dpToPx(140f)
        }

        eventView()
        creatorView()
        foucusView()
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
        rvBackgVideo.remoteKey {
            Log.e("dkvdknv", "ncjxcnbd backvideo")
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    eventVideoFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    tvPlay.requestFocus()
                }

                StreamEnum.LEFT_DPAD_KEY -> {
                    // ivLogout.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    // ivLogout.requestFocus()
                }

                else -> {}
            }
        }

        customIndicator.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {

                    if (rvBackgVideo.targetPosition == 0) {
                        ivHomeCross.requestFocus()
                    } else {
                        val currenPos = rvBackgVideo.targetPosition - 1
                        binding.rvBackgVideo.backScroll(currenPos)

                    }
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    if (rvBackgVideo.targetPosition == rvBackgVideo.mediaObjects.size - 1) {
//                        tvPlay.requestFocus()
                    } else {
                        val currenPos = rvBackgVideo.targetPosition + 1
                        binding.rvBackgVideo.smoothScrollToPosition(currenPos)
                    }
                }

                StreamEnum.LEFT_DPAD_KEY -> {
                    tvPlay.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                }

                else -> {}
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
        tvPlay.setOnClickListener {
            focusView = StreamEnum.PLAY_RESUME
            Log.e("lcsdwdw", "clicked ${tvPlay.text.toString()}")
            if (tvPlay.text.toString().contains("play")) {
                playEventVideo()
            } else {
                playEventVideo()
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

        rvBackgVideo.apply {
            gone()
            alpha = 0f
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            setList(images)
            var backgadapter = BackgroundAdpater(requireActivity(), images) { index -> }
            adapter = backgadapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        var newPos =
                            (rvBackgVideo.recyclerview?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        Log.e(
                            "skncksnc",
                            "current ${rvBackgVideo.targetPosition} new index $newPos skcks ${mediaObjects.size} "
                        )
                    }
                }
            })
        }
        thumbShow()
        //  customIndicator.setIndicatorCount(images.size, 0)
//        if (images.size > 1) {
//            binding.customIndicator.visible()
//        }
    }

    fun thumbShow() = with(binding) {
        rvBackgVideo.run {
            visible()
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(5000)
                .start()
        }

    }

    fun showTools() = with(binding) {
        clOpecity.visible()
        tvProjectDesc.visible()
        rvCreators.visible()
        tvProjectTitle.visible()

        tvProjectDesc.transition(transitionValue, 0f)
        rvCreators.transition(transitionValue, 0f)
        projectlogo.transition(transitionValue, 0f)
        tvProjectTitle.transition(transitionValue, 0f)

        tvProjectDesc.customAlfa(0f, 1f)
        rvCreators.customAlfa(0f, 1f)

    }

    fun dpToPx(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }

    fun hideTools() = with(binding) {

        tvProjectDesc.transition(0f, transitionValue)
        rvCreators.transition(0f, transitionValue)
        projectlogo.transition(0f, transitionValue)
        tvProjectTitle.transition(0f, transitionValue)

        tvProjectDesc.customAlfa(1f, 0f)
        rvCreators.customAlfa(1f, 0f)

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
                        rvBackgVideo.clearFocus()
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
                    Log.e("hfhddnub", "$isEventPagination page $page pagination ${it.data?.data?.events?.size}" + it.data?.data.toString())
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
                            // event video
                            lifecycleScope.launch(Dispatchers.IO) {
                                data.backgroundMedia?.run {
                                    if (this.isNotEmpty()) {
                                        images.addAll(this as ArrayList<BackgroundMediaItem>)
                                        withContext(Dispatchers.Main) {
                                            sliderInit()
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
                            }


                            eventAdapter.update(events as ArrayList<EventsItem>)
                            binding.let {
                                this.project?.get(0)?.run {
                                    proTitle = projectTitle.toString()
                                    proDesc = projectDescription.toString()
                                    it.tvProjectTitle.text = proTitle.toString()
                                    it.tvProjectDesc.text = proDesc.toString()
                                    it.clTitle.visible()
                                    it.ivTrailer.visible()
                                    it.tvPlay.visible()
                                    showTools()
                                }
//                                it. rvBackgVideo.requestFocus()
//                                it.projectlogo.loadUrl(this.logo)
                                proLogo = this.logo


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
            Log.e("filteridwith", "isEnded $isEnded duration $duration media id $mediaId eventId $eventId before event index $eventIndex update $newList")
            try {
                var newMedia = newList[eventIndex].media
                if (newMedia != null && newMedia.isNotEmpty()) {
                    var mediaIndex = newList[eventIndex].media?.indexOfFirst { it.id == mediaId }
                    delay(1000)
                    if (mediaIndex != null) {
                        Log.e("drawerindex","mediaId $mediaId mediaIndex $mediaIndex eventIndex $eventIndex")
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

    override fun onResume() {
        super.onResume()
        if (isNetworkAvailable) {
            binding.apply {
                Log.e("resumehandle", " $isLastPlay videoduraion $videoduraion isFirstVideo $isFirstVideo  hfhh $eventFocusPos ncdjknv ${isDrawerOpen}")
                isFirstVideo = true
                if (isLastPlay) {
                    tvPlay.setText("resume")
                } else {
                    tvPlay.setText("play")
                }
                showTools()
                rvBackgVideo.resumeVideo()
                viewFocus()
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


    override fun onPause() {
        binding.rvBackgVideo.apply {
            pauseVideo()
            toolsCount=0
            // playerHandler.release()
        }
        Log.e("homefocus", "onpause home $focusView")
        binding.rvBackgVideo.isfirst = true
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity()?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding.rvBackgVideo.apply {
            pauseVideo()
            playerHandler.release()
        }
    }

}
