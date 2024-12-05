package com.streamefy.component.ui.projects

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.streamefy.utils.gone
import com.streamefy.utils.invisible
import com.streamefy.utils.loadAny
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class EventFragment : BaseFragment<FragmentEventBinding>() {
    override fun netStatus() {}
    override fun bindView(): Int = R.layout.fragment_event
    lateinit var projectAdapter: ProjectsAdapter
    var list = ArrayList<ResponseItem>()
    private val viewModel: ProjectsVM by viewModel()
    var phone = ""
    var projectId: Int = 0
    var isHome = false
    var isPrimaryuser = false
    var   applogo=""
    companion object {
        lateinit var eventFragment: EventFragment
        var focusedIndex = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventFragment = this
        phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
        isPrimaryuser = SharedPref.getBoolean(PrefConstent.ISPRIMARY_USER)
        applogo = SharedPref.getString(PrefConstent.APP_LOGO).toString()

        arguments?.run {
            isHome = getBoolean(PrefConstent.ISHOME)
        }
        binding.ivApplogo.loadAny(applogo)
        binding.apply {
            if (isPrimaryuser) {
                if (isHome) {
                    ivBack.invisible()
                }
            }
        }
        focusable()
        clicable()
        rvInit()
//        viewModel.getProject(requireContext(), ProjectRequest(phone))
//        observe()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isPrimaryuser) {
                        if (!isHome) {
                            findNavController().navigate(R.id.loginFragment)
                        } else {
                            ExitDialog(requireActivity()).show()
                        }
                    } else {
                        findNavController().navigate(R.id.loginFragment)
                    }
                }
            })
    }

    private fun rvInit() = with(binding) {
        rvEvent.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            projectAdapter = ProjectsAdapter(requireActivity(), list) { index, streamEnum ->

                when (streamEnum) {
                    StreamEnum.LAST_EVENT -> {
                        // handle event add functionality
                    }

                    StreamEnum.SINGLE -> {
                        SharedPref.setBoolean(PrefConstent.ISCONFIRM_PIN, false)
                        SharedPref.setString(PrefConstent.PROJECT_NAME, list[index].name)
                        SharedPref.setString(PrefConstent.PROJECT_ID, list[index].id.toString())
                        val name = SharedPref.getString(PrefConstent.FULL_NAME).toString()
                        val bundle = Bundle()
                        bundle.putInt(PrefConstent.PROJECT_ID, list[index].id)
                        bundle.putString(PrefConstent.PHONE_NUMBER, phone)
                        bundle.putString(PrefConstent.FULL_NAME, name)

                        bundle.putBoolean(PrefConstent.ISHOME, false)
                        findNavController().navigate(
                            R.id.action_projectfragment_to_pinAuthenticationFragment,
                            bundle
                        )
//                ConfirmPinDialog(requireContext()) {
//                    if (it) {
//                        projectId = list[index].id
//                        viewModel.getProject(requireContext(), ProjectRequest(phone))
//                        observe()
//                    }
//                }.show()
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
                    ExitDialog(requireActivity()).show()
                }
            } else {
                findNavController().navigate(R.id.loginFragment)
            }
        }
    }

    private fun focusable() = with(binding) {
        ivBack.remoteKey {
            when (it) {
                StreamEnum.DOWN_DPAD_KEY -> {
                    rvEvent.requestFocus()
//                    rvEvent.isFocusable = true
//                    rvEvent.isFocusableInTouchMode = true
//                    rvEvent.post { rvEvent.getChildAt(focusedIndex)?.requestFocus() }
//                    rvEvent.scrollToPosition(focusedIndex)
                }

                else -> {}
            }
        }
        ivBack.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    resources.getDimensionPixelSize(R.dimen._17sdp) // Adjust to your desired size
                params.height = resources.getDimensionPixelSize(R.dimen._17sdp)
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
                    Log.e("sjxbjsbc", "ksjnckjanc ${it.data}")
                    if (it.data?.response == null) {
                        requireActivity().showMessage("PIN updated successfully")
                        findNavController().popBackStack()
                    } else {
                        it.data?.run {
                            list.clear()
                            list.addAll(this.response as ArrayList<ResponseItem>)
                           // list.add(ResponseItem(isLast = true))
                            projectAdapter.update(list)

                            binding.rvEvent.apply {
                                post {
                                    getChildAt(0)?.requestFocus()
                                }
                            }
//                            lifecycleScope.launch {
//                                delay(200)
//                                withContext(Dispatchers.Main){
//                                    projectAdapter.addItem(ResponseItem(isLast = true))
//                                    binding.rvEvent.adapter=projectAdapter
//                                    projectAdapter.notifyDataSetChanged()
//                                }
//                            }
                        }
                    }
                    dismissProgress()
                }

                is MyResource.isError -> {
                    dismissProgress()
                    if (it.error == "No primary projects found for the user.") {
                        SharedPref.setBoolean(PrefConstent.ISPRIMARY_USER, false)
                        SharedPref.setBoolean(PrefConstent.ISCONFIRM_PIN, false)
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
                            bundle,navOptions
                        )
                    }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        focusedIndex = 0
        viewModel.getProject(requireContext(), ProjectRequest(phone))
        observe()
    }
}