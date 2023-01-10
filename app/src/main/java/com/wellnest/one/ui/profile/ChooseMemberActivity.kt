package com.wellnest.one.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityChooseMemberBinding
import com.wellnest.one.dto.UserProfile
import com.wellnest.one.model.request.LinkMemberRequest
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.feedback.ECGFeedbackActivity
import com.wellnest.one.ui.recording.RecordingViewModel
import com.wellnest.one.utils.ProgressHelper
import com.wellnest.one.utils.Util
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by Hussain on 18/11/22.
 */
@AndroidEntryPoint
class ChooseMemberActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityChooseMemberBinding

    private var linkMember: LinkMemberRequest? = null

    private val recordingViewModel: RecordingViewModel by viewModels()
    private var ecgRecordingId: Int? = null

    private var user: UserProfile? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_member)

        ecgRecordingId = intent.getIntExtra("id", -1)

        user = preferenceManager.getUser()

        binding.self.tvUserName.text = user?.getFullName()

        recordingViewModel.getReadTokenForUser()

        binding.llAddMember.setOnClickListener(this)
        binding.self.root.setOnClickListener(this)
        binding.btnLinkReport.setOnClickListener(this)
        binding.tvCancel.setOnClickListener(this)

        linkMember = LinkMemberRequest(
            user?.getAge()?.toInt() ?: 0,
            user?.firstName,
            user?.gender,
            user?.lastName,
            user?.phoneNumber
        )

        setupObservers()

    }

    private fun setupObservers() {
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

        recordingViewModel.errorMsg.observe(this) {
            ProgressHelper.dismissDialog()
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        recordingViewModel.readTokenUser.observe(this) {
            Util.loadIm age(this,user?.profileId ?: "",it.sasToken,binding.self.imgUser)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {

            R.id.tvCancel -> {
                finish()
            }

            R.id.self -> {
                val user = sharedPreferenceManager.getUser()
                linkMember = LinkMemberRequest(
                    user?.getAge()?.toInt() ?: 0,
                    user?.firstName,
                    user?.gender,
                    user?.lastName,
                    user?.phoneNumber
                )
            }

            R.id.llAddMember -> {
                showAddMemberDialog()
            }

            R.id.btnLinkReport -> {
                linkMember?.let { recordingViewModel.linkRecording(ecgRecordingId!!, it) }
            }
        }
    }

    private fun showAddMemberDialog() {
        val addMemberDialog = AddMemberDialog {
            linkMember = it
            recordingViewModel.linkRecording(ecgRecordingId!!, it)
        }
        addMemberDialog.show(supportFragmentManager, null)
    }

}