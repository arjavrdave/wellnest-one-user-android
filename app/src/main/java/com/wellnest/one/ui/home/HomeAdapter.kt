package com.wellnest.one.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.wellnest.one.R
import com.wellnest.one.databinding.ItemRecordingBinding
import com.wellnest.one.model.response.GetRecordingResponse
import com.wellnest.one.ui.feedback.FeedbackStatus
import com.wellnest.one.utils.Util
import java.util.*
import kotlin.math.roundToInt


class HomeAdapter(private val context: Context) :
    RecyclerView.Adapter<HomeAdapter.RecordingVH>() {
    inner class RecordingVH(val binding: ItemRecordingBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val recordings = mutableListOf<GetRecordingResponse>()
    private var sasToken: String = ""
    private lateinit var recordingItemClickListener: ListItemClickListener

    interface ListItemClickListener {
        fun onItemClick(id: Int, position: Int)
    }

    fun setListener(recordingItemClickListener: ListItemClickListener) {
        this.recordingItemClickListener = recordingItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingVH {
        val inflater = LayoutInflater.from(parent.context)
        return RecordingVH(ItemRecordingBinding.inflate(inflater, parent, false))
    }


    override fun onBindViewHolder(holder: RecordingVH, position: Int) {
        val model = recordings[position]
        val name =
            "${model.patient?.patientFirstName ?: ""} ${model.patient?.patientLastName ?: ""}"
        holder.binding.tvName.text =
            String.format(
                Locale.ENGLISH,
                "%s",
                name.capitalize(Locale.getDefault())
            )

        holder.binding.tvBpm.text =
            String.format(Locale.ENGLISH, "%s", "${model.bpm?.roundToInt()} BPM")
        holder.binding.tvDay.text = model.notificationTime()
        if (model.reason.isNullOrBlank()) {
            holder.binding.tvReason.text = String.format(Locale.ENGLISH, "No Indication")
        } else {
            holder.binding.tvReason.text = String.format(Locale.ENGLISH, "Reason: ${model.reason}")
        }
        model.patient?.profileId?.let {
            Util.loadImage(context, it, sasToken, holder.binding.imgUser)
        }

        if (model.reviewStatus == "PendingFeedback") {
            if (model.forwarded!!) {
                holder.binding.tvAnalysis.text = String.format(Locale.ENGLISH, "Sent for Analysis")
                holder.binding.tvAnalysis.setTextColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.sent_analysis,
                        null
                    )
                )
                holder.binding.root.setOnClickListener {
                    recordingItemClickListener.onItemClick(
                        model.id!!,
                        FeedbackStatus.SentForAnalysis.ordinal
                    )
                }
            } else {
                holder.binding.tvAnalysis.text = String.format(Locale.ENGLISH, "Recording Complete")
                holder.binding.tvAnalysis.setTextColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.recording_complete,
                        null
                    )
                )
                holder.binding.root.setOnClickListener {
                    recordingItemClickListener.onItemClick(
                        model.id!!,
                        FeedbackStatus.RecordingCompleted.ordinal
                    )
                }
            }
        } else {
            holder.binding.tvAnalysis.text = String.format(Locale.ENGLISH, "Analysis Received")
            holder.binding.tvAnalysis.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.green_btn,
                    null
                )
            )
            holder.binding.root.setOnClickListener {
                recordingItemClickListener.onItemClick(
                    model.id!!,
                    FeedbackStatus.AnalysisReceived.ordinal
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return recordings.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addNewRecordings(
        list: MutableList<GetRecordingResponse>
    ) {
        recordings.clear()
        recordings.addAll(list)
        notifyDataSetChanged()
    }

    fun setSasToken(sasToken: String) {
        this.sasToken = sasToken
    }
}
