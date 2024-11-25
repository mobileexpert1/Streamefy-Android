package com.streamefy.component.ui.home

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.ExitDialog
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
import com.streamefy.utils.gone
import com.streamefy.utils.hideTransition
import com.streamefy.utils.moveDown
import com.streamefy.utils.moveUp
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showTransition
import com.streamefy.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

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
    var lastVideoDuration = ""
    var lastVideoThumb = ""
    var isPlayByPlayButton = false
    var isPrimaryuser = false


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //  progressDialog= CircularProgressDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth_pin = SharedPref.getString(PrefConstent.AUTH_PIN).toString()
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        isPrimaryuser = SharedPref.getBoolean(PrefConstent.ISPRIMARY_USER)
        projectId = SharedPref.getString(PrefConstent.PROJECT_ID).toString()
        homeFragment = this
        isEventPagination = false
        if (isFirst) {
            getUserData()
            //playbackObserver()
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
//                    drawerView.requestFocus()
                    isDrawerOpen = true
                    // eventVideoFocus()
                    rvDrawer.post { rvDrawer.getChildAt(0)?.requestFocus() }
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

    fun savePlayback(event: Int, mediaId: Int, bunneyId: String, videoDuration: Long) {
        videoId = bunneyId
        if (videoDuration >= 0) {
            var request = PlayBackRequest()
            request.phoneNumber = phone
            request.mediaId = mediaId
            request.videoId = videoId
            request.duration = videoDuration.toString()
            lastVideoDuration = videoDuration.toString()
//            if (!isPlayByPlayButton) {
//                lifecycleScope.launch(Dispatchers.IO) {
//                    var newList = eventAdapter.getList()
//                    if (newList.isNotEmpty()) {
//                        newList[eventVideoIndex].media?.get(mediaIndex)?.run {
//                            isLastPlay = true
//                            lastVideoUrl = ""
//                            lastVideoDuration = videoduraion.toString()
//                            lastVideoThumb = thumbnailS3bucketId
//                        }
//                    }
//                }
//            }

            viewModel.saveDuration(requireContext(), request)
            durationObserve(event, mediaId, videoDuration)
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
        ivHomeCross.requestFocus()
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


//        tvPlay.setOnClickListener {
//            Log.e("lcsdwdw", "clicked ${tvPlay.text.toString()}")
//            if (tvPlay.text.toString().contains("play")) {
//                tvPlay.setText("pause")
//                tvPlay.setCompoundDrawablesWithIntrinsicBounds(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_backg_pause
//                    ), null, null, null
//                )
//                binding.rvBackgVideo.apply {
//                    playerHandler.play()
//                }
//            } else {
//                tvPlay.setText("play")
//                tvPlay.setCompoundDrawablesWithIntrinsicBounds(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_backg_play
//                    ), null, null, null
//                )
//                binding.rvBackgVideo.apply {
//                    playerHandler.pause()
//                }
//            }
//
//        }

    }

    fun playEventVideo() {
        Log.e(
            "playresumesd",
            "play by button $isPlayByPlayButton last video data $isLastPlay $lastVideoDuration video url $lastVideoUrl thumb $lastVideoThumb"
        )
        isPlayByPlayButton = true
        val bundle = Bundle()
        bundle.putString(PrefConstent.VIDEO_URL, lastVideoUrl)
        bundle.putString(PrefConstent.PLAY_BACK_DURATION, lastVideoDuration)
        bundle.putBoolean(PrefConstent.ISRESUME, isPlayByPlayButton)
        bundle.putString(PrefConstent.VIDEO_THUMB, lastVideoThumb)
        bundle.putString(PrefConstent.VIDEO_ID, videoId)
        bundle.putString(PrefConstent.MEDIA_ID, mediaId.toString())
        findNavController().navigate(R.id.videofragment, bundle)
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
                        //  customIndicator.updateIndicator(newPos)

//                            tvPlay.setText("pause")
//                            tvPlay.setCompoundDrawablesWithIntrinsicBounds(
//                                ContextCompat.getDrawable(
//                                    requireActivity(),
//                                    R.drawable.ic_backg_pause
//                                ), null, null, null
//                            )
//                        if (tvPlay.isFocused){
//                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
//                                // Revert size when not focused
//                                tvPlay.compoundDrawableTintList =
//                                    ColorStateList.valueOf(
//                                        ContextCompat.getColor(
//                                            requireActivity(),
//                                            R.color.purple
//                                        )
//                                    )
//
//                            }else{
//                                val drawables = tvPlay.compoundDrawables
//                                val drawableStart = drawables[0]  // You can adjust this for top, end, bottom as needed
//                                if (drawableStart != null) {
//                                    val wrappedDrawable = DrawableCompat.wrap(drawableStart)
//                                    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireActivity(), R.color.purple))
//                                    tvPlay.setCompoundDrawablesWithIntrinsicBounds(wrappedDrawable, drawables[1], drawables[2], drawables[3])
//                                }
//                            }
//
//                            tvPlay.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//                        }
//                        else{
//                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//                                // Revert size when not focused
//                                tvPlay.compoundDrawableTintList =
//                                    ColorStateList.valueOf(
//                                        ContextCompat.getColor(
//                                            requireActivity(),
//                                            R.color.white
//                                        )
//                                    )
//
//                            }else{
//                                val drawables = tvPlay.compoundDrawables
//                                val drawableStart = drawables[0]  // You can adjust this for top, end, bottom as needed
//                                if (drawableStart != null) {
//                                    val wrappedDrawable = DrawableCompat.wrap(drawableStart)
//                                    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireActivity(), R.color.white))
//                                    tvPlay.setCompoundDrawablesWithIntrinsicBounds(wrappedDrawable, drawables[1], drawables[2], drawables[3])
//                                }
//                            }
//
//                            tvPlay.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
//                        }

                    }
                }
            })
        }

        //  customIndicator.setIndicatorCount(images.size, 0)
