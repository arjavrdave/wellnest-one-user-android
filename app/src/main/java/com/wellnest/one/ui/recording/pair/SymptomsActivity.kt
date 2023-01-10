package com.wellnest.one.ui.recording.pair

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.databinding.DataBindingUtil
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivitySymptomsBinding
import com.wellnest.one.model.Symptoms
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.recording.capture.RecordingEcgActivity

class SymptomsActivity : BaseActivity(), View.OnClickListener {


    private lateinit var binding: ActivitySymptomsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_symptoms)

        binding.btnProceed.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.checkboxOther.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.edOther.visibility = View.VISIBLE
            } else {
                binding.edOther.visibility = View.GONE
            }
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> {
                finish()
            }

            R.id.btnProceed -> {
                setUpOption()
            }
        }
    }

    private fun setUpOption() {
        val symptoms = Symptoms(
            binding.checkboxBreathless.isChecked,
            binding.checkboxExertion.isChecked,
            binding.checkboxChest.isChecked,
            binding.checkboxJaw.isChecked,
            binding.checkboxPalpitation.isChecked,
            binding.checkboxEmployment.isChecked,
            binding.checkboxInsureance.isChecked,
            binding.checkboxMediclaim.isChecked,
            binding.checkboxAssesment.isChecked,
            binding.checkboxRoutine.isChecked,
            binding.checkboxUneasiness.isChecked,
            binding.checkboxUnexplained.isChecked,
            binding.checkboxUpper.isChecked,
            binding.checkboxVomiting.isChecked,
            binding.checkboxSymptomatic.isChecked
        )

        val recordingIntent = Intent(this, RecordingEcgActivity::class.java)
        recordingIntent.putExtra("symptoms", symptoms)
        startActivity(recordingIntent)
        finish()
//        Amplitude.getInstance().logEvent("ECGDevice Connected (New Recording)")

//        if (isUsbDeviceConnected) {
//            startActivity(
//                Intent(this, PreUsbRecordingActivity::class.java)
//                    .putExtra("symptoms", symptoms)
//                    .putExtra("ecg_setup", option)
//                    .putExtras(intent.extras!!)
//            )
//        } else {
//            if (intent.extras != null) {
//                startActivity(
//                    Intent(this, RecordEcgActivity::class.java).putExtra("symptoms", symptoms)
//                        .putExtras(intent.extras!!).putExtra("ecg_setup", option)
//                )
//            } else {
//                startActivity(
//                    Intent(this, RecordEcgActivity::class.java).putExtra("symptoms", symptoms)
//                        .putExtra("ecg_setup", option)
//                )
//
//            }
//        }
        finish()
    }

}
