package com.streamefy.component.ui.home


import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
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
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
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
        if (isFirst) {
            showProgress()
            getUserData()
            playerHandler = PlayerHandler(requireActivity(), binding.playerView)
            transitionValue4 = dpToPx(140f)
            transitionValue3 = dpToPx(120f)
            transitionValue2 = dpToPx(85f)
            playerInitialization()

        }

        eventView()
        creatorView()
        focusView()
        keyMove()
        binding.apply {

            ivClose.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.END)
                isMenuOpened = false
            }
//            Handle right drawer listener functionality
            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                }

                override fun onDrawerOpened(drawerView: View) {
                    focusView = StreamEnum.DRAWER_VIEW
                    isDrawerOpen = true
                    rvDrawer.post { rvDrawer.getChildAt(drawerItemFocus)?.requestFocus() }
                }

                override fun onDrawerClosed(drawerView: View) {
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
//           Handle remote back press functionality
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showCustomDialog()
                }
            })

        lifecycleScope.launch {
//            launch {
//                delay(300)
//                Log.d("ThreadTest", "Current thread1: ${Thread.currentThread().name}")
//            }
//            launch(Dispatchers.IO) {
//                delay(200)
//                Log.d("ThreadTest", "Current thread2: ${Thread.currentThread().name}")
//            }
//            launch(Dispatchers.Default) {
//                delay(100)
//                Log.d("ThreadTest", "Current thread3: ${Thread.currentThread().name}")
//            }

            var job1= async {  delay(500)
                Log.d("ThreadTest", "Current thread1: ${Thread.currentThread().name} ")
                return@async "job one"
                }

            var job2= async {  delay(200)
                Log.d("ThreadTest", "Current thread3: ${Thread.currentThread().name} ")
               return@async 56
            }
           /// var result1=job1.await()

          //  var result2=  job2.await()
          launch(Dispatchers.IO) {
              channel.send("Hi, developer i am from channel")
          }

//            withContext(Dispatchers.Main){
                delay(50)

                Log.d("ThreadTest", "Main thread ttt: ${Thread.currentThread().name}  from channel ${channel.receive()}")
//            }
        }

    }
    private val channel = Channel<String>()

//    Handle all buttons key movement
    private fun keyMove() = with(binding) {
        ivHomeCross.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    tvPlay.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    tvPlay.requestFocus()
                }

                StreamEnum.LEFT_DPAD_KEY -> {
                    tvPlay.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    tvPlay.requestFocus()
                }

                else -> {}
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
                    ivTrailer.requestFocus()
                }

                else -> {}
            }
        }
        ivTrailer.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
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

//    Handle all view cursor focus
    private fun focusView() = with(binding) {
        ivHomeCross.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusView = StreamEnum.HOME_CLOSE
                val params = ivHomeCross.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._20sdp)
                params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                ivHomeCross.layoutParams = params
            } else {
                val params = ivHomeCross.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._16sdp)
                params.height = resources.getDimensionPixelSize(R.dimen._16sdp)
                ivHomeCross.layoutParams = params
            }
        }
        ivHomeCross.setOnClickListener {
            val bundle = Bundle().apply { putBoolean(PrefConstent.ISHOME, true) }
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.homefragment, true) // Set inclusive to true
                .build()
                findNavController().navigate(R.id.projectfragment, bundle, navOptions)

        }
        customIndicator.setOnFocusChangeListener { v, hasFocus ->
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

                val params = ivClose.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._20sdp)
                params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                ivClose.layoutParams = params
            } else {

                val params = ivClose.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp)
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
            if (tvPlay.text.toString().contains("play")) {
                playEventVideo()
            } else {
                playEventVideo()
            }

        }
        ivTrailer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusView = StreamEnum.TRAILER
                ivTrailer.setImageResource(R.drawable.ic_selected_trailer)
            } else {
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

//    Handle play event video
    private fun playEventVideo() {
        isPlayByPlayButton = true
        toGotoVideo(
            lastVideoDuration,
            lastVideoThumb,
            videoId,
            mediaId
        )
    }

//    Crew member list initialization
    private fun creatorView() = with(binding) {
        rvCreators.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            creatorsAdapter = CreatorsAdapter(requireActivity(), crewList) {}
            adapter = creatorsAdapter
        }
    }

    var toolsCount: Long = 0

