package com.wellnest.one.ui.recording.link

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.rc.wellnestmodule.interfaces.IECGRecordingUpload
import com.rc.wellnestmodule.interfaces.IWellnestGraph
import com.wellnest.one.BuildConfig
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityLinkRecordingBinding
import com.wellnest.one.dto.EcgRecording
import com.wellnest.one.model.Symptoms
import com.wellnest.one.model.request.AddRecordingRequest
import com.wellnest.one.model.response.toDto
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.feedback.ECGFeedbackActivity
import com.wellnest.one.ui.profile.ChooseMemberActivity
import com.wellnest.one.ui.recording.RecordingViewModel
import com.wellnest.one.ui.recording.capture.RecordingEcgActivity
import com.wellnest.one.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

/**
 * Created by Hussain on 17/11/22.
 */
@AndroidEntryPoint
class LinkRecordingActivity : BaseActivity(), IWellnestGraph, View.OnClickListener,
    IECGRecordingUpload {

    private var isRecording = false
    private var pdfBitmap: Bitmap? = null
    private var graphList: ArrayList<ArrayList<Double>> = ArrayList()
    private lateinit var binding: ActivityLinkRecordingBinding

    private var mEcgSetup = "standard"

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var reason: String = ""

    private var ecgFilename = UUID.randomUUID().toString()

    private var ecgRecordingId: Int? = null

    private val recordingViewModel: RecordingViewModel by viewModels()

    private var ecgRecording: EcgRecording? = null

    private val pdfGenerator = PrintBitmapGenerator()
    private var pdfFile: Uri? = null

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

        isRecording = intent.getBooleanExtra("rerecord", false)

        setupClickListeners()
        setupObservers()

        if (isRecording) {
            ecgRecordingId = intent.getIntExtra("id", -1)
            ecgFilename = intent.getStringExtra("filename") ?: UUID.randomUUID().toString()
            recordingViewModel.getEcgRecordingForId(ecgRecordingId!!)
        } else {
            binding.tvTestDate.text = Util.currentTestDateAmPm()
        }

        binding.graphRefresh.setOnRefreshListener {
            wellNestUtil.displayGraphs()
        }

        displaySymptoms()


        recordingViewModel.getUploadTokenForEcg(ecgFilename)

        this.graphList = RecordingGraphHelper.getInstance().chartData

        ProgressHelper.showDialog(this)

    }

    private fun setupObservers() {

        recordingViewModel.ecgRecording.observe(this) {
            ecgRecording = it.toDto()
            it.createdAt?.let { testDate ->
                binding.tvTestDate.text = Util.testDateAmPm(testDate)

            }
        }

        recordingViewModel.ecgUploadToken.observe(this) {
            wellNestUtil.uploadFile(
                BuildConfig.azureHOST,
                ecgFilename,
                it.sasToken,
                Constants.ECG_RECORDINGS,
                this
            )
        }

        recordingViewModel.addRecordingSuccess.observe(this) {
            ProgressHelper.dismissDialog()
            ecgRecording = it.toDto()
            ecgRecordingId = it.id

            it?.let { recording ->
                with(recording) {

                    bpm.let {
                        binding.tvHeart.text = "$bpm bpm"
                    }

                    qrs?.let {
                        binding.tvQRSaxis.text = "$qrs ms"
                    }

                    st?.let {
                        binding.tvSTaxis.text = "$st ms"
                    }

                    qt?.let {
                        binding.tvQTaxis.text = "$qt ms"
                    }

                    pr?.let {
                        binding.tvPRaxis.text = "$pr ms"
                    }

                    qtc?.let {
                        binding.tvQTc.text = "$qtc ms"
                    }
                }
            }

            pdfBitmap = pdfGenerator.createPdf(
                it.toDto(),
                graphList,
                this,
                Util.isoToDate(it.createdAt.toString()),
                "plain",
                null,
                null,
                false,
                graphSetup = "L2"
            )
            pdfFile =
                Util.createPdf(
                    pdfBitmap!!,
                    ecgRecording?.patient, ecgRecording?.createdAt.toString(), this
                )

        }

        recordingViewModel.errorMsg.observe(this) {
            ProgressHelper.dismissDialog()
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        recordingViewModel.linkSuccess.observe(this) {
            if (it) {
                // go to feedback page
                ProgressHelper.dismissDialog()
                Toast.makeText(this, "Patient Linked Successfully", Toast.LENGTH_SHORT).show()
                val feedbackIntent = Intent(this, ECGFeedbackActivity::class.java)
                feedbackIntent.putExtra("id", ecgRecordingId)
                feedbackIntent.putExtra("from", "link")
                startActivity(feedbackIntent)
                finish()
            }
        }

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
                showCloseDialogOptions()
            }
            R.id.imgRetake -> {
                retakeRecording()
            }
            R.id.imgPrint -> {
                pdfFile?.let { sharePdf(it) }
            }
            R.id.btnLinkPatient -> {
                val linkIntent = Intent(this, ChooseMemberActivity::class.java)
                linkIntent.putExtra("id", ecgRecordingId)
                startActivity(linkIntent)
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
                val linkIntent = Intent(this, ChooseMemberActivity::class.java)
                linkIntent.putExtra("id", ecgRecordingId)
                startActivity(linkIntent)
            },
            { _, _ ->
                wellNestUtil.clearData()
                WellNestProUtil.pushLandingActivity(this)
            })
    }

    private fun retakeRecording() {
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
                putExtra("rerecord", true)
                putExtra("id", ecgRecordingId)
                putExtra("filename", ecgRecording?.fileName)
            }

            startActivity(rerecordIntent)
            finish()
        }

    }

    override fun onSuccess() {
        runOnUiThread {
            val bluetoothDevice =
                preferenceManager.getBluetoothDevice()

            var ecgDeviceID = bluetoothDevice?.deviceId ?: 188

            val symptoms = intent.getParcelableExtra<Symptoms>("symptoms")
            symptoms?.let { syms ->
                val ecgRecordingDto = AddRecordingRequest(
                    null,
                    syms.breathlessnessOnExertion,
                    syms.breathlessnessWhileResting,
                    syms.chestPain,
                    ecgDeviceID,
                    null,
                    ecgFilename,
                    null,
                    syms.jawPain,
                    null,
                    syms.palpitation,
                    null,
                    syms.preEmployment,
                    syms.preLifeInsurance,
                    syms.preMediClaim,
                    syms.preOperativeAssessment,
                    reason,
                    syms.routineCheckUp,
                    mEcgSetup,
                    syms.symptomatic,
                    symptoms.uneasiness,
                    syms.unexplainedPerspiration,
                    syms.upperBackPain,
                    syms.vomiting
                )

                recordingViewModel.addRecording(ecgRecordingDto)
            }
        }
    }

    override fun onFailure(message: String?) {
        runOnUiThread {
            ProgressHelper.dismissDialog()

            DialogHelper.showDialog(
                "Error",
                getString(R.string.reupload),
                getString(R.string.discard),
                getString(R.string.reupload_msg),
                this
            ) { p0, p1 ->
                ProgressHelper.showDialog(this)
                if (ecgFilename.isNotBlank()) {
                    recordingViewModel.getUploadTokenForEcg(ecgFilename)
                }
            }
        }
    }
}


