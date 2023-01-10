package com.wellnest.one.ui.login_signup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityLoginBinding
import com.wellnest.one.model.CountryCode
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.CountryCodeActivity
import com.wellnest.one.ui.termofservice.TermsOfServiceActivity
import com.wellnest.one.ui.verifyotp.VerifyOtpActivity
import com.wellnest.one.utils.Constants
import com.wellnest.one.utils.ProgressHelper
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Hussain on 07/11/22.
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity() {


    private lateinit var binding : ActivityLoginBinding

    private val viewModel : LoginViewModel by viewModels()

    private var countryCode = 91

    private var mCountryName = "India"
    private var mCountryInit = "in"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.imgClose.setOnClickListener {
            finish()
        }

        binding.imgFlag.setOnClickListener {
            startActivityForResult(
                Intent(this, CountryCodeActivity::class.java).apply {
                    putExtra("name", mCountryName)
                    putExtra("country_code", mCountryInit)
                },
                Constants.COUNTRY_CODE_RESULT
            )
        }

        binding.tvCountryCode.setOnClickListener {
            startActivityForResult(
                Intent(this, CountryCodeActivity::class.java).apply {
                    putExtra("name", mCountryName)
                    putExtra("country_code", mCountryInit)
                },
                Constants.COUNTRY_CODE_RESULT
            )
        }

        binding.tvAgreement.setOnClickListener {
            startActivity(Intent(this, TermsOfServiceActivity::class.java))
        }

        binding.edPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val number = s.toString()
                binding.btnNext.isEnabled = number.length in 7..13
            }

        })

        binding.btnNext.setOnClickListener {
            ProgressHelper.showDialog(this)
            viewModel.login(countryCode,binding.edPhoneNumber.text.toString().trim())
            binding.tvError.visibility = View.GONE
        }

        viewModel.loginSuccess.observe(this) {
            ProgressHelper.dismissDialog()
            if (it) {
                val verifyIntent = Intent(this,VerifyOtpActivity::class.java)
                verifyIntent.putExtra("countryCode",countryCode)
                verifyIntent.putExtra("phoneNumber",binding.edPhoneNumber.text.toString())
                startActivity(verifyIntent)
            }
        }

        viewModel.errorMsg.observe(this) {
            ProgressHelper.dismissDialog()
            if (it.isNotBlank()) {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = it
            }
        }

        World.init(applicationContext)
        val countryFlagId = World.getFlagOf(mCountryInit)
        binding.imgFlag.setImageResource(countryFlagId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.COUNTRY_CODE_RESULT && resultCode == Activity.RESULT_OK) {
            val cc = data?.getParcelableExtra<CountryCode>("country_code") as CountryCode
            binding.tvCountryCode.text = cc.dialCode
            countryCode = cc.dialCode.split("+")[1].toInt()
            mCountryName = cc.name
            mCountryInit = cc.countryCode
            val countryFlagId = World.getFlagOf(mCountryInit)
            binding.imgFlag.setImageResource(countryFlagId)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}