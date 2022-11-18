package com.wellnest.one.ui.profile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityMedicalHistoryBinding
import com.wellnest.one.model.request.MedicalHstory
import com.wellnest.one.model.request.UpdateMedHistoryRequest
import com.wellnest.one.model.request.UserProfileRequest
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by Hussain on 10/11/22.
 */
@AndroidEntryPoint
class MedicalHistoryActivity : BaseActivity(), View.OnClickListener {


    private lateinit var binding: ActivityMedicalHistoryBinding

    private val profileViewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_medical_history)
        binding.btnSave.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)

        val from = intent.getStringExtra("fromActivity")
        if (from == UserProfileActivity::class.java.simpleName) {
            profileViewModel.getMedicalHistory()
        }

        binding.edOther.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val length = p0?.length ?: 0
                binding.tvLength.text = "$length/240"
            }

        })

        setupObservers()

    }

    private fun setupObservers() {

        profileViewModel.medicalHistoryData.observe(this) { medicalHistory ->
            preferenceManager.saveMedicalHistory(medicalHistory)

            if (medicalHistory.diabetes == true) binding.chkDiabetes.isChecked = true
            if (medicalHistory.stroke == true) binding.chkStroke.isChecked = true
            if (medicalHistory.kidneyProblem == true) binding.chkKidney.isChecked = true
            if (medicalHistory.bloodPressure == true) binding.chkBp.isChecked = true
            if (medicalHistory.hypothyroidism == true) binding.chkHypo.isChecked = true
            if (medicalHistory.heartProblem == true) binding.chkHeart.isChecked = true
            if (medicalHistory.lipidsIssue == true) binding.chkLipids.isChecked = true

            if (medicalHistory.siblingHeartProblem == true) binding.rgYes.isChecked = true

            if (!medicalHistory.healthComment.isNullOrBlank())
                binding.edOther.setText(medicalHistory.healthComment)
        }

        profileViewModel.updateSuccess.observe(this) {
            if (it) {
                Toast.makeText(this, "Medical History Updated Successfully.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }

        profileViewModel.profileSuccess.observe(this) {
            if (it) {
                Toast.makeText(this, "Profile Created Successfully", Toast.LENGTH_SHORT).show()
                val homeIntent = Intent(this, HomeActivity::class.java)
                homeIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(homeIntent)
            }
        }

        profileViewModel.errorMsg.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.imgBack -> finish()
            R.id.btnSave -> {
                saveMedicalHistory()
            }
        }

    }

    private fun saveMedicalHistory() {
        val from = intent.getStringExtra("fromActivity")
        val bp = binding.chkBp.isChecked
        val diabetes = binding.chkDiabetes.isChecked
        val hypo = binding.chkHypo.isChecked
        val lipid = binding.chkLipids.isChecked
        val kidney = binding.chkKidney.isChecked
        val stroke = binding.chkStroke.isChecked
        val heart = binding.chkHeart.isChecked

        val qa = !binding.rgNo.isChecked

        val other = binding.edOther.text.toString()

        if (from == UserProfileActivity::class.java.simpleName) {
            val userId = preferenceManager.getUser()?.id ?: 0
            val request = UpdateMedHistoryRequest(bp,diabetes,other,heart,hypo,kidney,lipid,qa,stroke)
            profileViewModel.updateMedicalHistory(userId ,request)
        } else {
            val profile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.extras?.getParcelable("profile")
            } else {
                intent.getParcelableExtra<UserProfileRequest>("profile")
            }


            val updated = profile?.copy(
                medicalHstory = MedicalHstory(
                    bp,
                    diabetes,
                    other,
                    heart,
                    hypo,
                    kidney,
                    lipid,
                    qa,
                    stroke
                )
            )
                ?: return

            profileViewModel.addProfile(updated)
        }
    }
}