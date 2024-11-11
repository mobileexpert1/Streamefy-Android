package com.streamefy.country_code
import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.R
import com.streamefy.country_code.model.CountryCodeModel
import com.streamefy.databinding.CountryCodeItemBinding

class CodeAdapter(
    var context: Activity,
    var list: ArrayList<CountryCodeModel>,
    var callBack: (Int) -> Unit
) : RecyclerView.Adapter<CodeAdapter.codeHolder>() {

    lateinit var binding: CountryCodeItemBinding
    private var countryListFull = ArrayList<CountryCodeModel>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): codeHolder {
//        var inflater=LayoutInflater.from(context).inflate(R.layout.quality_item,parent,false)
        binding = CountryCodeItemBinding.inflate(context.layoutInflater,parent,false)
        return codeHolder(binding)
    }

    override fun onBindViewHolder(holder: codeHolder, position: Int) {
        var data = list[position]
        Log.e("sncksnc", "skcnks $data")
        binding.apply {
            if (data.isSelected) {
                codeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
            } else {
                codeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
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

    class codeHolder(binding: CountryCodeItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun update(newList: CountryCodeModel) {
        list.add(newList)
        Log.e("sncksnc", "newList $newList")
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        list = if (query.isEmpty()) {
            countryListFull
        } else {
            countryListFull.filter { it.name.contains(query, ignoreCase = true) } as ArrayList<CountryCodeModel>
        }
        notifyDataSetChanged()
    }

}