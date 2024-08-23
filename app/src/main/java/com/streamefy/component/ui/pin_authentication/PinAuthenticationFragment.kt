package com.streamefy.component.ui.pin_authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.databinding.FragmentPinAuthenticationBinding
import com.streamefy.utils.setupNextFocusOnDigit

class PinAuthenticationFragment : BaseFragment<FragmentPinAuthenticationBinding>() {
    override fun bindView(): Int = R.layout.fragment_pin_authentication
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            tvProceed.setOnClickListener {
                findNavController().navigate(R.id.homefragment)
            }
            et1.setupNextFocusOnDigit(et2)
            et2.setupNextFocusOnDigit(et3)
            et3.setupNextFocusOnDigit(et4)

        }

    }

}