package com.streamefy.component.ui.home

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
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
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentHomeBinding
import com.streamefy.network.MyResource
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import com.streamefy.utils.visible
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun bindView(): Int = R.layout.fragment_home
    private val viewModel: HomeVm by viewModel()

    //    val images = listOf(
//        R.drawable.home_theme,
//        R.drawable.app_icon_your_company,
//        R.drawable.home_theme,
//        R.drawable.movie,
//        R.drawable.home_theme
//    )
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
    var currentWidth = 0
    var currentHeight = 0
    var firstIndecator = true
    var isEventPagination = false

    var focusView = StreamEnum.BOTTOM_EVENT_VIEW

    companion object {
        lateinit var homeFragment: HomeFragment

    }

    var background_current_play_duration = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  progressDialog= CircularProgressDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth_pin = SharedPref.getString(PrefConstent.AUTH_PIN).toString()
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        homeFragment = this
        isEventPagination = false
        if (isFirst) {
            getUserData()
        }
        eventView()
        creatorView()
        foucusView()
        binding.apply {


            ivLogout.setOnClickListener {
                LogoutDialog(requireContext()) {
                    SharedPref.clearData()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.homefragment, true) // Set inclusive to true
                        .build()
                    // Navigate to home fragment with the options
                    findNavController().navigate(R.id.loginFragment, null, navOptions)
                }.show()

            }


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
//                    drawerView.requestFocus()
                    isDrawerOpen = true
                    rvCategory.apply {
                        post {
                            getChildAt(eventFocusPos)?.clearFocus()
                        }
                    }
                    rvDrawer.post {
                        rvDrawer.getChildAt(0)?.requestFocus()
                    }

                }

                override fun onDrawerClosed(drawerView: View) {
                    Log.e("dndjvn", "drawer close")
                    isDrawerOpen = false
                    rvCategory.apply {
                        post {
                            getChildAt(eventFocusPos)?.requestFocus()
                        }
                    }
                    rvDrawer.post {
                        rvDrawer.getChildAt(0)?.clearFocus()
                    }
                }

                override fun onDrawerStateChanged(newState: Int) {
                    // Handle state change if needed
                }
            })

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
        ivLogout.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    rvCategory.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {

//                    rvBackgVideo.isFocusableInTouchMode = false
//                    rvBackgVideo.isFocusable = false
//                    rvBackgVideo.requestFocus()
                    customIndicator.requestFocus()
                }

                StreamEnum.LEFT_DPAD_KEY -> {
                    rvCategory.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    customIndicator.requestFocus()
                }

                else -> {}
            }
        }
        ivLogout.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusView = StreamEnum.LOGOUT_VIEW
                // Change size when focused
                val params = ivLogout.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._20sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                ivLogout.layoutParams = params
                ivLogout.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_lselected_logout)
            } else {
                // Revert size when not focused
                val params = ivLogout.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivLogout.layoutParams = params
                ivLogout.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_logout)
            }
        }
        rvCategory.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    customIndicator.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
