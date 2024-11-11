package com.streamefy.country_code

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.streamefy.R
import com.streamefy.component.base.BaseDialog
import com.streamefy.country_code.model.CountryCodeModel
import com.streamefy.databinding.LayoutPickerDialogBinding


class CodeDialog(var activity: Activity,var list:ArrayList<CountryCodeModel>, var callBack: (Int) -> Unit) : BaseDialog<LayoutPickerDialogBinding>(activity, R.layout.layout_picker_dialog, R.style.TransparentDialogTheme) {
    override fun setupViews() {
        var selectedIndex=0
        var codeAdapter:CodeAdapter?=null
        binding.apply {
            imgDismiss.setOnClickListener {
                callBack.invoke(selectedIndex)
                dismiss()
            }
            recyclerCountryDialog.apply {
                setHasFixedSize(true)
                layoutManager=LinearLayoutManager(context)
                 codeAdapter=CodeAdapter(activity,list){
                    selectedIndex=it
                }
                adapter=codeAdapter
            }

            editTextSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    codeAdapter?.filter(editable.toString())
                }
            })

        }
    }
}