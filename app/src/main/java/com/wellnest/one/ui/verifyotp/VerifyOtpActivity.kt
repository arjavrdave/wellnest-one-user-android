package com.wellnest.one.ui.verifyotp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityVerifyOtpBinding
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.home.HomeActivity
import com.wellnest.one.ui.login_signup.LoginViewModel
import com.wellnest.one.ui.profile.CreateEditProfileActivity
import com.wellnest.one.utils.KeyboardHelper
import com.wellnest.one.utils.ProgressHelper
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Hussain on 07/11/22.
 */
@AndroidEntryPoint
class VerifyOtpActivity : BaseActivity(), TextWatcher {

    private var code: String = ""
    private lateinit var binding : ActivityVerifyOtpBinding
    private lateinit var mEditTextFields:List<EditText>

    private val viewModel : LoginViewModel by viewModels()
    private var countryCode : Int? = null
    private var phoneNumber : String? = null
    private var timer: CountDownTimer? = null
    protected val FORMAT = "%02d:%02d"
    private var fcmToken : String? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_verify_otp)
        binding.ed1.addTextChangedListener(this)
        binding.ed2.addTextChangedListener(this)
        binding.ed3.addTextChangedListener(this)
        binding.ed4.addTextChangedListener(this)

        countryCode = intent.getIntExtra("countryCode",0)
        phoneNumber = intent.getStringExtra("phoneNumber")
        fcmToken = sharedPreferenceManager.getFcmToken()

        binding.tvOtpSent.text = getString(R.string.otp_sent_msg) + " +${countryCode} ${phoneNumber}"

        binding.fbNext.floatingButton(false,R.color.gray_btn)

        binding.imgBack.setOnClickListener { finish() }

        binding.fbNext.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            ProgressHelper.showDialog(this)
            viewModel.verifyOtp(
                countryCode ?: 0,phoneNumber,code,fcmToken ?: ""
            )
        }

        mEditTextFields = listOf(binding.ed1, binding.ed2, binding.ed3, binding.ed4)

        timer = object : CountDownTimer(1 * 30 * 1000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                binding.llResend.visibility = View.GONE
                binding.tvResend.visibility = View.VISIBLE
                binding.tvResend.setText(
                    "Resend in " + String.format(
                        FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(
                            millisUntilFinished
                        ) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                        ),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        )
                    ) + " Sec"
                )
            }

            override fun onFinish() {
                binding.llResend.setOnClickListener {
                    timer?.start()
                    viewModel.resendOtp(countryCode ?: 0,phoneNumber ?: "")
                }
                binding.tvResend.visibility = View.GONE
                binding.llResend.visibility = View.VISIBLE
            }
        }

        timer?.start()

        viewModel.otpSuccess.observe(this) {
            ProgressHelper.dismissDialog()
            it?.let { token ->
                preferenceManager.saveToken(token)
                val redirectIntent : Intent = if (token.isNewUser == true) {
                    // create new profile
                    Intent(this,CreateEditProfileActivity::class.java)
                } else {
                    // redirect to home screen
                    Intent(this,HomeActivity::class.java)
                }
                redirectIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(redirectIntent)
                finish()
            }
        }

        viewModel.errorMsg.observe(this) {
            ProgressHelper.dismissDialog()
            it?.let {
                mEditTextFields.forEach { edt ->
                    edt.setBackgroundResource(R.drawable.edt_otp_bg_err)
                }
                binding.tvErrorMsg.visibility = View.VISIBLE
                binding.tvErrorMsg.text = it
            }
        }

        viewModel.resendOtpSuccess.observe(this) {
            ProgressHelper.dismissDialog()
            if (it) {
                Toast.makeText(this, "Otp Sent Successfully", Toast.LENGTH_SHORT).show()
                timer?.start()
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    private fun FloatingActionButton.floatingButton(hasClick: Boolean, color: Int) {
        isClickable = hasClick
        isEnabled = hasClick
        backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                this@VerifyOtpActivity,
                color
            )
        )
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val text = s.toString()



        if (text.isNotEmpty() && validateEdtTxtFields(mEditTextFields)) {
            KeyboardHelper.hideKeyboard(this)
            val edt1: String = binding.ed1.text.toString()
            val edt2: String = binding.ed2.text.toString()
            val edt3: String = binding.ed3.text.toString()
            val edt4: String = binding.ed4.text.toString()
            code = edt1 + edt2 + edt3 + edt4


            KeyboardHelper.hideKeyboard(this)
            binding.fbNext.floatingButton(true,R.color.green_btn)
        } else {
            binding.fbNext.floatingButton(false,R.color.gray_btn)
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun afterTextChanged(s: Editable?) {

        if (s === binding.ed1.editableText) {
            if(binding.ed1.text.toString().isNotEmpty()) {
                binding.ed1.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.edt_otp_bg_active
                )
            }
            else binding.ed1.background = ContextCompat.getDrawable(this, R.drawable.edt_otp_bg_empty)
        }


        if (s === binding.ed2.editableText) {
            if(binding.ed2.text.toString().isNotEmpty()) {
                binding.ed2.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.edt_otp_bg_active
                )
            }
            else binding.ed2.background = ContextCompat.getDrawable(this, R.drawable.edt_otp_bg_empty)
        }

        if (s === binding.ed3.editableText) {
            if(binding.ed3.text.toString().isNotEmpty()) {
                binding.ed3.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.edt_otp_bg_active
                )
            }
            else binding.ed3.background = ContextCompat.getDrawable(this, R.drawable.edt_otp_bg_empty)
        }

        if (s === binding.ed4.editableText) {
            if(binding.ed4.text.toString().isNotEmpty()) {
                binding.ed4.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.edt_otp_bg_active
                )
            }
            else binding.ed4.background = ContextCompat.getDrawable(this, R.drawable.edt_otp_bg_empty)
        }

        if (s === binding.ed1.editableText) {
            if (binding.ed1.length() == 1) {
                binding.ed2.requestFocus()
            }
        }
        if (s === binding.ed2.editableText) {
            if (binding.ed2.length() == 1) {
                binding.ed3.requestFocus()
            } else {
                binding.ed1.requestFocus()
            }
        }
        if (s === binding.ed3.editableText) {
            if (binding.ed3.length() == 1) {
                binding.ed4.requestFocus()
            } else {
                binding.ed2.requestFocus()
            }
        }
        if (s === binding.ed4.editableText) {
            if (binding.ed4.length() == 0) {
                binding.ed3.requestFocus()
            }
        }
    }

    private fun validateEdtTxtFields(editTextList: List<EditText>): Boolean {
        for (field in editTextList) {
            if (field.text.toString().isEmpty()) {
                return false
            }
        }
        return true
    }
}