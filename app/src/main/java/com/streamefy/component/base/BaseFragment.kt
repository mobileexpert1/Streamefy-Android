package com.streamefy.component.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.streamefy.data.KoinCompo.progress

abstract class BaseFragment<B : ViewBinding> : Fragment() {
    lateinit var binding: B

    lateinit var progressDialog:CircularProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, bindView(), container, false)
        progressDialog= CircularProgressDialog(requireContext())
        return binding.root
    }

     fun showProgress(){
         progressDialog.show()
     }
    fun dismissProgress(){
        progressDialog.dismiss()
    }
    abstract fun bindView(): Int

    override fun onDestroyView() {
        super.onDestroyView()
        //unregister listener here
//        onBackPressedCallback.isEnabled = false
//        onBackPressedCallback.remove()
    }

}