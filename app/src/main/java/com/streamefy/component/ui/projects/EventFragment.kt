package com.streamefy.component.ui.projects

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.ExitDialog
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.pin_authentication.dialog.ConfirmPinDialog
import com.streamefy.component.ui.pin_authentication.model.ResetPinRequest
import com.streamefy.component.ui.projects.model.ProjectRequest
import com.streamefy.component.ui.projects.model.ResponseItem
import com.streamefy.component.ui.projects.model.remove.RemoveProjectRequest
import com.streamefy.component.ui.projects.viewmodel.ProjectsVM
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentEventBinding
import com.streamefy.network.MyResource
import com.streamefy.utils.invisible
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class EventFragment : BaseFragment<FragmentEventBinding>() {
    override fun netStatus() {}
    override fun bindView(): Int = R.layout.fragment_event
    lateinit var projectAdapter: ProjectsAdapter
    var list = ArrayList<ResponseItem>()
    private val viewModel: ProjectsVM by viewModel()
    var phone = ""
    var isHome = false
    var isPrimaryuser = false
    var projectId = ""

    companion object {
        lateinit var eventFragment: EventFragment
        var focusedIndex = 0
        var isDark=false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         isDark=   SharedPref.getBoolean(PrefConstent.IS_DARK)
        eventFragment = this
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        isPrimaryuser = SharedPref.getBoolean(PrefConstent.ISPRIMARY_USER)
        val app_background = SharedPref.getString(PrefConstent.AUTH_BACKGROUND).toString()
        binding.ivbackground.loadUrl(app_background)

        projectId = SharedPref.getString(PrefConstent.PROJECT_ID).toString()
        arguments?.run {
            isHome = getBoolean(PrefConstent.ISHOME)
        }
        binding.apply {
            if (isHome) {
                ivBack.invisible()
            }
        }

        resetColor()
        focusable()
        clicable()
        rvInit()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    ExitDialog(requireActivity()).show()
                }
            })
    }

    private fun resetColor()= with(binding) {
//handle themes
        if (isDark) {
            textView2.isEnabled=true
        } else {
            textView2.isEnabled=false
        }
    }


    private var selectedItem = 0
    private fun rvInit() = with(binding) {
//  Handle Event listing
        rvEvent.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            projectAdapter = ProjectsAdapter(requireActivity(), list) { index, streamEnum ->
                val data = list[index]
                when (streamEnum) {
                    StreamEnum.LAST_EVENT -> {
//                      navigate to the PIN screen if you want to add event
                        SharedPref.setString(PrefConstent.PROJECT_NAME, "New Event")
                        val bundle = Bundle()
                        bundle.putInt(PrefConstent.PROJECT_ID, 0)
                        bundle.putString(PrefConstent.PHONE_NUMBER, phone)
                        bundle.putString(PrefConstent.PROJECT_NAME, "New Event")
                        bundle.putBoolean(PrefConstent.ISHOME, false)
                        findNavController().navigate(
                            R.id.action_projectfragment_to_pinAuthenticationFragment,
                            bundle
                        )
                    }

                    StreamEnum.RESET_PIN -> {
                        selectedItem = index
                        /// handle reset pin functionality
                        SharedPref.setString(PrefConstent.PROJECT_NAME, data.name)
                        ConfirmPinDialog(requireContext()) {
                            if (it) {
                                viewModel.resetPin(
                                    requireContext(),
                                    ResetPinRequest(data.id, phone)
                                )
                                resetObserve()
                            }
                        }.show()
                    }

                    StreamEnum.REMOVE_PROJECT -> {
                        selectedItem = index
                        EventRemoveDialog(requireContext()) {
                        //    Toast.makeText(context, "Removed Project..", Toast.LENGTH_SHORT).show()
                            viewModel.removeProject(
                                    requireContext(),
                                    RemoveProjectRequest(data.id, phone)
                            )
                                removeProjectObserve()
                        }.show()
                    }

                    StreamEnum.SINGLE -> {
//                        Handle navigation functionality. start with landing if you already added event PIN otherwise open Pin Screen
                        if (data.isAuthorize) {
                            SharedPref.setBoolean(PrefConstent.ISLOGIN, true)
                            SharedPref.setBoolean(PrefConstent.ISPRIMARY_USER, data.isPrimary)
                            SharedPref.setString(PrefConstent.AUTH_PIN, "")
                            SharedPref.setString(PrefConstent.PROJECT_ID, data.id.toString())
                            if (isAdded) {
                                findNavController().navigate(R.id.homefragment)
                            }
                        } else {
                            SharedPref.setString(PrefConstent.PROJECT_NAME, list[index].name)
                            val bundle = Bundle()
                            bundle.putInt(PrefConstent.PROJECT_ID, list[index].id)
                            bundle.putString(PrefConstent.PHONE_NUMBER, phone)
                            bundle.putString(PrefConstent.PROJECT_NAME, list[index].name)
                            bundle.putBoolean(PrefConstent.ISHOME, false)
                            findNavController().navigate(
                                R.id.action_projectfragment_to_pinAuthenticationFragment,
                                bundle
                            )
                        }
                    }

                    else -> {}
                }

            }
            adapter = projectAdapter
        }
    }

    private fun clicable() = with(binding) {
        ivBack.setOnClickListener {
            if (isPrimaryuser) {
                if (!isHome) {
                    findNavController().navigate(R.id.loginFragment)
                } else {
//                    Show exit dialog
                    ExitDialog(requireActivity()).show()
                }
            } else {
                findNavController().navigate(R.id.loginFragment)
            }
        }
    }
