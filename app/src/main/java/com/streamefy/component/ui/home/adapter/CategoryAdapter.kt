package com.streamefy.component.ui.home.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.streamefy.R
import com.streamefy.component.ui.home.categoryModel.CateModel


class CategoryAdapter(private val context: Activity, private val cateList: List<CateModel>, var callBack:(Int)->Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoriView>() {


    override fun onBindViewHolder(viewHolder: CategoriView, position: Int) {
        var data = cateList[position]
        viewHolder.apply {
            lpVideoProgres.progress=data.progress
            tvSubtitle.text=data.subTitle
            tvTitle.text=data.title
//            imageView.loadAny(data.image)

            imageView.setImageResource(data.image)

            tvMore.setOnClickListener {
                callBack.invoke(position)
            }

        }

    }
    class CategoriView(itemView: View) : ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivCate)
        val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: AppCompatTextView = itemView.findViewById(R.id.tvSubtitle)
        val lpVideoProgres: LinearProgressIndicator = itemView.findViewById(R.id.lpVideoProgres)
        val tvMore: AppCompatTextView = itemView.findViewById(R.id.tvMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriView {
        val inflate = context.layoutInflater.inflate(R.layout.home_category_item, parent,false)
        return CategoriView(inflate)
    }

    override fun getItemCount(): Int=cateList.size
}