//                    rvBackgVideo.requestFocus()
                    ivLogout.requestFocus()
                }

                else -> {}
            }
        }
        rvCategory.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusView = StreamEnum.BOTTOM_EVENT_VIEW
                rvCategory.apply {
                    post {
                        getChildAt(eventFocusPos)?.requestFocus()
                    }
                }
            }

        }
        customIndicator.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {

                    if (rvBackgVideo.targetPosition != 0) {
                      //  ivLogout.requestFocus()
                  //  } else {
                        // rvBackgVideo.backScroll(rvBackgVideo.targetPosition)
                        val currenPos = rvBackgVideo.targetPosition - 1
                        binding.rvBackgVideo.smoothScrollToPosition(currenPos)

                    }
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    if (rvBackgVideo.targetPosition < rvBackgVideo.mediaObjects.size - 1) {
                       // rvCategory.requestFocus()
                   // } else {
                        val currenPos = rvBackgVideo.targetPosition + 1
                        binding.rvBackgVideo.smoothScrollToPosition(currenPos)
                    }
                }

                StreamEnum.LEFT_DPAD_KEY -> {
                    ivLogout.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    rvCategory.requestFocus()
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
//                val params = customIndicator.layoutParams as ConstraintLayout.LayoutParams
//
//                if (firstIndecator) {
//                    currentWidth = params.width
//                    currentHeight = params.height
//                    firstIndecator=false
//                }
//                val additionalSize = resources.getDimensionPixelSize(R.dimen._17sdp)
//                params.height = currentHeight + additionalSize
//                customIndicator.layoutParams = params

            } else {
//                val params = customIndicator.layoutParams as ConstraintLayout.LayoutParams
//                val decreaseSize = resources.getDimensionPixelSize(R.dimen._5sdp)
//
////                currentWidth = params.width
////                currentHeight = params.height
//                params.width = maxOf(0, currentWidth - decreaseSize)
//                params.height = maxOf(0, currentHeight - decreaseSize)
//                customIndicator.layoutParams = params

                customIndicator.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(), R.color._8b000000
                    )
                )
            }
        }

        ivClose.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusView = StreamEnum.DRAWER_VIEW
                // Change size when focused
                val params = ivClose.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._20sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                ivClose.layoutParams = params
                // ivLogout.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_lselected_logout)
            } else {
                // Revert size when not focused
                val params = ivClose.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivClose.layoutParams = params
                //ivLogout.background = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_logout)
            }
        }

    }

    private fun creatorView() = with(binding) {
        rvCreators.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            creatorsAdapter = CreatorsAdapter(requireActivity(), crewList) {}
            adapter = creatorsAdapter
        }
    }

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
                       var  newPos =
                            (rvBackgVideo.recyclerview?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        Log.e("skncksnc", "${rvBackgVideo.targetPosition} current $${rvBackgVideo.targetPosition} new index $newPos skcks ${mediaObjects.size} ")
                        customIndicator.updateIndicator(newPos)
                    }
                }
            })
        }

        customIndicator.setIndicatorCount(images.size, 0)

        if (images.size > 1) {
            binding.customIndicator.visible()
        }
        //  binding.ivLogout.visible()
