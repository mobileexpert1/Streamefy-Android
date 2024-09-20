package com.streamefy.component.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.CircularProgressDialog
import com.streamefy.component.base.ExitDialog
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.adapter.CategoryAdapter
import com.streamefy.component.ui.home.adapter.DrawerAdapter
import com.streamefy.component.ui.home.adapter.SliderAdapter
import com.streamefy.component.ui.home.categoryModel.CateModel
import com.streamefy.component.ui.home.model.BackgroundMediaItem
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.component.ui.home.model.HomeResponse
import com.streamefy.component.ui.home.model.MediaItem
import com.streamefy.data.KoinCompo
import com.streamefy.data.KoinCompo.homeVm
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.ExitDialogBinding
import com.streamefy.databinding.FragmentHomeBinding
import com.streamefy.network.MyResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun bindView(): Int = R.layout.fragment_home
//    val images = listOf(
//        R.drawable.home_theme,
//        R.drawable.app_icon_your_company,
//        R.drawable.home_theme,
//        R.drawable.movie,
//        R.drawable.home_theme
//    )
    val  images= ArrayList<BackgroundMediaItem>()
    var selectedTitle = ""
    var isMenuOpened = false
    var auth_pin = "Z1U5"
    var phone = ""
    var page = 1

    private val eventList = ArrayList<EventsItem>()
    private val mediaList = ArrayList<MediaItem>()
    lateinit var eventAdapter: CategoryAdapter
    lateinit var mediaAdapter: DrawerAdapter
    var isFirst=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  progressDialog= CircularProgressDialog(requireContext())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth_pin = SharedPref.getString(PrefConstent.AUTH_PIN).toString()
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        if (isFirst) {
            isFirst=false
            //getUserData()

        }
        eventView()
        binding.apply {



            ivClose.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.END)
                isMenuOpened = false
            }
            ivLogout.setOnClickListener {
                SharedPref.clearData()

                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true) // Set inclusive to true
                    .build()

                // Navigate to home fragment with the options
                findNavController().navigate(R.id.loginFragment, null, navOptions)

            }
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

    fun sliderInit()= with(binding){
        val adapter = SliderAdapter(requireActivity(), images) {
            Log.e("amcdanc", "sknmcjiadnc  $it")
        }
        imageSlider.setSliderAdapter(adapter)
        imageSlider.startAutoCycle()
        customIndicator.setIndicatorCount(images.size, 0)

        imageSlider.setCurrentPageListener {
            Log.e("aggggg", "sknmcjiadnc  $it")
            customIndicator.updateIndicator(it)
        }
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
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            mediaAdapter = DrawerAdapter(requireActivity(), mediaList) {
//                drawerLayout.closeDrawer(GravityCompat.END)
                 var bundle=Bundle()
                bundle.putString(PrefConstent.VIDEO_URL,mediaList[it].hlsPlaylistUrl)
                findNavController().navigate(R.id.videofragment,bundle)
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
//                description = "testing",
//                bunnyId = "",
//                thumbnailS3bucketId = "",
//                id = 0)
//            mediaList.add(model1)
//        }
// test case
        for (i in 1 until 10){
            var model1=EventsItem(
                eventId = i,
                eventTitle =  "Pre wedding No $i",
                media = mediaList ,
                userId = i,
                userName = "streamify"
            )
            eventList.add(model1)
        }



        rvCategory.apply {
            requestFocus()
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            eventAdapter = CategoryAdapter(requireActivity(), eventList) { pos,type->
                selectedTitle = eventList[pos].eventTitle

                var bundle=Bundle()
                bundle.putString(PrefConstent.VIDEO_URL,""
                    //eventList[pos].media?.get(0)?.hlsPlaylistUrl
                )
                findNavController().navigate(R.id.videofragment,bundle)

//                when(type){
//                    StreamEnum.SINGLE->{
//                        var bundle=Bundle()
//                        bundle.putString(PrefConstent.VIDEO_URL,""
//                            //eventList[pos].media?.get(0)?.hlsPlaylistUrl
//                        )
//                        findNavController().navigate(R.id.videofragment,bundle)
//                    }
//                    StreamEnum.MORE->{
//                        drawerLayout.openDrawer(GravityCompat.END)
//                        eventList[pos].media?.run {
//                            // if(isNotEmpty()){
//                            mediaList.clear()
//                            mediaList.addAll(eventList[pos].media as ArrayList<MediaItem>)
//                            drawerView()
//                            //  }
//                        }
//                    }
//                }



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
                    dismissProgress()
                    Log.e("feffefef", it.data?.data.toString())
                    it.data?.data?.run {
                        eventList.clear()
                        eventAdapter.update(events as ArrayList<EventsItem>)

                        this.backgroundMedia?.run {
                            if (this.isNotEmpty()) {
                                images.addAll(this as ArrayList<BackgroundMediaItem>)
                                sliderInit()
                            }
                        }

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
        if (binding.drawerLayout.isVisible) {
            drawerView()
        }

    }
}
