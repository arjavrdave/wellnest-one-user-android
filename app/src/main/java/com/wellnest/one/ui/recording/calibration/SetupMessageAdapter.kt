package com.wellnest.one.ui.recording.calibration

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wellnest.one.R
import com.wellnest.one.databinding.ItemSetupMsgBinding
import com.wellnest.one.model.SetupMessageInfo
import java.util.*
import kotlin.collections.ArrayList

class SetupMessageAdapter(private val navigateNext : () -> Unit) : RecyclerView.Adapter<SetupMessageAdapter.SetupMessageVH>() {
    private lateinit var msgs: SetupMessageInfo
    private var completedTask = ArrayList<Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetupMessageVH {
        val inflater = LayoutInflater.from(parent.context)
        val time = msgs.msgTime
        val successTime = msgs.successTime
        return SetupMessageVH(
            ItemSetupMsgBinding.inflate(inflater, parent, false),
            time,
            successTime
        )
    }

    override fun onBindViewHolder(holder: SetupMessageVH, position: Int) {
        val msg = msgs.messages[position]

        holder.bind(msg)
    }

    override fun getItemCount(): Int {
        return if (this::msgs.isInitialized) return msgs.messages.size else 0
    }

    fun setupMessage(msgs: SetupMessageInfo) {
        this.msgs = msgs
        completedTask = ArrayList(msgs.messages.size)
        for (i in 0 until msgs.messages.size)
            completedTask.add(false)
    }


    inner class SetupMessageVH(
        private val binding: ItemSetupMsgBinding,
        val time: Int,
        val successTime: Int
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val defaultTimer = Timer()
        private var isCompleted = false
        private val handler = Handler(Looper.getMainLooper())

        fun bind(msg: String) {
            binding.tvMsg.text = msg
            setupDefaultTimer()
        }

        private fun setupDefaultTimer() {
            val duration = if (adapterPosition < msgs.messages.size-1)
                time.toLong() * adapterPosition
            else
                time.toLong() * Int.MAX_VALUE

            defaultTimer.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        binding.tvMsg.setTextColor(Color.BLACK)
                        binding.imgTick.setImageResource(R.drawable.checkmark_active)
                        isCompleted = true
                        completedTask[adapterPosition] = true
                    }
                }

            }, duration)
        }

        fun setupSuccessTimer() {
            if (isCompleted) return
            defaultTimer.cancel()
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        binding.tvMsg.setTextColor(Color.BLACK)
                        binding.imgTick.setImageResource(R.drawable.checkmark_active)
                        completedTask[adapterPosition] = true
                        var allCompleted = false
                        for (i in completedTask.indices) {
                            if (!completedTask[i]) {
                                allCompleted = false
                                break
                            } else {
                                allCompleted = true
                            }
                        }

                        // navigate to next screen
                        if (allCompleted) {
                            navigateNext()
                        }
                    }
                }

            }, successTime.toLong() * (adapterPosition+1))
        }


    }
}


inline fun <reified T : RecyclerView.ViewHolder> RecyclerView.forEachVisibleHolder(
    action: (T) -> Unit
) {
    for (i in 0 until childCount) {
        action(getChildViewHolder(getChildAt(i)) as T)
    }
}
