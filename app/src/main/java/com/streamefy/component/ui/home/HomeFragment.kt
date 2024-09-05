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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.ExitDialog
import com.streamefy.component.ui.home.adapter.CategoryAdapter
import com.streamefy.component.ui.home.adapter.DrawerAdapter
import com.streamefy.component.ui.home.adapter.SliderAdapter
import com.streamefy.component.ui.home.categoryModel.CateModel
import com.streamefy.databinding.ExitDialogBinding
import com.streamefy.databinding.FragmentHomeBinding
import java.util.Collections

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun bindView(): Int = R.layout.fragment_home
    val images = listOf(
        R.drawable.home_theme,
        R.drawable.app_icon_your_company,
        R.drawable.home_theme,
        R.drawable.movie,
        R.drawable.home_theme
    )
    var selectedTitle = ""
    var isMenuOpened = false

    private val cateList = ArrayList<CateModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val adapter = SliderAdapter(requireActivity(), images) {
                Log.e("amcdanc", "sknmcjiadnc  $it")
            }
            imageSlider.setSliderAdapter(adapter)
            customIndicator.setIndicatorCount(images.size, 0)

            imageSlider.setCurrentPageListener {
                Log.e("aggggg", "sknmcjiadnc  $it")
                customIndicator.updateIndicator(it)
            }

            ivClose.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.END)
                isMenuOpened = false
            }
        }

        categoryview()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Show the custom dialog when back is pressed
                    showCustomDialog()
                }
            })
    }

    private fun showCustomDialog() {
        ExitDialog(requireActivity()).show()
    }

    private fun drawerView() = with(binding) {
        val draList = ArrayList<CateModel>()
        tvTitle.setText(selectedTitle)
        draList.add(
            CateModel(
                title = "Mehendi ceremony",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme
            )
        )
        draList.add(
            CateModel(
                title = "Wedding ceremony",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme, progress = 70
            )
        )
        draList.add(
            CateModel(
                title = "Reception ceremony",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme, progress = 70
            )
        )
        draList.add(
            CateModel(
                title = "Dinner ceremony",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme
            )
        )
        draList.add(
            CateModel(
                title = "Pre wedding",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme
            )
        )
        draList.add(
            CateModel(
                title = "Dance",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme
            )
        )
        draList.add(
            CateModel(
                title = "Pre wedding",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme, progress = 70
            )
        )

        rvDrawer.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity())
            var draAdapter = DrawerAdapter(requireActivity(), draList) {
//                drawerLayout.closeDrawer(GravityCompat.END)

                findNavController().navigate(R.id.videofragment)
//                        findNavController().navigate(R.id.dynamicscreen)
            }

            adapter = draAdapter
        }
    }

    private fun categoryview() = with(binding) {

        cateList.add(
            CateModel(
                title = "Mehendi ceremony",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme
            )
        )
        cateList.add(
            CateModel(
                title = "Wedding ceremony",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme, progress = 70
            )
        )
        cateList.add(
            CateModel(
                title = "Reception ceremony",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme, progress = 70
            )
        )
        cateList.add(
            CateModel(
                title = "Dinner ceremony",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme
            )
        )
        cateList.add(
            CateModel(
                title = "Pre wedding",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme
            )
        )
        cateList.add(
            CateModel(
                title = "Dance",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme
            )
        )
        cateList.add(
            CateModel(
                title = "Pre wedding",
                subTitle = "Lorem Ipsum is simply dummy text of the printing and",
                image = R.drawable.home_theme, progress = 70
            )
        )

        rvCategory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            var catAdapter = CategoryAdapter(requireActivity(), cateList) {
                selectedTitle = cateList[it].title
                drawerLayout.openDrawer(GravityCompat.END)

                drawerView()
                isMenuOpened = true
            }

            adapter = catAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.drawerLayout.isVisible) {
            drawerView()
        }

    }
}
