package com.wellnest.one.ui.profile

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.flyco.tablayout.listener.CustomTabEntity
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityEditBioBinding
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.utils.TabEntity
import com.wellnest.one.utils.units.HeightUnit
import com.wellnest.one.utils.units.WeightUnit
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

/**
 * Created by Hussain on 10/11/22.
 */
@AndroidEntryPoint
class EditBioActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding : ActivityEditBioBinding

    private val smoking = listOf("Never","Low","Med","High")
    private val tobacco = listOf("Never","Low","Med","High")
    private val exercise = listOf("Low","Med","High")

    private val profileViewModel : ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_bio)

        binding.imgBack.setOnClickListener(this)
        binding.tvSave.setOnClickListener(this)

        val smokingArray = ArrayList<CustomTabEntity>()

        for (elem in smoking) {
            smokingArray.add(TabEntity(elem,0,0))
        }

        binding.smokingTab.setTabData(smokingArray)


        val tobaccoArray = ArrayList<CustomTabEntity>()

        for (elem in tobacco) {
            tobaccoArray.add(TabEntity(elem,0,0))
        }

        binding.tobaccoTab.setTabData(tobaccoArray)

        val exerciseArray = ArrayList<CustomTabEntity>()

        for (elem in exercise) {
            exerciseArray.add(TabEntity(elem,0,0))
        }

        binding.exerciseTab.setTabData(tobaccoArray)

        binding.spnrKilos.data = getWeightKiloData()
        binding.spnrGrams.data = getWeightGramsData()
        binding.spnrPounds.data = getWeightPoundsData()

        binding.spnrFeet.data = getHeightFeetData()
        binding.spnrInch.data = getHeightInchData()
        binding.spnrCms.data = getHeightCmData()

        setupObservers()

        profileViewModel.getProfile()

    }

    private fun setupObservers() {
        profileViewModel.profileData.observe(this) { data ->
            when(data.smoking) {
                "Never" -> binding.smokingTab.currentTab = 0
                "Low" ->  binding.smokingTab.currentTab = 1
                "Med" ->  binding.smokingTab.currentTab = 2
                "High" ->  binding.smokingTab.currentTab = 3
            }

            when(data.tobaccoUse) {
                "Never" -> binding.tobaccoTab.currentTab = 0
                "Low" ->  binding.tobaccoTab.currentTab = 1
                "Med" ->  binding.tobaccoTab.currentTab = 2
                "High" ->  binding.tobaccoTab.currentTab = 3
            }

            when(data.exerciseLevel) {
                "Never" -> binding.exerciseTab.currentTab = 0
                "Low" ->  binding.exerciseTab.currentTab = 1
                "Med" ->  binding.exerciseTab.currentTab = 2
                "High" ->  binding.exerciseTab.currentTab = 3
            }

            if (data.weightUnit == WeightUnit.KILO.toString() || data.weightUnit == WeightUnit.Kg.toString()) {
                binding.tgWeight.setCheckedPosition(0)
            } else {
                binding.tgWeight.setCheckedPosition(1)
            }

            if (data.heightUnit == HeightUnit.CMS.toString() || data.heightUnit == HeightUnit.Cm.toString()) {
                binding.tgHeight.setCheckedPosition(0)
            } else {
                binding.tgHeight.setCheckedPosition(1)
            }

        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> finish()
            R.id.tvSave -> saveUserBio()
        }
    }

    private fun saveUserBio() {

    }

    private fun getWeightKiloData(): List<Int> {
        val list = ArrayList<Int>()
        for (i in 0..1000) {
            list.add(i)
        }
        return list
    }

    private fun getWeightGramsData(): List<String> {
        val list = ArrayList<String>()
        var i = 0.0
        val dF = DecimalFormat(".000")
        while (i <= 0.91) {
            list.add(dF.format(i))
            i += 0.1
        }
        return list
    }

    private fun getWeightPoundsData(): List<String> {
        val list = ArrayList<String>()
        var i = 0
        while (i <= 1000) {
            list.add("$i lbs")
            i += 1
        }
        return list
    }

    private fun getHeightCmData(): List<String> {
        val list = ArrayList<String>()
        var i = 0
        while (i <= 500) {
            list.add("$i")
            i += 1
        }
        return list
    }

    private fun getHeightFeetData(): List<String> {
        val list = ArrayList<String>()
        var i = 0
        while (i <= 12) {
            list.add("$i Feet")
            i += 1
        }
        return list
    }

    private fun getHeightInchData(): List<String> {
        val list = ArrayList<String>()
        var i = 0
        while (i <= 11) {
            list.add("$i Inches")
            i += 1
        }
        return list
    }


}