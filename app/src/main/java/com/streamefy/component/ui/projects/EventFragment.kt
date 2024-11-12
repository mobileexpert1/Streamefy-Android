package com.streamefy.component.ui.projects

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.databinding.FragmentEventBinding
import com.streamefy.utils.remoteKey

class EventFragment : BaseFragment<FragmentEventBinding>() {
    override fun bindView(): Int = R.layout.fragment_event
    lateinit var projectAdapter:ProjectsAdapter
    var list= ArrayList<EventsItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        focusable()
        clicable()
        rvInit()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Show the custom dialog when back is pressed
                    findNavController().popBackStack()
                }
            })
    }

    private fun rvInit()= with(binding) {
      rvEvent.apply {
          setHasFixedSize(true)
          layoutManager=LinearLayoutManager(requireContext())
          projectAdapter=ProjectsAdapter(requireActivity(),list){
              index,streamEnum->

          }
          adapter=projectAdapter
      }
    }

    private fun clicable() = with(binding) {
        ivBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun focusable() = with(binding) {
        ivBack.remoteKey {
            when (it) {
                StreamEnum.DOWN_DPAD_KEY -> {
                    rvEvent.requestFocus()
                }

                else -> {}
            }
        }

        rvEvent.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                else -> {}
            }
        }

    }
}