//        val adapter = SliderAdapter(requireActivity(), images) {
//            Log.e("amcdanc", "sknmcjiadnc  $it")
//        }
//
//        imageSlider.setSliderAdapter(adapter)
//        imageSlider.startAutoCycle()
//        customIndicator.setIndicatorCount(images.size, 0)
//
//        imageSlider.setCurrentPageListener {
//            Log.e("aggggg", "sknmcjiadnc  $it")
//            customIndicator.updateIndicator(it)
//        }
    }

    private fun getUserData() {
        viewModel.getUserVideos(requireActivity(), page, 10, auth_pin, phone)
        observe()
    }

    private fun showCustomDialog() {
        ExitDialog(requireActivity()).show()
    }

    private fun drawerView() = with(binding) {

        tvTitle.setText(selectedTitle)
        // testing
//        for (i in 1 until 10){
//            var model1=MediaItem(
//                 size = "4",
//                 format = "jpj",
//                 hlsPlaylistUrl = "",
//                 description = "testing",
//                 bunnyId = "",
//                 thumbnailS3bucketId = "",
//                 id = 0)
//            mediaList.add(model1)
//        }
////
        rvDrawer.apply {

            getChildAt(0)?.requestFocus()

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            mediaAdapter = DrawerAdapter(requireActivity(), mediaList) {
//                drawerLayout.closeDrawer(GravityCompat.END)
                drawerItemFocus = it
                var bundle = Bundle()
                bundle.putString(PrefConstent.VIDEO_URL, mediaList[it].hlsPlaylistUrl)
                bundle.putBoolean(PrefConstent.SMART_REVISION, mediaList[it].isSmartRevision)
                findNavController().navigate(R.id.videofragment, bundle)
//                        findNavController().navigate(R.id.dynamicscreen)
            }

            adapter = mediaAdapter
        }
    }

    private fun eventView() = with(binding) {

//        cateList.add(
//            CateModel(
//                title = "Pre wedding",
//                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
//                image = R.drawable.home_theme, progress = 70
//            )
//        )

//        for (i in 1 until 4){
//            var model1=MediaItem(
//                size = "4",
//                format = "jpj",
//                hlsPlaylistUrl = "",
//                description = "testing Lorem Ipsum is simply dummy text of the printing and Lorem Ipsum is simply dummy text of the printing and ",
//                bunnyId = "",
//                thumbnailS3bucketId = "",
//                id = 0)
//            mediaList.add(model1)
//        }
//// test case
//        for (i in 1 until 10){
//            var model1=EventsItem(
//                eventId = i,
//                eventTitle =  "Pre wedding No $i",
//                media = mediaList ,
//                userId = i,
//                userName = "streamify"
//            )
//            eventList.add(model1)
//        }


        rvCategory.apply {
            requestFocus()
            setHasFixedSize(true)

            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            eventAdapter = CategoryAdapter(requireActivity(), eventList) { pos, type ->
                selectedTitle = eventList[pos].eventTitle

//                var bundle=Bundle()
//                bundle.putString(PrefConstent.VIDEO_URL,""
//                    //eventList[pos].media?.get(0)?.hlsPlaylistUrl
//                )
//                findNavController().navigate(R.id.videofragment,bundle)

                Log.e("feffefef", "fnsdsnfs $type ")
                eventFocusPos = pos
                when (type) {
                    StreamEnum.SINGLE -> {
                        var bundle = Bundle()
                        bundle.putString(
                            PrefConstent.VIDEO_URL, eventList[pos].media?.get(0)?.hlsPlaylistUrl
                        )
                        findNavController().navigate(R.id.videofragment, bundle)
                    }

                    StreamEnum.MORE -> {
                        //  drawerLayout.requestFocus()
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

                        customIndicator.requestFocus()
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
    }

    fun eventVideosMore() {
        viewModel.getUserVideos(requireActivity(), page, 10, auth_pin, phone)
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

                        if (isEventPagination) {
                            if (events != null && events.isNotEmpty()) {
                                eventAdapter.pagination(events as ArrayList<EventsItem>)
                                isEventPagination = true
                                page++
                            }else{
                                isEventPagination=false
                            }
                            dismissProgress()
                        } else {
                            page++
                            isEventPagination = true
                            eventList.clear()
                            binding.ivLogout.visible()
                            /// background
                            this.backgroundMedia?.run {
                                if (this.isNotEmpty()) {
                                    images.addAll(this as ArrayList<BackgroundMediaItem>)
                                    sliderInit()

                                }
                            }
                            // event video
                            eventAdapter.update(events as ArrayList<EventsItem>)
                            binding.let {
                                this.project?.get(0)?.run {
                                    proTitle = projectTitle.toString()
                                    proDesc = projectDescription.toString()
                                    it.tvProjectTitle.text = proTitle.toString()
                                    it.tvProjectDesc.text = proDesc.toString()
                                }
                                it.rvCategory.requestFocus()
//                            it. rvBackgVideo.requestFocus()
                                it.projectlogo.loadUrl(this.logo)
                                proLogo = this.logo


                            }
                            crewList.clear()
                            //  this.crewMembers?.let { it1 -> crewList.addAll(it1) }

                            if (crewMembers != null && crewMembers.isNotEmpty()) {
                                // crewList.addAll(crewMembers)
                                creatorsAdapter.update(crewMembers as ArrayList<crewMembers>)
                            }
                            isFirst = false
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
                }
                else->{}
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            tvProjectTitle.text = proTitle.toString()
            tvProjectDesc.text = proDesc.toString()
            projectlogo.loadUrl(proLogo)
            if (tvProjectTitle.text.toString().isNotEmpty()) {
                ivLogout.visible()
            }
            Log.e("dndjvn", "$isFirstVideo  hfhh $eventFocusPos ncdjknv ${isDrawerOpen}")
            if (isDrawerOpen) {
                drawerView()
//                rvCategory.apply {
//                    post {
//                        getChildAt(eventFocusPos)?.clearFocus()
//                    }
//                }
                rvDrawer.post {
                    rvDrawer.getChildAt(drawerItemFocus)?.requestFocus()
                }
            }
//            else{
////                rvCategory.apply {
////                    post {
////                        getChildAt(eventFocusPos)?.requestFocus()
////                    }
////                }
//            }

//            lifecycleScope.launch {
//                delay(5000)
//                rvCategory.apply {
//                    post {
//                        getChildAt(eventFocusPos)?.requestFocus()
//                    }
//                }
//            }

//              rvBackgVideo.initializer()
            if (isFirstVideo) {
                sliderInit()
//                rvBackgVideo.initializer()
//                rvBackgVideo.resumeVideo(currentVideoDuration)

            }
            isFirstVideo = true
        }
        // background
        //

    }

    fun eventFocus() = with(binding) {
        lifecycleScope.launch {
            Log.e("kdmcdkmc", "$eventFocusPos dmvdmv $focusView")
            if (focusView == StreamEnum.INDECATOR_VIEW) {
                customIndicator.requestFocus()
            } else {
                rvCategory.apply {
                    post {
                        getChildAt(eventFocusPos)?.requestFocus()
                    }
                }
            }
        }

    }

    override fun onPause() {
        binding.rvBackgVideo.pauseVideo()
        binding.rvBackgVideo.isfirst = true
        super.onPause()
    }

}