//    Handle button focus and key movement
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
                    resources.getDimensionPixelSize(R.dimen._17sdp)
                params.height = resources.getDimensionPixelSize(R.dimen._17sdp)
                ivBack.layoutParams = params
            } else {
                val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                params.width = resources.getDimensionPixelSize(R.dimen._15sdp)
                params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                ivBack.layoutParams = params
            }
        }
        rvEvent.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                }
                StreamEnum.LEFT_DPAD_KEY -> {
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
                    if (it.data?.response == null) {
//                        add event if event not available
                        list.clear()
                        list.add(ResponseItem(isLast = true, isPrimary = false))
                        projectAdapter.update(list)
                        lifecycleScope.launch {
                            binding.rvEvent.apply {
                                binding.ivBack.clearFocus()
                                isFocusable = true
                                isFocusableInTouchMode = true
                                delay(1000)
                                requestFocus()
                                post {
                                    getChildAt(0)?.requestFocus()
                                }
                            }
                        }

                    } else {
                        it.data?.run {
//                            add all event to the list
                            list.clear()
                            list.addAll(this.response as ArrayList<ResponseItem>)
                            lifecycleScope.launch {
//                                handle add event button at end of event list
                                list.add(ResponseItem(isLast = true, isPrimary = false))
                                if (projectId.isNotEmpty()) {
                                    focusedIndex = list.indexOfFirst { it.id == projectId.toInt() }
                                }
                                withContext(Dispatchers.Main) {
                                    projectAdapter.update(list)
                                }

                                binding.rvEvent.apply {
                                    binding.ivBack.clearFocus()
                                    isFocusable = true
                                    isFocusableInTouchMode = true
                                    delay(1000)
                                    requestFocus()
                                    post {
                                        getChildAt(0)?.requestFocus()
                                    }
                                }
                            }
                        }
                    }
                    dismissProgress()
                }

                is MyResource.isError -> {
                    dismissProgress()
//                    navigate to the PIN screen
                    if (it.error == "No primary projects found for the user.") {
                        SharedPref.setBoolean(PrefConstent.ISPRIMARY_USER, false)
                        SharedPref.setString(PrefConstent.PROJECT_NAME, "")
                        SharedPref.setString(PrefConstent.PROJECT_ID, "0")
                        val name = SharedPref.getString(PrefConstent.FULL_NAME).toString()
                        val bundle = Bundle()
                        bundle.putInt(PrefConstent.PROJECT_ID, 0)
                        bundle.putString(PrefConstent.PHONE_NUMBER, phone)
                        bundle.putString(PrefConstent.FULL_NAME, name)
                        bundle.putBoolean(PrefConstent.ISHOME, false)
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.projectfragment, true) // Pop fragment B from the stack
                            .build()
                        findNavController().navigate(
                            R.id.event_to_pin_no_projects_found,
                            bundle, navOptions
                        )
                    }

                }

                else -> {}
            }
        }
    }

    private fun resetObserve() {
//        Handle reset PIN functionality
        viewModel.resetData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    dismissProgress()
                    SharedPref.setBoolean(PrefConstent.ISRESET_PIN, true)
                    requireContext().showMessage(it.data?.response.toString())
                    list[selectedItem].isAuthorize = false
                    projectAdapter.updateAuth(selectedItem)
                }

                is MyResource.isError -> {
                    dismissProgress()
                }

                else -> {}
            }
        }
    }

    // remove project observer
    private fun removeProjectObserve() {
//        Handle remove project functionality
        viewModel.removeProjectData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    dismissProgress()

                    if (it.data!!.response!=null){
                        Log.e("call",it.data!!.response)
                        // project removed successfully
                        list.removeAt(selectedItem)
                        // Notify adapter about item removed
                        projectAdapter.notifyItemRemoved(selectedItem)
                    }

                }

                is MyResource.isError -> {
                    dismissProgress()
                }

                else -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getProject(requireContext(), ProjectRequest(phone))
        observe()
    }
}