package com.wellnest.one.ui.getintouch

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityGetInTouchBinding
import com.wellnest.one.model.CountryCode
import com.wellnest.one.model.request.GetInTouchRequest
import com.wellnest.one.ui.CountryCodeActivity
import com.wellnest.one.ui.login_signup.LoginViewModel
import com.wellnest.one.utils.ProgressHelper
import com.wellnest.one.utils.isAlphaNumeric
import com.wellnest.one.utils.isValidEmail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetInTouchActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityGetInTouchBinding

    private val subjects = listOf(
        "Want to purchase ECG 12L",
        "Want to subscribe to Wellnest's newsletter",
        "Want to know more about Wellnest",
        "Just bought new 12L & want to register as a user",
        "I have been registered but there is a verification error",
        "Other"
    )

    private val COUNTRY_CODE_RESULT = 101
    private var mCountryName = "India"
    private var mCountryInit = "in"

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor =
                ContextCompat.getColor(this@GetInTouchActivity, R.color.activity_gray_bkg)// S
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_get_in_touch)

        binding.imgClose.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
        binding.tvCountryCode.setOnClickListener(this)


        binding.spinner.adapter = ArrayAdapter(this, R.layout.item_spinner, subjects)

        binding.edtName.requestFocus()


        viewModel.getInTouchSuccess.observe(this) {
            ProgressHelper.dismissDialog()
            val thanksGetInTouchIntent = Intent(this, ThanksGetInTouchActivity::class.java)
            startActivity(thanksGetInTouchIntent)
            finish()
        }

        viewModel.errorMsg.observe(this) {
            ProgressHelper.dismissDialog()
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgClose -> {
                finish()
            }

            R.id.btnSubmit -> {
                validateAndSubmitForm()
            }

            R.id.tvCountryCode -> {
                startActivityForResult(
                    Intent(this, CountryCodeActivity::class.java).apply {
                        putExtra("name", mCountryName)
                        putExtra("country_code", mCountryInit)
                    },
                    COUNTRY_CODE_RESULT
                )
            }
        }
    }

    private fun validateAndSubmitForm() {
        val name = binding.edtName.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, "Name Required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!name.isAlphaNumeric()) {
            Toast.makeText(this,"Invalid Name",Toast.LENGTH_SHORT).show()
            return
        }

        val mail = binding.edtEmailName.text.toString()
        if (mail.isNotBlank() && !mail.isValidEmail()) {
            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show()
            return
        }


        val phone = binding.edtPhone.text.toString()

        if (phone.length !in 7..12) {
            Toast.makeText(this, "Invalid Phone Number", Toast.LENGTH_SHORT).show()
            return
        }


        val getInTouch = GetInTouchRequest(
            binding.tvCountryCode.text.split("+")[1].toInt(),
            binding.edtEmailName.text.toString(),
            binding.edtName.text.toString(),
            binding.edtNote.text.toString(),
            binding.edtPhone.text.toString(),
            binding.spinner.selectedItem.toString()
        )

        ProgressHelper.showDialog(this)
        viewModel.getInTouch(getInTouch)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                COUNTRY_CODE_RESULT -> {
                    val cc = data!!.getParcelableExtra<CountryCode>("country_code") as CountryCode
                    binding.tvCountryCode.text = cc.dialCode
                    mCountryName = cc.name
                    mCountryInit = cc.countryCode
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}