//        if (images.size > 1) {
//            binding.customIndicator.visible()
//        }
    }

    fun hideTools() = with(binding) {
        Log.e("skmcks", "sbjcbs $toolsCount")
        clOpecity.gone()
        projectlogo.moveDown(requireContext())
        tvProjectTitle.moveDown(requireContext())
        tvProjectDesc.hideTransition(requireContext())
        rvCreators.hideTransition(requireContext())

    }

    fun showTools() = with(binding) {
        clOpecity.visible()
        projectlogo.moveUp(requireContext())
        tvProjectTitle.moveUp(requireContext())
        tvProjectDesc.showTransition(requireContext())
        rvCreators.showTransition(requireContext())
    }

    private fun moveUp(view: View) {
        view.translationY = 0f
    }

    private fun moveDown(view: View) {
        view.translationY = 0f
    }


    private fun getUserData() {
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
            getChildAt(0)?.requestFocus()

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            mediaAdapter = DrawerAdapter(requireActivity(), mediaList as ArrayList<Any>) {
//                drawerLayout.closeDrawer(GravityCompat.END)
                drawerItemFocus = it
                mediaIndex = it
                mediaId = mediaList[it].id
                videoId = mediaList[it].bunnyId
                isPlayByPlayButton = false
                var bundle = Bundle()
                bundle.putString(PrefConstent.PLAY_BACK_DURATION, mediaList[it].playbackDuration)
                bundle.putString(PrefConstent.VIDEO_URL, "")
                bundle.putBoolean(PrefConstent.ISRESUME, isPlayByPlayButton)
                bundle.putString(PrefConstent.VIDEO_THUMB, mediaList[it].thumbnailS3bucketId)
                bundle.putString(PrefConstent.VIDEO_ID, videoId)
                bundle.putString(PrefConstent.MEDIA_ID, mediaId.toString())
                findNavController().navigate(R.id.videofragment, bundle)
//                        findNavController().navigate(R.id.dynamicscreen)
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
                // drawerLayout.closeDrawer(GravityCompat.END)
                //  binding.rvBackgVideo.backScroll(it)
                images[0]?.run {
                    var hlsPlaylistUrl = this.hlsPlaylistUrl
                    var thumbnailS3bucketId = this.thumbnailSBucketId
                    var playbackDuration = "0"
                    videoId = this.bunnyId
                    val bundle = Bundle()
                    bundle.putString(PrefConstent.VIDEO_URL, hlsPlaylistUrl)
                    bundle.putString(
                        PrefConstent.PLAY_BACK_DURATION,
                        playbackDuration
                    )
                    bundle.putBoolean(PrefConstent.ISRESUME, isPlayByPlayButton)
                    bundle.putString(PrefConstent.VIDEO_THUMB, thumbnailS3bucketId)
                    bundle.putString(PrefConstent.VIDEO_ID, videoId)
                    bundle.putString(PrefConstent.MEDIA_ID, mediaId.toString())
                    findNavController().navigate(R.id.videofragment, bundle)
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
                        if (eventList[pos].media != null) {
                            if (eventList[pos].media?.size!! >= 1) {
                                eventList[pos].media?.get(0)?.run {
                                    isPlayByPlayButton = false
                                    mediaId = this.id
                                    videoId = this.bunnyId
                                    val bundle = Bundle()
                                    bundle.putString(PrefConstent.VIDEO_URL, "")
                                    bundle.putString(
                                        PrefConstent.PLAY_BACK_DURATION,
                                        this.playbackDuration
                                    )
                                    bundle.putBoolean(PrefConstent.ISRESUME, isPlayByPlayButton)
                                    bundle.putString(PrefConstent.VIDEO_THUMB, thumbnailS3bucketId)
                                    bundle.putString(PrefConstent.VIDEO_ID, videoId)
                                    bundle.putString(PrefConstent.MEDIA_ID, mediaId.toString())
                                    findNavController().navigate(R.id.videofragment, bundle)
                                }


//                                eventList[pos].media?.forEach {
//                                    if (it.isLastPlayed) {
//                                        isPlayByPlayButton = false
//                                        mediaId = it.id
//                                        videoId = it.bunnyId
//                                        val bundle = Bundle()
//                                        bundle.putString(PrefConstent.VIDEO_URL, "")
//                                        bundle.putString(
//                                            PrefConstent.PLAY_BACK_DURATION,
//                                            it. playbackDuration
//                                        )
//                                        bundle.putBoolean(PrefConstent.ISRESUME, isPlayByPlayButton)
//                                        bundle.putString(
//                                            PrefConstent.VIDEO_THUMB,
//                                            it.thumbnailS3bucketId
//                                        )
//                                        bundle.putString(PrefConstent.VIDEO_ID, videoId)
//                                        bundle.putString(PrefConstent.MEDIA_ID, mediaId.toString())
//                                        findNavController().navigate(R.id.videofragment, bundle)
//                                    }
//                                }

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

                        eventVideosMore()
                    }

                    else -> {}
                }


                isMenuOpened = true
            }

            adapter = eventAdapter
        }
        eventFocus()

    }

    fun eventVideosMore() {
        viewModel.getUserVideos(requireActivity(), page, 10, auth_pin, projectId.toInt(), phone)
        observe()
    }

    private fun observe() {
        viewModel._homeLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {

                    Log.e(
                        "hfhddnub",
                        "$isEventPagination page $page pagination ${it.data?.data?.events?.size}" + it.data?.data.toString()
                    )
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
                            // binding.ivLogout.visible()
                            /// background

                            // event video

//                            events?.filter { if (it.media?.filter { if (it.isLastPlayed)})}
                            lifecycleScope.launch(Dispatchers.IO) {
                                data.backgroundMedia?.run {
                                    if (this.isNotEmpty()) {
                                        images.addAll(this as ArrayList<BackgroundMediaItem>)
                                        withContext(Dispatchers.Main) {
                                            sliderInit()
                                        }
                                    }
                                }
//                                val filteredEvents = events?.filter { event ->
//                                    event.media?.any { it.isLastPlayed } == true
//                                }
//
//                                if (filteredEvents != null && filteredEvents.isNotEmpty()) {
//                                    filteredEvents.forEach {
//                                        var media = it.media?.filter { it.isLastPlayed }
//                                        Log.e("hhutyuigjf", "knkndv ${it?.media?.size} data $media")
//                                        if (media != null && media.size > 0) {
//                                            if (media[0].isLastPlayed) {
//                                                isLastPlay = true
//                                                lastVideoUrl =""
//                                                lastVideoDuration = media[0].playbackDuration
//                                                lastVideoThumb = media[0].thumbnailS3bucketId
////                                                mediaId = media[0].id
//                                                videoId = media[0].bunnyId
//                                                lifecycleScope.launch(Dispatchers.Main) {
//                                                    binding.tvPlay.setText(
//                                                        "resume"
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    var media = events?.get(0)?.media
//                                    media?.get(0)?.run {
//                                        isLastPlay = false
//                                        lastVideoUrl = ""
//                                        lastVideoDuration = "0"
//                                        lastVideoThumb = thumbnailS3bucketId
////                                        mediaId = id
//                                        videoId = this.bunnyId
//                                        lifecycleScope.launch(Dispatchers.Main) {
//                                            binding.tvPlay.setText(
//                                                "play"
//                                            )
//                                        }
//                                    }
//                                }


                                var found = false
                                if (events != null && events.size > 0) {
                                    var reverseList = events.reversed()
                                    for (i in 0 until reverseList.size) {
                                        var mediaReverseList = reverseList[i].media?.reversed()
                                        if (mediaReverseList != null) {
                                            for (j in 0 until mediaReverseList.size) {
                                                var media = mediaReverseList[j]
                                                if (media.isLastPlayed) {
                                                    lastVideoDuration = media.playbackDuration
                                                    lastVideoThumb = media.thumbnailS3bucketId
                                                    videoId = media.bunnyId
                                                    lastVideoUrl = ""
                                                    mediaId = media.id
                                                    isLastPlay = true
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

//                                    events?.reversed()?.forEach { event ->
//                                        // Reverse the media list for each event
//                                        event.media?.reversed()?.forEach { media ->
//                                            if (media.isLastPlayed) {
//                                                // Once a match is found, capture the details and exit
//                                                lastVideoDuration = media.playbackDuration
//                                                lastVideoThumb = media.thumbnailS3bucketId
//                                                videoId = media.bunnyId
//                                                lastVideoUrl = ""
//
//                                                // Log the captured media details
//                                                Log.e(
//                                                    "lastmediapllll",
//                                                    "Duration: $lastVideoDuration, Thumb: $lastVideoThumb, VideoID: $videoId"
//                                                )
//                                                binding.tvPlay.setText(
//                                                    "resume"
//                                                )
//                                                found = true
//                                                return@forEach  // Exit the inner loop (media list)
//                                            }
//                                        }
//
//                                        // If found, break the outer loop as well
//                                        if (found) return@forEach
//
//                                    }
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
                                    }else{
                                         withContext(Dispatchers.Main){ binding.tvPlay.setText("resume")}
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



                            Log.e(
                                "hhutyuigjf",
                                "last video data $isLastPlay $lastVideoDuration video url $lastVideoUrl thumb $lastVideoThumb"
                            )

                        }



                        dismissProgress()
                        Log.e(
                            "dadaewed",
                            crewList.toString() + "dhbdh \n" + this.crewMembers.toString()
                        )
                    }

                }

                is MyResource.isError -> {
                    dismissProgress()
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

    fun durationObserve(event: Int, mediaId: Int, videoDuration: Long) {
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
                        "hduudhuirjirj",
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

    fun filterItem(eventId: Int, mediaId: Int, duration: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            var newList = homeFragment.eventAdapter.getList()

            val eventIndex = newList.indexOfFirst { it.eventId == eventId }
            Log.e("filteridwith", "duration $duration media id $mediaId eventId $eventId before event index $eventIndex update $newList")
            try {
                var newMedia = newList[eventIndex].media
                if (newMedia != null && newMedia.isNotEmpty()) {
                    var mediaIndex = newList[eventIndex].media?.indexOfFirst { it.id == mediaId }
                    delay(1000)

                    homeFragment.eventAdapter.updateDuration(eventIndex, mediaIndex, duration)
                    var after = homeFragment.eventAdapter.getList()
                    Log.e("filteridwith", "$mediaId after media index $mediaIndex update$after")
                }
            } catch (e: Exception) {
                Log.e("filteridwith", "crashed $e")
            }

        }
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            tvProjectTitle.text = proTitle.toString()
            tvProjectDesc.text = proDesc.toString()
            tvPlay.requestLayout()
            tvPlay.invalidate()
            if (tvProjectTitle.text.toString().isNotEmpty()) {
                clTitle.visible()
//                ivLogout.visible()
                ivTrailer.visible()
                tvPlay.visible()
            }
            Log.e(
                "resumehandle",
                " $isLastPlay videoduraion $videoduraion isFirstVideo $isFirstVideo  hfhh $eventFocusPos ncdjknv ${isDrawerOpen}"
            )
            if (isDrawerOpen) {
                if (isTrailer) {
                    trailerDrawer()
                } else {
                    drawerView()
                }
                drawerVideoFocus()
            }
            if (isFirstVideo) {
                sliderInit()
            }
            isFirstVideo = true

            /// check if user played video or not

            if (isLastPlay) {
                tvPlay.setText("resume")
            } else {
                tvPlay.setText("play")
            }
        }
//        if (!isTrailer) {
//            savePlayback()
//        }

    }

    fun eventFocus() = with(binding) {
        lifecycleScope.launch {
            Log.e("kdmcdkmc", "$eventFocusPos dmvdmv $focusView ")
            if (focusView == StreamEnum.INDECATOR_VIEW) {
//                customIndicator.requestFocus()
                ivTrailer.requestFocus()
            } else if (isDrawerOpen) {
                drawerVideoFocus()
            } else {
                eventVideoFocus()
            }
        }

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
        Log.e("focussss", "focus $focusView")
        when (focusView) {
            StreamEnum.LOGOUT_VIEW -> {
                ivLogout.requestFocus()
            }

            StreamEnum.HOME_CLOSE -> {
                ivHomeCross.requestFocus()
            }

            StreamEnum.PLAY_RESUME -> {
                tvPlay.requestFocus()
            }

            StreamEnum.TRAILER -> {
                ivTrailer.requestFocus()
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
        }
        binding.rvBackgVideo.isfirst = true
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity()?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding.rvBackgVideo.apply {
            pauseVideo()
            //  playerHandler.pause()
            playerHandler.release()
        }
    }

}
