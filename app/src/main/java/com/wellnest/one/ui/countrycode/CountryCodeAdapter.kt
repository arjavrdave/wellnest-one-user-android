package com.wellnest.one.ui.countrycode

/**
 * Created by Hussain on 07/11/22.
 */

import android.annotation.SuppressLint
import com.wellnest.one.databinding.ItemCountryCodeBinding
import com.wellnest.one.model.CountryCode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.wellnest.one.R


class CountryCodeAdapter: RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder>,SectionIndexer{

    private var mDialCodeListener: IDialCodeListener
    private var mCodeList:MutableList<CountryCode> = ArrayList()

    private var mSectionPositions: ArrayList<Int>? = null

    constructor(listener: IDialCodeListener, list: List<CountryCode>){
        this.mDialCodeListener = listener
        mCodeList = list as MutableList<CountryCode>
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryCodeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemCountryCodeBinding = DataBindingUtil.inflate<ItemCountryCodeBinding>(
            inflater,
            R.layout.item_country_code,
            parent,
            false
        )
        return CountryCodeViewHolder(itemCountryCodeBinding)
    }

    override fun onBindViewHolder(holder: CountryCodeViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.bind(mCodeList[position])

        holder.binding.root.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mDialCodeListener.onCountryCodeClick(mCodeList[position])
            }
        })
    }

    override fun getItemCount(): Int = mCodeList.size

    fun updateList(list:List<CountryCode>){
        mCodeList = list as MutableList
        notifyDataSetChanged()
    }

    class CountryCodeViewHolder(val binding: ItemCountryCodeBinding):RecyclerView.ViewHolder(binding.root) {

        fun bind(countryCode: CountryCode){
            binding.countryCode = countryCode
            binding.executePendingBindings()
        }
    }

    override fun getSections(): Array<out Any>? {
        val sections: MutableList<String> = ArrayList(26)
        mSectionPositions = ArrayList(26)

        for (i in mCodeList.indices){
            val section = mCodeList[i].name.toCharArray()[0].toUpperCase().toString()
            if(!sections.contains(section)){
                sections.add(section)
                mSectionPositions!!.add(i)
            }
        }

        return toArray<String>(sections)
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return mSectionPositions!![sectionIndex]
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }

    inline fun <reified T> toArray(list: List<*>): Array<T> {
        return (list as List<T>).toTypedArray()
    }
}