//    Trailer video scrolling functionality which is not in use
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
       // thumbShow()
        //  customIndicator.setIndicatorCount(images.size, 0)
//        if (images.size > 1) {
//            binding.customIndicator.visible()
//        }
    }

//    fun thumbShow() = with(binding) {
////        rvBackgVideo.run {
////            visible()
////            animate()
////                .alpha(1f)
////                .scaleX(1f)
////                .scaleY(1f)
////                .setInterpolator(DecelerateInterpolator())
////                .setDuration(5000)
////                .start()
////        }
//
//    }

//    Show project desc and crew member list
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

//    Calculate the position of project desc and crew member to manage transition
    private fun dpToPx(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }

//    Hide project desc and crew member list
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

//    Get project details
    private fun getUserData() {
        dismissProgress()
        viewModel.getUserVideos(requireActivity(), page, 10, auth_pin, projectId.toInt(), phone)
        observe()
    }

//    Show logout dialog and checks for right drawer
    private fun showCustomDialog() {
        if (isDrawerOpen) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            LogoutDialog(requireContext()) {
                SharedPref.setBoolean(PrefConstent.ISLOGIN, false)
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.homefragment, true)
                    .build()
                findNavController().navigate(R.id.loginFragment, null, navOptions)
            }.show()
        }
    }

