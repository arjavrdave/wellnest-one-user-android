package com.wellnest.one.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wellnest.one.BuildConfig
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityUserProfileBinding
import com.wellnest.one.dto.UserProfile
import com.wellnest.one.model.request.LogoutRequest
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.onboarding.WelcomeActivity
import com.wellnest.one.utils.*
import com.wellnest.one.utils.Constants.GRAM_FACTOR
import com.wellnest.one.utils.Constants.METER_FACTOR
import com.wellnest.one.utils.units.HeightUnit
import com.wellnest.one.utils.units.WeightUnit
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.*
import javax.inject.Inject

/**
 * Created by Hussain on 10/11/22.
 */
@AndroidEntryPoint
class UserProfileActivity : BaseActivity(), View.OnClickListener {


    private lateinit var binding: ActivityUserProfileBinding

    private val viewModel: ProfileViewModel by viewModels()
    private var selectedProfileImage: Bitmap? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)

        binding.llEditProfile.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.llMedicalHistory.setOnClickListener(this)
        binding.llRecommend.setOnClickListener(this)
        binding.llLogout.setOnClickListener(this)

        setupLayout()
        setupObservers()


    }

    override fun onResume() {
        super.onResume()
        val user = preferenceManager.getUser()
        user?.let {
            setupUI(it)
        }

        viewModel.getProfile()
    }

    private fun setupObservers() {
        viewModel.profileData.observe(this) {
            preferenceManager.saveUser(it)
            viewModel.getReadImageSasToken()
            setupUI(it)
        }

        viewModel.logoutSuccess.observe(this) {
            ProgressHelper.dismissDialog()
            preferenceManager.clear()
            val logoutIntent = Intent(this, WelcomeActivity::class.java)
            logoutIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
            finish()
        }

        viewModel.errorMsg.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            ProgressHelper.dismissDialog()
        }

        viewModel.readImageToken.observe(this) {
            val id = preferenceManager.getUser()?.profileId ?: return@observe
            viewModel.getProfileImage(id, Constants.PROFILE_IMAGES, it)
        }

        viewModel.userProfileImage.observe(this) {
            it?.let { bmp ->
                binding.imgProfile.setImageBitmap(bmp)
            }
        }
    }

    private fun setupUI(profile: UserProfile) {
        binding.tvName.text = "${profile.firstName} ${profile.lastName}"

        binding.tvPhone.text =
            "+${profile.countryCode}.${profile.phoneNumber?.substring(IntRange(0, 4))}.${
                profile.phoneNumber?.substring(
                    IntRange(
                        5,
                        profile.phoneNumber?.length!! - 1
                    )
                )
            }"

        binding.apply {
            if (profile.weightUnit == WeightUnit.KILO.toString() || profile.weightUnit == WeightUnit.Kg.toString())
                weightLayout.tvValue.text = "${profile.weight!! / 1000.0} Kg"
            else
                weightLayout.tvValue.text =
                    "${CalculatorUtil.formattedPounds(gramsToPounds(profile.weight?.toDouble() ?: 0.0))} Lbs"


            if (profile.heightUnit == HeightUnit.CMS.toString() || profile.heightUnit == HeightUnit.Cm.toString()) {
                heightLayout.tvValue.text = "${profile.height} Cms"
            } else {
                val inches = profile.height?.convertCmsToInch() ?: 0.0
                val feet = (inches / 12).toInt()
                val inch = (inches - (12 * feet)).roundValue().toInt()
                heightLayout.tvValue.text = "$feet ft $inch in"
            }

            binding.smokingLayout.tvValue.text = profile.smoking
            binding.tobaccoLayout.tvValue.text = profile.tobaccoUse
            binding.exerciseLayout.tvValue.text = profile.exerciseLevel

            binding.bmiLayout.tvValue.text = profile.bmi.toString()


            binding.tvAgeGender.text =
                "${profile.getAge()} Years | ${profile.gender?.capitalize(Locale.getDefault())}"
        }
    }


    fun gramsToPounds(weight: Double): Double {
        return weight / GRAM_FACTOR
    }

    fun cmsToFeet(cms: Double): Double {

        val feet = (cms.toFloat() / 100) * METER_FACTOR
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING

        return df.format(feet).toDouble()
    }

    private fun feetToCms(feet: String, inch: String): Double {
        val heightInCms =
            feet.toFloat() * Constants.FEET_FACTOR + inch.toFloat() * Constants.INCH_FACTOR
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING

        return df.format(heightInCms).toDouble()
    }

    private fun setupLayout() {
        binding.heightLayout.tvHeader.gravity = View.TEXT_ALIGNMENT_TEXT_START
        binding.heightLayout.tvValue.gravity = View.TEXT_ALIGNMENT_TEXT_START

        binding.smokingLayout.tvHeader.gravity = View.TEXT_ALIGNMENT_TEXT_START
        binding.smokingLayout.tvValue.gravity = View.TEXT_ALIGNMENT_TEXT_START

        binding.weightLayout.tvHeader.gravity = ViewGroup.TEXT_ALIGNMENT_CENTER
        binding.weightLayout.tvValue.gravity = View.TEXT_ALIGNMENT_CENTER

        binding.tobaccoLayout.tvHeader.gravity = ViewGroup.TEXT_ALIGNMENT_CENTER
        binding.tobaccoLayout.tvValue.gravity = View.TEXT_ALIGNMENT_CENTER

        binding.bmiLayout.tvHeader.gravity = ViewGroup.TEXT_ALIGNMENT_TEXT_END
        binding.bmiLayout.tvValue.gravity = View.TEXT_ALIGNMENT_TEXT_END

        binding.exerciseLayout.tvHeader.gravity = ViewGroup.TEXT_ALIGNMENT_TEXT_END
        binding.exerciseLayout.tvValue.gravity = View.TEXT_ALIGNMENT_TEXT_END

        binding.weightLayout.tvHeader.text = "Weight"
        binding.bmiLayout.tvHeader.text = "BMI"
        binding.exerciseLayout.tvHeader.text = "Exercise"
        binding.tobaccoLayout.tvHeader.text = "Tobacco"
        binding.smokingLayout.tvHeader.text = "Smoking"

        binding.tvVersion.text =
            "Wellnest One v${packageManager.getPackageInfo(packageName, 0).versionName}"
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> finish()
            R.id.llEditProfile -> {
                val profileIntent = Intent(this, CreateEditProfileActivity::class.java)
                profileIntent.putExtra("fromActivity", UserProfileActivity::class.java.simpleName)
                startActivity(profileIntent)
            }
            R.id.llMedicalHistory -> {
                val medicalIntent = Intent(this, MedicalHistoryActivity::class.java)
                medicalIntent.putExtra("fromActivity", UserProfileActivity::class.java.simpleName)
                startActivity(medicalIntent)
            }
            R.id.llRecommend -> {

                val recommendText =
                    "Hello, I recommend Wellnest 12L machine for quick, easy and accurate heart check-up.  Get the device here https://www.wellnest.tech/shop and download the app here https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

                val recommendIntent = Intent(Intent.ACTION_SEND)
                recommendIntent.type = "text/plain"
                recommendIntent.putExtra(Intent.EXTRA_TEXT, recommendText)

                if (recommendIntent.resolveActivity(packageManager) != null) {
                    val chooser = Intent.createChooser(recommendIntent, "Recommend a Friend")
                    startActivity(chooser)
                } else {
                    Toast.makeText(this, "No Apps Founds", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.llLogout -> {
                MaterialAlertDialogBuilder(this).setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Ok") { dialog, which ->
                        ProgressHelper.showDialog(this)
                        val fcmToken = preferenceManager.getFcmToken()
                        viewModel.logout(LogoutRequest(fcmToken ?: ""))
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    override fun pickedImage(bitmap: Bitmap) {
        binding.imgProfile.setImageBitmap(bitmap)
        selectedProfileImage = bitmap
    }


}