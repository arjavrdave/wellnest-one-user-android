package com.wellnest.one.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.aigestudio.wheelpicker.WheelPicker
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityCreateProfileBinding
import com.wellnest.one.dto.UserProfile
import com.wellnest.one.model.request.MedicalHstory
import com.wellnest.one.model.request.UserProfileRequest
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.utils.*
import com.wellnest.one.utils.Constants.GRAM_FACTOR
import com.wellnest.one.utils.units.HeightUnit
import com.wellnest.one.utils.units.WeightUnit
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/**
 * Created by Hussain on 08/11/22.
 */
@AndroidEntryPoint
class CreateEditProfileActivity : BaseActivity(), View.OnClickListener,
    WheelPicker.OnItemSelectedListener, TextWatcher {


    private lateinit var binding: ActivityCreateProfileBinding
    private val mHandler = Handler(Looper.getMainLooper())
    private var mGender = "Male"
    private var mHeightUnit = HeightUnit.FEET
    private var mWeightUnit = WeightUnit.KILO
    private lateinit var profileBitmap: Bitmap

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val profileViewModel: ProfileViewModel by viewModels()

    private val smoking = listOf("Never", "Low", "Med", "High")
    private val tobacco = listOf("Never", "Low", "Med", "High")
    private val exercise = listOf("Never", "Low", "Med", "High")

    private var profileImage = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_profile)

        binding.imgBack.setOnClickListener(this)
        binding.tvDay.setOnClickListener(this)
        binding.tvMonth.setOnClickListener(this)
        binding.tvYear.setOnClickListener(this)
        binding.tvKilo.setOnClickListener(this)
        binding.tvGrams.setOnClickListener(this)
        binding.tvFeet.setOnClickListener(this)
        binding.tvInch.setOnClickListener(this)
        binding.tvBdHint.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.imgProfile.setOnClickListener(this)
        binding.imgEditPic.setOnClickListener(this)

        binding.edtFirstName.addTextChangedListener(this)
        binding.edtLastName.addTextChangedListener(this)


        setupObservers()
        setDefaults()
        setToggleListeners()
        setSpinnerListeners()

        val user = preferenceManager.getUser()
        val from = intent.getStringExtra("fromActivity")

        if (from == UserProfileActivity::class.java.simpleName) {
            binding.tvHeader.text = "Edit Profile"
            binding.btnNext.text = "Save"
            profileViewModel.getReadImageSasToken()
            profileViewModel.getMedicalHistory()
            profileImage = if (user?.profileId?.isBlank() == false) {
                user.profileId
            } else {
                UUID.randomUUID().toString()
            }
            setData(user)
        } else {
            profileImage = UUID.randomUUID().toString()
            binding.tvHeader.text = "Create Profile"
        }

        profileViewModel.getProfile()


    }

    private fun setupObservers() {
        profileViewModel.medicalHistoryData.observe(this) {
            preferenceManager.saveMedicalHistory(it)
        }

        profileViewModel.profileData.observe(this) { profile ->
            preferenceManager.saveUser(profile)
            setData(profile)
            checkValidation()
        }

        profileViewModel.uploadImageToken.observe(this) { sasToken ->
            profileViewModel.uploadImage(profileBitmap, sasToken, profileImage)
        }

        profileViewModel.profileImgUploadSuccess.observe(this) {
            if (it) {
            }
        }

        profileViewModel.errorMsg.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }

        profileViewModel.readImageToken.observe(this) {
            val id = preferenceManager.getUser()?.profileId ?: return@observe
            profileViewModel.getProfileImage(id, Constants.PROFILE_IMAGES, it)
        }

        profileViewModel.userProfileImage.observe(this) {
//            binding.imgEditPic.visibility = if (it == null) View.VISIBLE else View.INVISIBLE
            it?.let { bmp ->
                binding.imgProfile.setImageBitmap(bmp)
            }
        }

        profileViewModel.profileSuccess.observe(this) {
            if (it) {
                Toast.makeText(this, "Profile Successfully Updated", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setData(profile: UserProfile?) {
        profile?.let { user ->

            binding.tvPhone.text = "+${user.countryCode} ${user.phoneNumber}"

            binding.edtFirstName.setText(user.firstName)
            binding.edtLastName.setText(user.lastName)

            binding.edtEmail.setText(user.email)
            mGender = user.gender ?: "FEMALE"
            when (user.gender) {
                "Male" -> binding.tgSex.setCheckedPosition(0)
                "Female" -> binding.tgSex.setCheckedPosition(1)
                "Other" -> binding.tgSex.setCheckedPosition(2)
            }

            for (i in tobacco.indices) {
                if (user.tobaccoUse == tobacco[i])
                    binding.tobaccoTab.setCheckedPosition(i)
            }

            for (i in smoking.indices) {
                if (user.smoking == smoking[i])
                    binding.smokingTab.setCheckedPosition(i)
            }

            for (i in exercise.indices) {
                if (user.exerciseLevel == exercise[i])
                    binding.exerciseTab.setCheckedPosition(i)
            }

            if (profile.heightUnit == "FEET") {
                profile.height?.let {
                    val feet =
                        CalculatorUtil.convertToFeetInches(profile.height.toString()).split(",")

                    binding.tvFeet.text = feet[0] + "'"
                    binding.tvInch.text = feet[1] + "\""

                    binding.tgHeight.setCheckedPosition(0)
                }
            } else {
                mHeightUnit = HeightUnit.CMS
                val heightData = profile.height?.toString()?.split(".")
                binding.tvFeet.text = heightData?.get(0) ?: "0"
                binding.tvInch.visibility = View.GONE


                mHandler.postDelayed({
                    binding.tgHeight.setCheckedPosition(1)
                    binding.spnrCms.visibility = View.VISIBLE
                    binding.spnrInch.visibility = View.GONE
                    binding.spnrFeet.visibility = View.GONE
                }, 500)

            }

            if (profile.weightUnit == "KILO") {
                binding.tgWeight.setCheckedPosition(0)
                binding.tvGrams.visibility = View.VISIBLE

                profile.weight?.let {
                    val kilos = CalculatorUtil.formattedGrams(profile.weight.toDouble()).toString()
                        .split(".")

                    binding.tvKilo.text = kilos[0]
                    binding.tvGrams.text = ".${kilos[1]}"
                }
            } else {
                mWeightUnit = WeightUnit.PND
                binding.tvGrams.visibility = View.GONE

                profile.weight?.let {
                    val pounds =
                        CalculatorUtil.gramsToPounds(profile.weight.toDouble()).roundToInt()
                            .toString()
                    binding.tvKilo.text = pounds
                }

                mHandler.postDelayed({
                    binding.tgWeight.setCheckedPosition(1)
                    binding.spnrPounds.visibility = View.VISIBLE

                    binding.spnrKilos.visibility = View.GONE
                    binding.spnrGrams.visibility = View.GONE
                }, 500)
            }

            val date = profile.dob?.let { Util.isoToDobString(it).split(" ") }
            binding.tvYear.hint = ""

            if (date?.get(0).isNullOrBlank() || date?.get(1).isNullOrBlank() || date?.get(2)
                    .isNullOrBlank()
            ) {
                binding.tvBdHint.visibility = View.VISIBLE
            } else {
                binding.tvBdHint.visibility = View.GONE
            }

            binding.tvDay.text = date?.get(0) ?: ""
            binding.tvMonth.text = date?.get(1) ?: ""
            binding.tvYear.text = date?.get(2) ?: ""
        }
    }

    private fun setToggleListeners() {

        binding.tobaccoTab.onChangeListener = object : ToggleSwitch.OnChangeListener {
            override fun onToggleSwitchChanged(position: Int) {

            }

        }

        binding.smokingTab.onChangeListener = object : ToggleSwitch.OnChangeListener {
            override fun onToggleSwitchChanged(position: Int) {

            }

        }

        binding.exerciseTab.onChangeListener = object : ToggleSwitch.OnChangeListener {
            override fun onToggleSwitchChanged(position: Int) {

            }

        }


        binding.tgSex.onChangeListener = object : ToggleSwitch.OnChangeListener {
            override fun onToggleSwitchChanged(position: Int) {
                mGender = when (position) {
                    0 -> {
                        "Male"
                    }
                    1 -> {
                        "Female"
                    }
                    else -> {
                        "Other"
                    }
                }
                checkValidation()
            }
        }

        binding.tgHeight.onChangeListener = object : ToggleSwitch.OnChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onToggleSwitchChanged(position: Int) {
                if (position == 1) {// convert feet inch to cms
                    mHeightUnit = HeightUnit.CMS
                    binding.spnrFeet.visibility = View.GONE
                    binding.spnrInch.visibility = View.GONE

                    binding.spnrCms.visibility = View.VISIBLE
                    binding.tvInch.visibility = View.GONE

                    //convert values that are inserted
                    val feet = binding.tvFeet.text.toString().replace("'", "").toDouble()
                    val inch = binding.tvInch.text.toString().replace("\"", "").toDouble()

                    val cms = (feet.convertFeetToCms() + inch.convertInchToCms())
                    val cmsString = cms.roundToInt()

                    binding.tvFeet.text = cmsString.toString()
                    binding.tvInch.text = ""

                    mHandler.postDelayed({
                        binding.spnrCms.setSelectedItemPosition(
                            cmsString,
                            true
                        )
                    }, 500)

                } else { // convert cms to feet
                    mHeightUnit = HeightUnit.FEET
                    binding.spnrFeet.visibility = View.VISIBLE
                    binding.spnrInch.visibility = View.VISIBLE
                    binding.spnrCms.visibility = View.GONE
                    binding.tvInch.visibility = View.VISIBLE

                    //convert
                    val cms = binding.tvFeet.text.toString().toDouble()
                    val inches = cms.convertCmsToInch()
                    val feet = (inches / 12).toInt()
                    val inch = (inches % 12).toInt()/*.roundValue().toInt()*/
                    /*val inch = (inches - (12 * feet))
                    val roundoff = (inch * 100.0).roundToInt() / 100.0
*/
                    binding.tvFeet.text = "$feet'"
                    binding.tvInch.text = "$inch\""

                    mHandler.postDelayed({
                        binding.spnrFeet.setSelectedItemPosition(
                            feet,
                            true
                        )
                    }, 500)

                    mHandler.postDelayed({
                        binding.spnrInch.setSelectedItemPosition(
                            inch,
                            true
                        )
                    }, 300)
                }
            }
        }

        binding.tgWeight.onChangeListener = object : ToggleSwitch.OnChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onToggleSwitchChanged(position: Int) {
                if (position == 1) {  // kgs to PND converter
                    mWeightUnit = WeightUnit.PND
                    binding.spnrKilos.visibility = View.GONE
                    binding.spnrGrams.visibility = View.GONE
                    binding.spnrPounds.visibility = View.VISIBLE
                    binding.tvGrams.visibility = View.GONE
                    //  mBinding.spnrPoundsPoints.visibility = View.VISIBLE

                    val totalKilos =
                        (binding.tvKilo.text.toString() + binding.tvGrams.text.toString()).toDouble()
                    val pounds = totalKilos.convertKilosToPounds().roundValue().toInt().toString()

                    binding.tvKilo.text = pounds
                    binding.tvGrams.text = ""

                    mHandler.postDelayed({
                        binding.spnrPounds.setSelectedItemPosition(
                            pounds.toInt(),
                            true
                        )
                    }, 500)

                } else { // PND to Kilo
                    mWeightUnit = WeightUnit.KILO
                    binding.spnrKilos.visibility = View.VISIBLE
                    binding.spnrGrams.visibility = View.VISIBLE
                    binding.spnrPounds.visibility = View.GONE
                    binding.tvGrams.visibility = View.VISIBLE

                    val kilos = binding.tvKilo.text.toString().toDouble()
                        .convertPoundsToKilos().formatValue().toString().split(".")

                    binding.tvKilo.text = kilos[0]
                    binding.tvGrams.text = ".${kilos[1]}"

                    mHandler.postDelayed({
                        binding.spnrKilos.setSelectedItemPosition(
                            kilos[0].toInt(),
                            true
                        )
                    }, 500)

                    mHandler.postDelayed({
                        binding.spnrGrams.setSelectedItemPosition(
                            kilos[1].toInt(),
                            true
                        )
                    }, 300)
                    checkValidation()
                }
            }
        }
    }

    private fun setDefaults() {


        binding.smokingTab.setEntries(smoking)
        binding.smokingTab.setCheckedPosition(0)

        binding.tobaccoTab.setEntries(tobacco)
        binding.tobaccoTab.setCheckedPosition(0)

        binding.exerciseTab.setEntries(exercise)
        binding.exerciseTab.setCheckedPosition(0)

        //check female by default
        binding.tgSex.setCheckedPosition(0)

        //check ft by default
        binding.tgHeight.setCheckedPosition(0)

        //check kgs by default
        binding.tgWeight.setCheckedPosition(0)

        //Setting Spinner Data
        binding.spnrDay.data = getDaysInAMonth()
        binding.spnrMonth.data = getMonthNames(this)
        binding.spnrYear.data = getYears()

        binding.spnrKilos.data = getWeightKiloData()
        binding.spnrGrams.data = getWeightGramsData()
        binding.spnrPounds.data = getWeightPoundsData()

        binding.spnrFeet.data = getHeightFeetData()
        binding.spnrInch.data = getHeightInchData()
        binding.spnrCms.data = getHeightCmData()
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

    private fun getYears(): List<Int> {
        val calInstance = Calendar.getInstance()
        val currYear: Int = calInstance.get(Calendar.YEAR)
        val list = ArrayList<Int>()
        var i = 1900
        while (i <= currYear) {
            list.add(i)
            i++
        }
        return list
    }

    private fun getMonthNames(context: Context) =
        context.resources.getStringArray(R.array.months).toList()

    private fun getDaysInAMonth(): List<Int> {
        val list = ArrayList<Int>()
        var i = 1
        while (i <= 31) {
            list.add(i)
            i++
        }
        return list
    }

    override fun onClick(v: View?) {
        when (v!!.id) {

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.tvDay, R.id.tvMonth, R.id.tvYear, R.id.tvBdHint -> {
                if (binding.datePicker.visibility != View.VISIBLE) {
                    binding.datePicker.visibility = View.VISIBLE
                    binding.tvYear.hint = ""


                    val numb = binding.tvDay.text.toString()
                    if (numb.isNotBlank()) {
                        mHandler.postDelayed({
                            binding.spnrDay.setSelectedItemPosition(
                                numb.toInt() - 1,
                                true
                            )
                        }, 400)
                    }

                    val month = binding.tvMonth.text.toString()
                    val listOfMonths = getMonthNames(this)
                    val pos = listOfMonths.indexOf(month)
                    val index = if (pos == -1) 0 else pos
                    mHandler.postDelayed({
                        binding.spnrMonth.setSelectedItemPosition(
                            index,
                            true
                        )
                    }, 500)

                    val year = binding.tvYear.text.toString()
                    if (year.isNotBlank()) {
                        val list = getYears()
                        val posY = list.indexOf(year.toInt())
                        mHandler.postDelayed({
                            binding.spnrYear.setSelectedItemPosition(posY, true)
                        }, 800)
                    }


                } else {
                    binding.datePicker.visibility = View.GONE
                }

            }

            R.id.tvKilo, R.id.tvGrams -> {

                if (binding.weightPicker.visibility != View.VISIBLE) {
                    binding.weightPicker.visibility = View.VISIBLE

                    mHandler.postDelayed({
                        binding.scrollView.fullScroll(View.FOCUS_DOWN)
                    }, 500)




                    if (mWeightUnit == WeightUnit.KILO) {

                        val value = binding.tvKilo.text.toString().toInt()

                        mHandler.postDelayed({
                            binding.spnrKilos.setSelectedItemPosition(
                                value,
                                true
                            )
                        }, 500)

                        val valueG = binding.tvGrams.text.toString().split(".")[1].toInt()

                        mHandler.postDelayed({
                            binding.spnrGrams.setSelectedItemPosition(
                                valueG,
                                true
                            )
                        }, 300)
                    } else {

                        val value = binding.tvKilo.text.toString().toInt()

                        mHandler.postDelayed({
                            binding.spnrPounds.setSelectedItemPosition(
                                value,
                                true
                            )
                        }, 500)
                    }

                } else {
                    binding.weightPicker.visibility = View.GONE
                }

            }

            R.id.tvFeet, R.id.tvInch -> {


                if (binding.heightPicker.visibility != View.VISIBLE) {
                    binding.heightPicker.visibility = View.VISIBLE

                    mHandler.postDelayed({
                        binding.scrollView.fullScroll(View.FOCUS_DOWN)

                    }, 500)


                    if (mHeightUnit == HeightUnit.CMS) {

                        val cmValue = binding.tvFeet.text.toString().toInt()

                        mHandler.postDelayed({
                            binding.spnrCms.setSelectedItemPosition(
                                cmValue,
                                true
                            )
                        }, 800)

                    } else {

                        val feetValue = binding.tvFeet.text.toString().split("'")[0].toInt()
                        val getInchValue = binding.tvInch.text.toString()/*.split("\"")[0].toInt()*/
                        val inchValue = getInchValue.replace("\"", "")

                        mHandler.postDelayed({
                            binding.spnrFeet.setSelectedItemPosition(
                                feetValue,
                                true
                            )
                        }, 500)

                        mHandler.postDelayed({
                            binding.spnrInch.setSelectedItemPosition(
                                inchValue.toInt(),
                                true
                            )
                        }, 200)
                    }


                } else {
                    binding.heightPicker.visibility = View.GONE
                }

            }

            R.id.btnNext -> {
                saveUserDetails()
            }

            R.id.imgProfile, R.id.imgEditPic -> {
                selectProfileImage()
            }

        }

    }

    private fun saveUserDetails() {

        if (binding.edtFirstName.text.isNullOrBlank()) {
            Toast.makeText(this, "First Name Required", Toast.LENGTH_SHORT).show()
            return
        }

        val firstName = binding.edtFirstName.text.toString().trim()

        if (!firstName.isAlphaNumeric(true)) {
            Toast.makeText(this, "special characters not allowed.", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.edtLastName.text.isNullOrBlank()) {
            Toast.makeText(this, "Last Name Required", Toast.LENGTH_SHORT).show()
            return
        }

        val lastName = binding.edtLastName.text.toString().trim()

        if (!lastName.isAlphaNumeric(true)) {
            Toast.makeText(this, "special characters not allowed.", Toast.LENGTH_SHORT).show()
            return
        }

        val day = binding.tvDay.text.toString()
        val month = binding.tvMonth.text.toString()
        val year = binding.tvYear.text.toString()

        if (day.isBlank() || month.isBlank() || year.isBlank()) {
            Toast.makeText(this, "Date Of Birth Required", Toast.LENGTH_SHORT).show()
            return
        }

        val dob = dobToIsoString(day + month + year)

        val email = binding.edtEmail.text.toString().trim()

        if (email.isNotBlank() && !email.isValidEmail()) {
            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show()
            return
        }

        val height = if (mHeightUnit == HeightUnit.FEET) {
            val feet = binding.tvFeet.text.trim().toString().replace("'", "")
            val inch = binding.tvInch.text.trim().toString().replace("\"", "")
            feetToCms(feet, inch)
        } else {
            val centimeters = binding.tvFeet.text.trim().toString()
            centimeters.toDouble()
        }

        if (height == 0.0) {
            Toast.makeText(this, "Height Required", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = if (mWeightUnit == WeightUnit.KILO) {
            val kilos = binding.tvKilo.text.trim().toString()
            val grams = binding.tvGrams.text.trim().toString()

            kilosToGrams(kilos, grams.toCharArray()[1].toString() + "00")

        } else {
            val pounds = binding.tvKilo.text.trim().toString()
            poundsToGrams(pounds)
        }

        if (weight == 0.0) {
            Toast.makeText(this, "Weight Required", Toast.LENGTH_SHORT).show()
            return
        }

        val smoking = when (binding.smokingTab.checkedPosition) {
            0 -> "Never"
            1 -> "Low"
            2 -> "Med"
            3 -> "High"
            else -> "Never"
        }

        val tobacco = when (binding.tobaccoTab.checkedPosition) {
            0 -> "Never"
            1 -> "Low"
            2 -> "Med"
            3 -> "High"
            else -> "Never"
        }

        val exercise = when (binding.exerciseTab.checkedPosition) {
            0 -> "Never"
            1 -> "Low"
            2 -> "Med"
            3 -> "High"
            else -> "Never"
        }

        val from = intent.getStringExtra("fromActivity")

        val userProfile = preferenceManager.getUser()

        if (from == UserProfileActivity::class.java.simpleName) {
            val medicalHistory = preferenceManager.getMedicalHistory()
            val updated = UserProfileRequest(
                userProfile?.countryCode,
                dob,
                email,
                firstName,
                mGender,
                height,
                mHeightUnit.toString(),
                lastName,
                MedicalHstory(
                    medicalHistory?.bloodPressure,
                    medicalHistory?.diabetes,
                    medicalHistory?.healthComment,
                    medicalHistory?.heartProblem,
                    medicalHistory?.hypothyroidism,
                    medicalHistory?.kidneyProblem,
                    medicalHistory?.lipidsIssue,
                    medicalHistory?.siblingHeartProblem,
                    medicalHistory?.stroke
                ),
                userProfile?.phoneNumber,
                weight.toInt(),
                mWeightUnit.toString(),
                smoking,
                tobacco,
                exercise,
                profileImage
            )
            profileViewModel.addProfile(updated)
        } else {
            val user = UserProfileRequest(
                userProfile?.countryCode,
                dob,
                email,
                firstName,
                mGender,
                height,
                mHeightUnit.toString(),
                lastName,
                null,
                userProfile?.phoneNumber,
                weight.toInt(),
                mWeightUnit.toString(),
                smoking,
                tobacco,
                exercise,
                profileImage
            )
            val medicalIntent = Intent(this, MedicalHistoryActivity::class.java)
            medicalIntent.putExtra("profile", user)
            startActivity(medicalIntent)
        }


    }

    private fun poundsToGrams(pounds: String): Double {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(pounds.toDouble() * GRAM_FACTOR).toDouble()
    }

    private fun kilosToGrams(kilo: String, gram: String): Double {

        val weight = kilo.toFloat() * 1000 + gram.toFloat()

        return weight.toDouble()
    }

    @SuppressLint("SimpleDateFormat")
    private fun dobToIsoString(dob: String): String {
        val date = SimpleDateFormat("ddMMMMyyyy").parse(dob)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        return dateFormat.format(date!!)
    }

    private fun feetToCms(feet: String, inch: String): Double {

        val feetConvert = 30.48 * Integer.parseInt(feet)
        val inchConvert = 2.54 * Integer.parseInt(inch)
        return feetConvert + inchConvert
    }

    private fun setSpinnerListeners() {
        binding.spnrDay.setOnItemSelectedListener(this)
        binding.spnrMonth.setOnItemSelectedListener(this)
        binding.spnrYear.setOnItemSelectedListener(this)
        binding.spnrFeet.setOnItemSelectedListener(this)
        binding.spnrInch.setOnItemSelectedListener(this)
        binding.spnrCms.setOnItemSelectedListener(this)
        binding.spnrKilos.setOnItemSelectedListener(this)
        binding.spnrGrams.setOnItemSelectedListener(this)
        binding.spnrPounds.setOnItemSelectedListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onItemSelected(picker: WheelPicker?, data: Any?, position: Int) {
        when (picker!!.id) {
            R.id.spnrDay -> {
                binding.tvBdHint.visibility = View.GONE
                binding.tvDay.text = data.toString()
            }
            R.id.spnrMonth -> {
                binding.tvBdHint.visibility = View.GONE
                binding.tvMonth.text = data.toString()
            }
            R.id.spnrYear -> {
                binding.tvBdHint.visibility = View.GONE
                binding.tvYear.text = data.toString()
            }
            R.id.spnrFeet -> {
                val feet = data.toString().split(" ")
                binding.tvFeet.text = feet[0] + "'"
            }
            R.id.spnrInch -> {
                val inch = data.toString().split(" ")
                binding.tvInch.text = inch[0] + "\""
            }
            R.id.spnrCms -> {
                val cms = data.toString().split(" ")
                binding.tvFeet.text = cms[0]
            }
            R.id.spnrKilos -> {
                binding.tvKilo.text = data.toString()
            }
            R.id.spnrGrams -> {
                val grams = data.toString().split("0").toMutableList()
                if (grams[0] == ".") {
                    grams[0] = grams[0] + "0"
                }
                binding.tvGrams.text = grams[0]
            }
            R.id.spnrPounds -> {
                val pounds = data.toString().split(" ")
                binding.tvKilo.text = pounds[0]
            }
        }

    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        checkValidation()
    }

    override fun afterTextChanged(p0: Editable?) {
    }

    override fun pickedImage(bitmap: Bitmap) {
        profileBitmap = bitmap
        binding.imgProfile.setImageBitmap(profileBitmap)
        profileViewModel.getUploadImageSasToken()
    }

    private fun checkValidation() {
        binding.apply {
            binding.btnNext.isEnabled =
                (edtFirstName.text.isNotBlank() && edtLastName.text.isNotBlank() && binding.tvDay.text.isNotBlank() && binding.tvMonth.text.isNotBlank() && binding.tvYear.text.isNotBlank()
                        && binding.tgSex.checkedPosition != null && (binding.tvKilo.text.isNotBlank() || binding.tvGrams.text.isNotBlank()))
        }
    }
}