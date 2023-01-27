package com.wellnest.one.ui.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.wellnest.one.R
import com.wellnest.one.databinding.ItemRecordingBinding
import com.wellnest.one.model.response.GetRecordingResponse
import com.wellnest.one.ui.feedback.ECGFeedbackActivity
import com.wellnest.one.ui.feedback.FeedbackStatus
import com.wellnest.one.utils.Util
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by Hussain on 28/11/22.
 */
class HomeAdapter(private val context: Context) :
    RecyclerView.Adapter<HomeAdapter.RecordingVH>() {

    private val recordings = mutableListOf<GetRecordingResponse>()

    private var sasToken: String = ""

    inner class RecordingVH(val binding: ItemRecordingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recording: GetRecordingResponse) {
            val name =
                "${recording.patient?.patientFirstName ?: ""} ${recording.patient?.patientLastName ?: ""}"
            binding.tvName.text = name.capitalize(Locale.getDefault())
            binding.tvBpm.text = "${recording.bpm?.roundToInt()} BPM"
            binding.tvDay.text = recording.notificationTime()
            if (recording.reason.isNullOrBlank()) {
                binding.tvReason.text = "No Indication"
            } else {
                binding.tvReason.text = "Reason: ${recording.reason}"
            }

            recording.patient?.profileId?.let {
                Util.loadImage(context, it, sasToken, binding.imgUser)
            }

            if (recording.reviewStatus == "PendingFeedback") {
                if (recording.forwarded == false) {
                    binding.tvAnalysis.text = "Recording Complete"
                    binding.tvAnalysis.setTextColor(
                        ResourcesCompat.getColor(
                            context.resources,
                            R.color.recording_complete,
                            null
                        )
                    )
                    binding.root.setOnClickListener {
                        val feedbackIntent = Intent(context, ECGFeedbackActivity::class.java)
                        feedbackIntent.putExtra("id", recording.id)
                        feedbackIntent.putExtra("status", FeedbackStatus.RecordingCompleted.ordinal)
                        context.startActivity(feedbackIntent)
                    }
                } else {
                    binding.tvAnalysis.text = "Sent for Analysis"
                    binding.tvAnalysis.setTextColor(
                        ResourcesCompat.getColor(
                            context.resources,
                            R.color.sent_analysis,
                            null
                        )
                    )
                    binding.root.setOnClickListener {
                        val feedbackIntent = Intent(context, ECGFeedbackActivity::class.java)
                        feedbackIntent.putExtra("id", recording.id)
                        feedbackIntent.putExtra("status", FeedbackStatus.SentForAnalysis.ordinal)
                        context.startActivity(feedbackIntent)
                    }
                }
            } else {
                binding.tvAnalysis.text = "Analysis Received"
                binding.tvAnalysis.setTextColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.green_btn,
                        null
                    )
                )
                binding.root.setOnClickListener {
                    val feedbackIntent = Intent(context, ECGFeedbackActivity::class.java)
                    feedbackIntent.putExtra("id", recording.id)
                    feedbackIntent.putExtra("status", FeedbackStatus.AnalysisReceived.ordinal)
                    context.startActivity(feedbackIntent)
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingVH {
        val inflater = LayoutInflater.from(parent.context)
        return RecordingVH(ItemRecordingBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecordingVH, position: Int) {
        holder.bind(recordings[position])
    }

    override fun getItemCount(): Int {
        return recordings.size
    }

    fun setSasToken(sasToken: String) {
        this.sasToken = sasToken
    }

    fun addNewRecordings(
        list: MutableList<GetRecordingResponse>,
        isSearch: Boolean,
        charLength: Int
    ) {
        if (list.size != 0) {
            recordings.clear()
        }
        if (isSearch && charLength >= 3) {
            recordings.clear()
        }

        recordings.addAll(list)
        notifyDataSetChanged()
    }
}
