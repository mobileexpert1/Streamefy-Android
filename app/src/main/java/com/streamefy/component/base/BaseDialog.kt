package com.streamefy.component.base

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.streamefy.R

abstract class BaseDialog<T : ViewDataBinding>(
    context: Context,
    private val layoutId: Int,
    style:Int
) : Dialog(context,style ) {
    protected val binding: T by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, null, false)
    }

    init {
        setContentView(binding.root)
        setupViews()
    }

    protected abstract fun setupViews()
}