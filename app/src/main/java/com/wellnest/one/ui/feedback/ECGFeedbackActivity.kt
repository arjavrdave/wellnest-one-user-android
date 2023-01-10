package com.wellnest.one.ui.feedback

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.rc.wellnestmodule.graphview.EcgLoaderView
import com.rc.wellnestmodule.graphview.EcgView
import com.rc.wellnestmodule.interfaces.IWellnestEcgFileListener
import com.wellnest.one.BuildConfig
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityEcgFeedbackBinding
import com.wellnest.one.model.response.EcgRecordingResponse
import com.wellnest.one.model.response.toDto
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.recording.RecordingViewModel
import com.wellnest.one.utils.*
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Hussain on 24/11/22.
 */
class ECGFeedbackActivity : BaseActivity(), View.OnClickListener, IWellnestEcgFileListener {

    private lateinit var binding: ActivityEcgFeedbackBinding

    private var graphData: ArrayList<ArrayList<Double>> = ArrayList()
    private var ecgRecordingId: Int = -1

    private var ecgRecording: EcgRecordingResponse? = null
    private val recordingViewModel: RecordingViewModel by viewModels()
    private val pdfGenerator = PrintBitmapGenerator()
    private var pdfFile: Uri? = null
    private var pdfBitmap: Bitmap? = null
    private var ecgFeedbackStatus = FeedbackStatus.RecordingCompleted
    private var signatureBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor =
                ContextCompat.getColor(this@ECGFeedbackActivity, R.color.activity_gray_bkg)// S
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ecg_feedback)

        binding.imgClose.setOnClickListener(this)
        binding.imgPrint.setOnClickListener(this)
        binding.imgRetake.setOnClickListener(this)
        binding.btnFeedBack.setOnClickListener(this)
        binding.llShare.setOnClickListener(this)

        setupObservers()

        ecgRecordingId = intent.getIntExtra("id", -1)

        when (intent.getIntExtra("status", -1)) {
            FeedbackStatus.RecordingCompleted.ordinal -> {
                ecgFeedbackStatus = FeedbackStatus.RecordingCompleted
                binding.layoutFeedBack.visibility = View.VISIBLE
            }

            FeedbackStatus.SentForAnalysis.ordinal -> {
                ecgFeedbackStatus = FeedbackStatus.SentForAnalysis
                binding.layoutFeedBack.visibility = View.GONE
            }

            FeedbackStatus.AnalysisReceived.ordinal -> {
                ecgFeedbackStatus = FeedbackStatus.AnalysisReceived
                binding.layoutFeedBack.visibility = View.GONE
            }
        }

        recordingViewModel.getEcgRecordingForId(ecgRecordingId)

        val ecgLoaderView = EcgLoaderView(this)
        binding.graphView.addView(ecgLoaderView.getLoaderView())

    }

    private fun setupObservers() {

        recordingViewModel.ecgRecording.observe(this) { recording ->
            ecgRecording = recording
            recordingViewModel.getReadTokenForEcg(recording.fileName ?: "")


            recording?.let { ecgRecording ->
                ProgressHelper.dismissDialog()
                this.ecgRecording = ecgRecording
                with(ecgRecording) {

                    when (intent.getIntExtra("status", -1)) {
                        FeedbackStatus.RecordingCompleted.ordinal -> {
                            binding.tvHeader.text = notificationTime()
                        }

                        FeedbackStatus.SentForAnalysis.ordinal -> {
                            binding.tvHeader.text = "ECG Report - ${
                                ecgRecording.patient?.fullName()!!.capitalize(Locale.getDefault())}"
                        }

                        FeedbackStatus.AnalysisReceived.ordinal -> {
                            binding.tvHeader.text = "ECG Report - ${ecgRecording.patient?.fullName()!!.capitalize(Locale.getDefault())}"
                        }
                    }

                    binding.tvTestDate.text = Util.testDateAmPm(createdAt ?: "")
                    if (patient?.patientGender != null && patient.patientDateOfBirth != null) {
                        binding.tvGenderAge.text =
                            Util.getGenderAge(
                                patient.patientGender,
                                patient.patientDateOfBirth
                            )
                    }

                    binding.btnFeedBack.isEnabled = true

                    binding.tvName.text = patient?.fullName()?.capitalize(Locale.getDefault()) ?: ""



                    bpm?.let {
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

                if (ecgRecording.reason != "") {
                    binding.tvReason.text = ecgRecording.reason
                } else {
                    binding.tvReason.text = getString(R.string.no_indication)
                }

                if (ecgFeedbackStatus == FeedbackStatus.AnalysisReceived) {
                    binding.llFeedback.visibility = View.VISIBLE

                    binding.tvFindings.text = recording.ecgFindings
                    binding.tvRecommendations.text = recording.recommendations
                    binding.tvInterpretation.text = recording.interpretations
                    if (recording.addSignature == true) {
                        recordingViewModel.getReadTokenForSignature(recording.reportedById ?: 0)
                    }

                    binding.llCondition.visibility = View.VISIBLE
                    setRiskValue(recording.risk ?: "")
                }


                if (ecgRecording.reportedBy != null) {
                    val fullName = ecgRecording.reportedBy.toString()
                    binding.tvDoctorName.text = fullName
                    binding.tvPhone.text = Util.formatPhone(
                        ecgRecording.reportedBy.phoneNumber,
                        ecgRecording.reportedBy.countryCode.toString()
                    )
                    binding.tvQualification.visibility = View.VISIBLE
                    binding.tvQualification.text = ecgRecording.reportedBy.qualification ?: ""
                }
            }

        }

        recordingViewModel.ecgReadToken.observe(this) {
            wellNestUtil.getFile(
                ecgRecording?.fileName ?: "",
                BuildConfig.azureHOST,
                it.sasToken,
                Constants.ECG_RECORDINGS,
                this
            )
        }

        recordingViewModel.errorMsg.observe(this) {
            ProgressHelper.dismissDialog()
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        recordingViewModel.sendForFeedbackSuccess.observe(this) {
            ProgressHelper.dismissDialog()
            if (it) {
                val dialog = DialogHelper.showSuccessDialog(this, "Report sent for analysis")
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                    WellNestProUtil.pushLandingActivity(this)
                }, 2000)
            }
        }

        recordingViewModel.readSignatureToken.observe(this) {
            ProgressHelper.dismissDialog()
            if (it != null) {
                val signatureUrl =
                    BuildConfig.azureHOST + Constants.SIGNATURES + "/${ecgRecording?.reportedById}" + it.sasToken

                Glide.with(this)
                    .asBitmap()
                    .load(signatureUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            binding.imgSignature.visibility = View.VISIBLE
                            binding.imgSignature.setImageBitmap(resource)
                            signatureBitmap = resource
                            generatePdf()
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            Toast.makeText(
                                this@ECGFeedbackActivity,
                                "Unable to load signature",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                    })
            }
        }
    }

    private fun setRiskValue(it: String) {
        binding.tvCondition.text = it
        if (it.equals(getString(R.string.no_action_required))) {
            binding.tvCondition.setTextColor(
                ContextCompat.getColor(
                    this@ECGFeedbackActivity,
                    R.color.no_action
                )
            )
        } else if (it == getString(R.string.urgent_action_required)) {
            binding.tvCondition.setTextColor(
                ContextCompat.getColor(
                    this@ECGFeedbackActivity,
                    R.color.urgent_action
                )
            )
        } else if (it == getString(R.string.action_required_not_urgent)) {
            binding.tvCondition.setTextColor(
                ContextCompat.getColor(
                    this@ECGFeedbackActivity,
                    R.color.condition_color
                )
            )
        }
    }


    override fun onBackPressed() {

        val from = intent.getStringExtra("from")
        if (from == "link" || from == "pushnotification") {
            WellNestProUtil.pushLandingActivity(this)
        } else {
            finish()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgClose -> {
                onBackPressed()
            }

            R.id.imgPrint -> {
                pdfFile?.let { sharePdf(it) }
            }

            R.id.llShare -> {
                pdfFile?.let { sharePdf(it) }
            }

            R.id.imgRetake -> {

            }
            R.id.btnFeedBack -> {
                DialogHelper.showDialog(
                    "GET ECG ANALYSIS",
                    "OK",
                    "Cancel",
                    getString(R.string.ecg_analysis_msg),
                    this, { dialog, _ ->
                        ProgressHelper.showDialog(this)
                        recordingViewModel.sendForFeedback(ecgRecordingId)
                        dialog.dismiss()
                    }, { dialog, _ ->
                        dialog.dismiss()
                    }
                ).show()
            }

        }
    }

    override fun onSuccess(recordingData: ArrayList<List<Byte>>) {
        val chartData =
            recordsByteHandler.setUpDataForRecording(recordingData)

        val graphList = recordsByteHandler.getGraphList(
            chartData,
            ecgRecording?.bpm?.toInt() ?: 0,
            allData = true
        )
        val ecgView = EcgView(this)
        val graphView = ecgView.getGraphView(
            ecgRecording?.setup ?: "standard",
            graphList
        )

        binding.graphView.removeAllViews()
        binding.graphView.addView(graphView)
        this.graphData = chartData
        generatePdf()
    }

    private fun generatePdf() {
        if (graphData.isEmpty()) return
        pdfBitmap = pdfGenerator.createPdf(
            ecgRecording?.toDto(), graphData, this, Util.isoToDate(
                ecgRecording?.createdAt.toString()
            ), "plain", signatureBitmap, null, ecgRecording?.addSignature ?: false, graphSetup = "L2"
        )
        pdfFile = Util.createPdf(
            pdfBitmap!!,
            ecgRecording?.patient!!, ecgRecording?.createdAt!!, this
        )
    }

    override fun onFailure(exception: Exception) {

    }

}