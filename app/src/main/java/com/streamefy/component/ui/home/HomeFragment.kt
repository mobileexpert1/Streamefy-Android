package com.streamefy.component.ui.home

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
import com.streamefy.component.ui.home.background.SliderAdapter
import com.streamefy.component.ui.home.model.BackgroundMediaItem
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.component.ui.home.model.MediaItem
import com.streamefy.component.ui.home.model.crewMembers
import com.streamefy.data.KoinCompo.homeVm
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentHomeBinding
import com.streamefy.network.MyResource
import com.streamefy.utils.loadUrl

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun bindView(): Int = R.layout.fragment_home

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

        if (isFirst) {
            getUserData()
        }
        eventView()
        creatorView()
        binding.apply {


            ivLogout.setOnClickListener {
                LogoutDialog(requireContext()){
                    SharedPref.clearData()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.homefragment, true) // Set inclusive to true
                        .build()
                    // Navigate to home fragment with the options
                    findNavController().navigate(R.id.loginFragment, null, navOptions)
                }.show()

            }
            ivLogout.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
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

            ivClose.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.END)
                isMenuOpened = false
            }
            ivClose.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
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



            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    // Handle drawer slide if needed
                }

                override fun onDrawerOpened(drawerView: View) {
                    // Set focus to the first item if needed
                    drawerView.requestFocus()
                    rvDrawer.post {
                        rvDrawer.getChildAt(0)?.requestFocus()
                    }
                }

                override fun onDrawerClosed(drawerView: View) {
                    binding.rvCategory.apply {
                        post {
                            getChildAt(eventFocusPos)?.requestFocus()
                        }
                    }
                }

                override fun onDrawerStateChanged(newState: Int) {
                    // Handle state change if needed
                }
            })

        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Show the custom dialog when back is pressed
                    showCustomDialog()
                }
            })

    }

    private fun creatorView() = with(binding) {
        rvCreators.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            creatorsAdapter = CreatorsAdapter(requireActivity(), crewList) {

            }
            adapter = creatorsAdapter
        }
    }

    fun sliderInit() = with(binding) {

        rvBackgVideo.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            setList(images)
            var backgadapter = BackgroundAdpater(requireActivity(), images)
            { index -> }
            adapter = backgadapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        Log.e("skncksnc", "skcks ${mediaObjects.size} data $mediaObjects")
                        customIndicator.updateIndicator(rvBackgVideo.targetPosition)
                    }
                }
            })

        }

        customIndicator.setIndicatorCount(images.size, 0)



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
        homeVm.getUserVideos(requireActivity(), page, 10, auth_pin, phone)
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
                var bundle = Bundle()
                bundle.putString(PrefConstent.VIDEO_URL, mediaList[it].hlsPlaylistUrl)
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
                eventFocusPos = pos
                when (type) {
                    StreamEnum.SINGLE -> {
                        var bundle = Bundle()
                        bundle.putString(
                            PrefConstent.VIDEO_URL,
                            eventList[pos].media?.get(0)?.hlsPlaylistUrl
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
                }


                isMenuOpened = true
            }

            adapter = eventAdapter
        }
    }


    private fun observe() {
        homeVm.homeLiveData.observe(requireActivity()) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {

                    Log.e("feffefef", it.data?.data.toString())
                    it.data?.data?.run {

                        eventList.clear()
                        eventAdapter.update(events as ArrayList<EventsItem>)
                        binding.let {
                            this.project?.get(0)?.run {
                                proTitle = projectTitle.toString()
                                proDesc = projectDescription.toString()
                                it.tvProjectTitle.text = proTitle.toString()
                                it.tvProjectDesc.text = proDesc.toString()
                            }
                            it.rvCategory.requestFocus()
                            it.projectlogo.loadUrl(this.logo)
                            proLogo = this.logo
                        }

                        /// background
                        this.backgroundMedia?.run {
                            if (this.isNotEmpty()) {
                                images.addAll(this as ArrayList<BackgroundMediaItem>)
                                sliderInit()
                            }
                        }
                        crewList.clear()
                        //  this.crewMembers?.let { it1 -> crewList.addAll(it1) }

                        if (crewMembers != null && crewMembers.isNotEmpty()) {
                            // crewList.addAll(crewMembers)
                            creatorsAdapter.update(crewMembers as ArrayList<crewMembers>)
                        }
                        isFirst = false
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
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            tvProjectTitle.text = proTitle.toString()+"0.01"
            tvProjectDesc.text = proDesc.toString()
            projectlogo.loadUrl(proLogo)
            if (drawerLayout.isVisible) {
                drawerView()
            }
            rvCategory.apply {
                post {
                    getChildAt(eventFocusPos)?.requestFocus()
                }
            }
            //  rvBackgVideo.initializer()
              rvBackgVideo.resumeVideo(currentVideoDuration)
        }
        // background
        sliderInit()

    }


    override fun onPause() {
        binding.rvBackgVideo.pauseVideo()
        binding.rvBackgVideo.isfirst=true
        super.onPause()
    }

}
