package com.wellnest.one.ui

/**
 * Created by Hussain on 07/11/22.
 */

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityCountryCodeBinding
import com.wellnest.one.model.CountryCode
import com.wellnest.one.ui.countrycode.CountryCodeAdapter
import com.wellnest.one.ui.countrycode.IDialCodeListener
import com.wellnest.one.utils.Util


class CountryCodeActivity : BaseActivity(), IDialCodeListener,TextWatcher, View.OnClickListener {

    private lateinit var mBinding: ActivityCountryCodeBinding
    private lateinit var mAdapter: CountryCodeAdapter

    private var mList:List<CountryCode> = ArrayList()
    private var filteredList:MutableList<CountryCode> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_country_code)
        init()
    }

    private fun init(){

        val recyclerView = mBinding.countryRv

        val name = intent.getStringExtra("name")
        val init = intent.getStringExtra("country_code")?.toLowerCase()

        mBinding.tvSelectedCountry.text = name
        //https://countryflagsapi.com/png/$initials
        val countryFlagId = World.getFlagOf(init)
        mBinding.imageFlag.setImageResource(countryFlagId)

        mBinding.edtSearch.addTextChangedListener(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        mList = Util.getCountryCode(this)!!
        mAdapter = CountryCodeAdapter(this, mList)
        recyclerView.adapter = mAdapter

        recyclerView.setIndexTextSize(12)
        recyclerView.setIndexBarTransparentValue(0.0.toFloat())
        recyclerView.setIndexBarTextColor(R.color.black)
        recyclerView.setIndexBarStrokeWidth(0)
        recyclerView.setIndexBarStrokeVisibility(false)

        val typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        recyclerView.setTypeface(typeface)

        mBinding.imgBack.setOnClickListener(this)

    }

    override fun onCountryCodeClick(countryCode: CountryCode) {
        val intent = Intent()
        intent.putExtra("country_code", countryCode)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun filterCountries(query:CharSequence){

        filteredList.clear()

        for(country in mList){
            val name = country.name.toLowerCase()
            if(name.contains(query))
                filteredList.add(country)
        }

        mAdapter.updateList(filteredList)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(charSeq: CharSequence?, start: Int, before: Int, count: Int) {
        filterCountries(charSeq!!)
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun onClick(v: View?) {

        when(v!!.id){

            R.id.imgBack -> {
                finish()
            }
        }
    }
}