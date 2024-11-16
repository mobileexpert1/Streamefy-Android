package com.streamefy.component.ui.projects

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.HomeFragment
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.component.ui.pin_authentication.PinVM
import com.streamefy.component.ui.pin_authentication.dialog.ConfirmPinDialog
import com.streamefy.component.ui.projects.model.ProjectRequest
import com.streamefy.component.ui.projects.model.ResponseItem
import com.streamefy.component.ui.projects.viewmodel.ProjectsVM
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentEventBinding
import com.streamefy.network.MyResource
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showMessage
import org.koin.androidx.viewmodel.ext.android.viewModel

class EventFragment : BaseFragment<FragmentEventBinding>() {
    override fun bindView(): Int = R.layout.fragment_event
    lateinit var projectAdapter: ProjectsAdapter
    var list = ArrayList<ResponseItem>()
    private val viewModel: ProjectsVM by viewModel()
    var phone = ""
    var projectId: Int = 0

    companion object {
        lateinit var eventFragment: EventFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventFragment = this
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        focusable()
        clicable()
        rvInit()
        viewModel.getProject(requireContext(), ProjectRequest(projectId, phone))
        observe()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Show the custom dialog when back is pressed
                    findNavController().popBackStack()
                }
            })
    }

    private fun rvInit() = with(binding) {
        rvEvent.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            projectAdapter = ProjectsAdapter(requireActivity(), list) { index, streamEnum ->
                SharedPref.setBoolean(PrefConstent.ISCONFIRM_PIN, false)
                SharedPref.setString(PrefConstent.PROJECT_NAME, list[index].name)
                ConfirmPinDialog(requireContext()) {
                    if (it) {
                        projectId = list[index].id
                        viewModel.getProject(requireContext(), ProjectRequest(projectId, phone))
                        observe()
                    }
                }.show()
            }
            adapter = projectAdapter
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
        ivBack.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._20sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                ivBack.layoutParams = params
            } else {
                val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivBack.layoutParams = params
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


    private fun observe() {
        viewModel.projectLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    Log.e("sjxbjsbc", "ksjnckjanc ${it.data}")
                    if (it.data?.response == null) {
                        requireActivity().showMessage("PIN updated successfully")
                        findNavController().popBackStack()
                    } else {
                        it.data?.run {
                            list.clear()
                            list.addAll(this.response as ArrayList<ResponseItem>)
                            projectAdapter.update(list)
                            binding.rvEvent.apply {
                                post {
                                    getChildAt(0)?.requestFocus()
                                }
                            }
                        }
                    }
                    dismissProgress()
                }

                is MyResource.isError -> {
                    dismissProgress()
                }
            }
        }
    }

}