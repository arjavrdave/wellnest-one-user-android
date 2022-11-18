package com.wellnest.one.ui.recording.link

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.rc.wellnestmodule.interfaces.IWellnestGraph
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityLinkRecordingBinding
import com.wellnest.one.model.Symptoms
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.profile.ChooseMemberActivity
import com.wellnest.one.ui.recording.capture.RecordingEcgActivity
import com.wellnest.one.utils.Constants
import com.wellnest.one.utils.DialogHelper
import com.wellnest.one.utils.Util
import com.wellnest.one.utils.WellNestProUtil
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

/**
 * Created by Hussain on 17/11/22.
 */
@AndroidEntryPoint
class LinkRecordingActivity : BaseActivity(), IWellnestGraph, View.OnClickListener {

    private lateinit var binding: ActivityLinkRecordingBinding

    private var mEcgSetup = "standard"

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var reason: String = ""
    private var isLinked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor =
                ContextCompat.getColor(this@LinkRecordingActivity, R.color.activity_gray_bkg)
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_link_recording)

        initialize()

    }

    private fun initialize() {

        isLinked = intent.getBooleanExtra(Constants.isLinked, false)

        setupClickListeners()

        if (isLinked) {
            // patient is linked
        } else {
            binding.tvTestDate.text = Util.currentTestDateAmPm()
        }

        binding.graphRefresh.setOnRefreshListener {
            wellNestUtil.displayGraphs()
        }

        displaySymptoms()

    }

    private fun setupClickListeners() {
        binding.btnLinkPatient.setOnClickListener(this)
        binding.imgClose.setOnClickListener(this)
        binding.imgRetake.setOnClickListener(this)
        binding.imgPrint.setOnClickListener(this)
    }

    private fun displaySymptoms() {
        val symptoms = intent.getParcelableExtra<Symptoms>("symptoms")
        symptoms?.let {
            if (symptoms.routineCheckUp) {
                reason = "Routine Check Up"
            }
            if (symptoms.preOperativeAssessment) {
                if (!reason.equals("")) {
                    reason = "$reason, Pre-Operative Assessment"
                } else {
                    reason = "Pre-Operative Assessment"
                }
            }
            if (symptoms.preLifeInsurance) {
                if (!reason.equals("")) {
                    reason = "$reason, Pre-Life Insurance"
                } else {
                    reason = "Pre-Life Insurance"
                }
            }
            if (symptoms.preMediClaim) {
                if (!reason.equals("")) {
                    reason = "$reason, Pre-Mediclaim"
                } else {
                    reason = "Pre-Mediclaim"
                }
            }
            if (symptoms.preEmployment) {
                if (!reason.equals("")) {
                    reason = "$reason, Pre-Employment"
                } else {
                    reason = "Pre-Employment"
                }
            }
            if (symptoms.chestPain) {
                if (!reason.equals("")) {
                    reason = "$reason, Chest Pain"
                } else {
                    reason = "Chest Pain"
                }
            }

            if (symptoms.uneasiness) {
                if (!reason.equals("")) {
                    reason = "$reason, Uneasiness"
                } else {
                    reason = "Uneasiness"
                }
            }
            if (symptoms.jawPain) {
                if (!reason.equals("")) {
                    reason = "$reason, Jaw Pain"
                } else {
                    reason = "Jaw Pain"
                }
            }
            if (symptoms.upperBackPain) {
                if (!reason.equals("")) {
                    reason = "$reason, Upper Back Pain"
                } else {
                    reason = "Upper Back Pain"
                }
            }
            if (symptoms.palpitation) {
                if (!reason.equals("")) {
                    reason = "$reason, Palpitation"
                } else {
                    reason = "Palpitation"
                }
            }
            if (symptoms.vomiting) {
                if (!reason.equals("")) {
                    reason = "$reason, Vomiting"
                } else {
                    reason = "Vomiting"
                }
            }
            if (symptoms.unexplainedPerspiration) {
                if (!reason.equals("")) {
                    reason = "$reason, Unexplained Perspiration"
                } else {
                    reason = "Unexplained Perspiration"
                }
            }
            if (symptoms.breathlessnessOnExertion) {
                if (!reason.equals("")) {
                    reason = "$reason, Breathlessness On Exertion"
                } else {
                    reason = "Breathlessness On Exertion"
                }
            }
            if (symptoms.breathlessnessWhileResting) {
                if (!reason.equals("")) {
                    reason = "$reason, Breathlessness While Resting"
                } else {
                    reason = "Breathlessness While Resting"
                }
            }

            if (symptoms.symptomatic) {
                if (!reason.equals("")) {
                    reason = "$reason, Symptomatic"
                } else {
                    reason = "Symptomatic"
                }
            }

            if (reason != "") {
                binding.tvReason.text = reason
            } else {
                binding.tvReason.text = getString(R.string.no_indication)
                reason = ""
            }
        }
    }

    override fun onResume() {
        super.onResume()
        wellNestUtil.setEcgSetup(mEcgSetup)
        wellNestUtil.displayGraphs()
    }

    override fun setGraphView(view: View) {
        binding.graphRefresh.isRefreshing = false
        binding.graphView.removeAllViews()
        binding.graphView.addView(view)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgClose -> {
                if (isLinked) {

                } else {
                    showCloseDialogOptions()
                }
            }

            R.id.imgRetake -> {

            }

            R.id.imgPrint -> {

            }

            R.id.btnLinkPatient -> {
                val chooseMemberIntent = Intent(this, ChooseMemberActivity::class.java)
                startActivity(chooseMemberIntent)
            }
        }
    }

    private fun showCloseDialogOptions() {
        DialogHelper.showDialogWithNeutral("DISCARD ECG",
            "Link Patient",
            "Discard ECG",
            "Cancel",
            getString(R.string.discard_ecg),
            this,
            { _, _ ->

//                if (isConnectedToInternet(this)) {
//                    startActivity(
//                        Intent(
//                            this,
//                            NewRecordingPatientSearchActivity::class.java
//                        ).apply {
//                            putExtras(intent.extras!!)
//                            if (mEcgRecordingId != null)
//                                putExtra("recording_id", mEcgRecordingId)
//                            putExtra("quality", quality)
//                            putExtra("reRecord", false) //reseting for retaking
//                        }
//                    )
//                } else {
//
//                    startActivity(
//                        Intent(
//                            this,
//                            AddPatientActivity::class.java
//                        ).apply {
//                            putExtras(intent.extras!!)
//                            if (mOfflineId != null)
//                                putExtra("recording_id", mOfflineId!!.toInt())
//                            putExtra("quality", quality)
//                            putExtra("reRecord", false)
//                            putExtra("ecg_dto", mEcgRecordingDto)
//                            putExtra("internet_status", mIsConnected)
//                        })
//                }
            },
            { _, _ ->
                wellNestUtil.clearData()
                WellNestProUtil.pushLandingActivity(this)
            })
    }

    private fun retakeRecording() {
//        if (mPatientId != -1) {
//            val eventProp = JSONObject()
//            eventProp.put("patientId", mPatientId)
//            Amplitude.getInstance().logEvent("Retake Recording", eventProp)
//        }

        DialogHelper.showDialog(
            getString(R.string.re_record),
            getString(R.string.yes),
            getString(R.string.no),
            getString(R.string.rec_record_ecg_message),
            this
        ) { _, _ ->
            val rerecordIntent: Intent = Intent(
                this,
                RecordingEcgActivity::class.java
            ).apply {
                intent.extras?.let { putExtras(it) }
                putExtra("reRecord", true)

            }

            startActivity(rerecordIntent)
            finish()
        }

    }
}