//    Initialize the event drawer video list
    private fun drawerView() = with(binding) {
        tvTitle.text = selectedTitle
        isTrailer = false
        focusView = StreamEnum.DRAWER_VIEW
        rvDrawer.apply {
            isTrailer = false
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            mediaAdapter = DrawerAdapter(requireActivity(), mediaList as ArrayList<Any>) {

//                Handle event item clicks and navigate to the Video Screen to play video
                drawerItemFocus = it
                mediaIndex = it
                lifecycleScope.launch(Dispatchers.IO) {
                    mediaList[it].run {
                        if (this.playbackDuration.isNotEmpty()) {
                            val number: Double = this.playbackDuration.toDouble()
                            val intValue = floor(number).toInt()
                            var newDuration = intValue.toString()
                            val totalDuration: Long =
                                convertToMillis(this.totalVideoDuration)
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

//    Initialize the trailer drawer video list
    private fun trailerDrawer() = with(binding) {
        tvTitle.text = "Trailer"
        focusView = StreamEnum.DRAWER_VIEW
        rvDrawer.apply {
            getChildAt(0)?.requestFocus()
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            mediaAdapter = DrawerAdapter(requireActivity(), images as ArrayList<Any>) {

 //                Handle trailer item clicks and navigate to the Video Screen to play video
                lifecycleScope.launch(Dispatchers.IO) {
                    images[it].run {
                        val thumbnailS3bucketId = this.thumbnailSBucketId
                        val newvideoId = this.bunnyId
                        val newmediaId = this.id
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

//    Initialize the Event video list
    private fun eventView() = with(binding) {
        rvCategory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            eventAdapter = CategoryAdapter(requireActivity(), eventList) { pos, type ->
//                Handle trailer item clicks
                selectedTitle = eventList[pos].eventTitle
                eventFocusPos = pos
                eventVideoIndex = pos
                mediaIndex = 0
                when (type) {
                    StreamEnum.SINGLE -> {
//                        Handle item clicks and navigate to play the video
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (eventList[pos].media != null) {
                                if (eventList[pos].media?.size!! >= 1) {
                                    eventList[pos].media?.get(0)?.run {
                                        val number: Double = this.playbackDuration.toDouble()
                                        val intValue = floor(number).toInt()
                                        var newDuration = intValue.toString()

                                        val totalDuration: Long =
                                            convertToMillis(this.totalVideoDuration)
                                        if (newDuration.toLong() >= totalDuration) {
                                            newDuration = "0"
                                        }
                                        isPlayByPlayButton = false
                                        mediaId = this.id
                                        videoId = this.bunnyId
                                        withContext(Dispatchers.Main) {
//                                            findNavController().navigate(R.id.dynamicscreen)
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
//                        Open more event list to the right drawer
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
//                        Handle event item focus to play/resume button
                        tvPlay.isFocusable = true
                        tvPlay.isFocusableInTouchMode = true
                        tvPlay.post {
                            tvPlay.requestFocus()
                        }

                    }

                    StreamEnum.PAGINATION -> {
//                        Handle pagination
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

//    Navigate to the video screen to play the video
    private fun toGotoVideo(duration: String, thumb: String, bunneId: String, mediaId: Int) {
        val bundle = Bundle()
        bundle.putString(PrefConstent.PLAY_BACK_DURATION, duration)
        bundle.putString(PrefConstent.VIDEO_THUMB, thumb)
        bundle.putString(PrefConstent.VIDEO_ID, bunneId)
        bundle.putString(PrefConstent.MEDIA_ID, mediaId.toString())
        findNavController().navigate(R.id.videofragment, bundle)
    }

//    Get more video from server
    private fun eventVideosMore() {
        viewModel.getUserVideos(requireActivity(), page, 10, auth_pin, projectId.toInt(), phone)
        observe()
    }

//    Get project response from server
    private fun observe() {
        viewModel._homeLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    progressDialog.show()
                }

                is MyResource.isSuccess -> {
                    dismissProgress()
                    binding.ivHomeCross.visible()
                    it.data?.data?.run {
                        val data = this
                        if (isEventPagination) {
//                            Handle pagination
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
                           val projectData=project?.get(0)
                            lifecycleScope.launch(Dispatchers.IO) {
//                                Add trailer video to the trailer list and handle trailer playing time
                                data.backgroundMedia?.run {
                                    if (this.isNotEmpty()) {
                                        images.addAll(this as ArrayList<BackgroundMediaItem>)
                                        withContext(Dispatchers.Main) {
                                            if (images.isNotEmpty()) {
                                                if (images[0].hlsPlaylistUrl.isNotEmpty()) {
                                                    if (images[0].thumbnailSBucketId.isNotEmpty()) {
                                                       // binding.ivVideoThumb.loadUrl(images[0].thumbnailSBucketId)
                                                    }
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
//                                    Add event to the event video list
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

                                                    val totalDuration: Long =
                                                        convertToMillis(media.totalVideoDuration)

                                                    lastVideoDuration =
                                                        if (lastVideoDuration.toLong() >= totalDuration) {
                                                            "0"
                                                        } else {
                                                            media.playbackDuration
                                                        }
                                                    found = true
                                                    break
                                                }
                                            }
                                        }
                                        if (found) {
                                            break
                                        }
                                    }
//                                    checks for select last video played
                                    if (!found) {
                                        val media = events[0].media
                                        if (!media.isNullOrEmpty()) {
                                            media[0].run {
                                                isLastPlay = false
                                                lastVideoUrl = ""
                                                lastVideoDuration = "0"
                                                lastVideoThumb = thumbnailS3bucketId
                                                mediaId = id
                                                videoId = this.bunnyId
                                                lifecycleScope.launch(Dispatchers.Main) {
                                                    binding.tvPlay.text = "play"
                                                }
                                            }
                                        }else{
                                            withContext(Dispatchers.Main) { binding.tvPlay.text = "play"
                                            binding.ivHomeCross.requestFocus()}
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) { binding.tvPlay.text = "resume" }
                                    }
                                }
                                withContext(Dispatchers.Main){
//                                    Show project details
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
//                                        it.projectlogo.loadUrl(proLogo)
                                        it.projectlogo.gone()
                                    }
                                    eventAdapter.update(events as ArrayList<EventsItem>)
                                }
                            }

                            crewList.clear()

                            if (crewMembers != null && crewMembers.isNotEmpty()) {
//                                Add crew member to the crew list
                                binding.rvCreators.visible()
                                creatorsAdapter.update(crewMembers as ArrayList<crewMembers>)
                            }
                            isFirst = false

                            binding.apply {
                                lifecycleScope.launch {
//                                    Checks for handle crew member transition
                                    if (crewMembers != null) {
                                        if (crewMembers.size == 3) {
                                            transitionValue4 =
                                                transitionValue3
                                        } else if (crewMembers.size == 2) {
                                            transitionValue4 = transitionValue2
                                        }
                                    }

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
//                        Navigate to the login screen if PIN is incorrect
                        SharedPref.setBoolean(PrefConstent.ISLOGIN, false)
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.homefragment, true)
                            .build()
                        findNavController().navigate(R.id.loginFragment, null, navOptions)
                    }
                }

            }
        }

    }

//    save video played duration to the server
    fun saveDuration(request: PlayBackRequest) {
        viewModel.saveDuration(requireActivity(), request)
        durationObserve()
    }
//     Get response, video is saved or not
    private fun durationObserve() {
        viewModel._videoduraion.observe(requireActivity()) {
            when (it) {
                is MyResource.isLoading -> {
                }

                is MyResource.isSuccess -> {
                    videoduraion = 0
                }

                is MyResource.isError -> {
                }

            }
        }
    }

//    Save video played duration locally
    fun filterItem(
        eventId: Int,
        mediaId: Int,
        duration: Long,
        bunneyId: String,
        thumb: String,
        isEnded: Boolean
    ) {
        Log.e("saveme","bunnid $bunneyId isEnded $isEnded duration $duration eventId $eventId mediaId $mediaId ")
        showProgress()
        lifecycleScope.launch(Dispatchers.IO) {
            val newList = homeFragment.eventAdapter.getList()
            videoId = bunneyId
            lastVideoThumb = thumb
            lastVideoDuration = if (isEnded) {
                "0"
            } else {
                duration.toString()
            }
            isLastPlay = true
            val eventIndex = newList.indexOfFirst { it.eventId == eventId }

            try {
                val newMedia = newList[eventIndex].media
                if (newMedia != null && newMedia.isNotEmpty()) {
                    val mediaIndex = newList[eventIndex].media?.indexOfFirst { it.id == mediaId }
                    delay(1000)
                    if (mediaIndex != null) {
                        homeFragment.eventAdapter.updateDuration(eventIndex, mediaIndex, duration)
                        if (isDrawerOpen) {
                            if (!isTrailer) {
                                withContext(Dispatchers.Main) {
                                    binding.rvDrawer.adapter = mediaAdapter
                                }
                                viewFocus()
                            }
                        }

                    }

                }
                withContext(Dispatchers.Main) {
                    dismissProgress()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    dismissProgress()
                }
            }
        }
    }


    override fun netStatus() {
    }

//      Handle focus on event video
    fun eventVideoFocus() = with(binding) {
        rvCategory.apply {
            post {
                getChildAt(eventVideoIndex)?.requestFocus()
            }
        }
    }

//      Handle focus on right drawer video
    private fun drawerVideoFocus() = with(binding) {
        rvDrawer.post {
            rvDrawer.getChildAt(drawerItemFocus)?.requestFocus()
        }
    }
//       Handle all view focus
    fun viewFocus() = with(binding) {
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

//    handle video play state with video listener
    fun playerInitialization()  {
       if (::playerHandler.isInitialized) {
           playerHandler.player?.addListener(object : Player.Listener {
               override fun onPlaybackStateChanged(playbackState: Int) {
                   if (playbackState == Player.STATE_BUFFERING) {
                   } else if (playbackState == Player.STATE_READY) {
                       videoTranisition()
                   } else if (playbackState == Player.STATE_ENDED) {
                   }
               }
               override fun onPlayerError(error: PlaybackException) {
               }
               override fun onPlayerErrorChanged(error: PlaybackException?) {
                   super.onPlayerErrorChanged(error)
               }
           })
           playerHandler.mute()
       }
    }

//      Play trailer video
    private fun play(videoUrl: String) {
        if (::playerHandler.isInitialized) {
            mediaUrl=videoUrl
            playerHandler.setMediaUri(videoUrl, 0)
        }
    }

//       Handle next trailer play functionality
    fun playNextVideo() = with(binding) {
        if (playerHandler.player != null) {
            playerHandler.player?.run {
                playerHandler.pause()
                this.stop()
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
                        showRemainsTime(30000)
                        countDownTimer.start()
                        isTimerRunning = true
                }
            }

        }

    }

//         Currenctly not in used
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


//    Handle hide/show views and next video play
    fun showRemainsTime(timeInMillis: Long) {
        toolsCount=0
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
               millisRemaining = millisUntilFinished
                toolsCount++
                if (millisUntilFinished <= 24000L) {
                    if (toolsCount == 10L) {
                        hideTools()
                    }
                    if (millisUntilFinished <= 10000L) {

                      lifecycleScope.launch(Dispatchers.IO) {
                          if (images.isNotEmpty()) {
                              if (videoPlayingIndex < images.size) {
                                  if (videoPlayingIndex == images.size - 1) {
                                      videoPlayingIndex = 0
                                  } else {
                                      videoPlayingIndex++
                                  }
                                  if (images[videoPlayingIndex].thumbnailSBucketId.isNotEmpty()) {
                                     withContext(Dispatchers.Main){
                                         if (millisUntilFinished <= 4000L) {
                                         thumbShow()
                                         }

                                     }

                                  }
                              }

                          }
                      }
                    }
                }

            }

            override fun onFinish() {
                millisRemaining = 0
                toolsCount=0L
                playNextVideo()
                playerInitialization()
                showTools()
                toolsCount=0L
            }
        }
    }

//    Handle trailer video transition
    fun videoTranisition() = with(binding) {
        ivVideoThumb.animate()
            .alpha(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(4000)
            .withEndAction {
                playerView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(DecelerateInterpolator())// Scale to original size
                    .setDuration(4000)
                    .start()
            }
            .start()
    }

//    Handle visibilty of trailer video
    fun thumbShow()= with(binding){
                    ivVideoThumb.run {
                        visible()
                        animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setInterpolator(DecelerateInterpolator())
                            .setDuration(4000)
                            .start()
                    }

        }

//      Pause the timer
    private fun pauseCountdown() {
        if (isTimerRunning) {
                countDownTimer.cancel()
                isTimerRunning = false
            Log.e("homevideotest", "Timer Paused")
        }
    }

    // Function to resume the countdown timer from where it was paused
    private fun resumeCountdown() {
        if (!isTimerRunning) {
            if (::countDownTimer.isInitialized) {
                showRemainsTime(millisRemaining)
                countDownTimer.start()// Start from the remaining time
                isTimerRunning = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isNetworkAvailable) {
            binding.apply {

                isFirstVideo = true
                if (isLastPlay) {
                    tvPlay.setText("resume")
                } else {
                    tvPlay.setText("play")
                }
                showTools()
                if (playerHandler.player != null) {
                    if (::playerHandler.isInitialized){
                    playerHandler.player?.run {
                            playerHandler.setMediaUri(mediaUrl, this.currentPosition)

                        resumeCountdown()
                    }
                }}
                viewFocus()
            }
        }
    }

    override fun onPause() {
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
        requireActivity().window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (playerHandler.player != null) {
            pauseCountdown()
            playerHandler.pause()
            playerHandler.release()
        }
    }

}
