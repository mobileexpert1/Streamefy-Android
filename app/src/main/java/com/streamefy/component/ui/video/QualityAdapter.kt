package com.streamefy.component.ui.video

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.R
import com.streamefy.component.ui.video.model.QualityModel
import com.streamefy.databinding.CreatorsItemBinding
import com.streamefy.databinding.QualityItemBinding

class QualityAdapter(
    var context: Activity,
    var list: ArrayList<QualityModel>,
    var callBack: (Int) -> Unit
) : RecyclerView.Adapter<QualityAdapter.qualityHolder>() {

    lateinit var binding: QualityItemBinding
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): qualityHolder {
//        var inflater=LayoutInflater.from(context).inflate(R.layout.quality_item,parent,false)
        binding = QualityItemBinding.inflate(context.layoutInflater,parent,false)
        return qualityHolder(binding)
    }

    override fun onBindViewHolder(holder: qualityHolder, position: Int) {
        var data = list[position]
        Log.e("sncksnc", "skcnks $data")
        binding.apply {
            if (data.isSelected) {
                tvquality.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
            } else {
                tvquality.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            tvquality.text = data.title
            tvquality.setOnClickListener {
                callBack.invoke(position)
                list.forEachIndexed { index, pair ->
                    if (index == position) {
                        pair.isSelected = true
                        tvquality.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.light_gray
                            )
                        )
                    } else {
                        pair.isSelected = false
                        tvquality.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    }
                    notifyDataSetChanged()
                }

            }


            tvquality.setOnFocusChangeListener { _, hasFocus ->
                Log.e("sjncjsbc","skck $hasFocus")
                if (hasFocus) {
                    tvquality.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
                } else{
                    if (!data.isSelected) {
                        tvquality.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    }else{
                        tvquality.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
                    }

                }
            }
        }
    }

    override fun getItemCount(): Int = list.size
    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    class qualityHolder(binding: QualityItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun update(newList: QualityModel) {
        list.add(newList)
        Log.e("sncksnc", "newList $newList")
        notifyDataSetChanged()
    }
}