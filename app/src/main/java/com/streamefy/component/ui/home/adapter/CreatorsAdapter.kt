package com.streamefy.component.ui.home.adapter

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.component.ui.home.model.crewMembers
import com.streamefy.databinding.CreatorsItemBinding

class CreatorsAdapter(
    private val context: Activity,
    private val creatorList: ArrayList<crewMembers>,
    var callBack: (Int) -> Unit
) :
    RecyclerView.Adapter<CreatorsAdapter.CreatorsView>() {
    lateinit var binding: CreatorsItemBinding

    override fun onBindViewHolder(viewHolder: CreatorsView, position: Int) {
        var data = creatorList[position]
        binding.apply {
            tvCreators.setText(data.title + ":" + data.name)
        }

    }

    class CreatorsView(itemView: CreatorsItemBinding) : RecyclerView.ViewHolder(itemView.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatorsView {
        binding = CreatorsItemBinding.inflate(context.layoutInflater)
        return CreatorsView(binding)
    }

    override fun getItemCount(): Int = creatorList.size

    fun update(newList: ArrayList<crewMembers>) {
//        creatorList.clear()
        creatorList.addAll(newList)
        Log.e("sncsak","adck $newList")
        notifyDataSetChanged()
    }

}