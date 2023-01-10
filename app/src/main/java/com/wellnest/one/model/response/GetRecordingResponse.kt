package com.wellnest.one.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import android.util.Log
import com.wellnest.one.utils.Util
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class GetRecordingResponse(
    @SerializedName("bpm")
    val bpm: Double?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("forwardCount")
    val forwardCount: Int?,
    @SerializedName("forwarded")
    val forwarded: Boolean?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("patient")
    val patient: PatientX?,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("reviewStatus")
    val reviewStatus: String?
) : Parcelable {
    fun notificationTime(): String {
        if (createdAt == null) return ""
        try {
            val today = Date(System.currentTimeMillis())
            val timeStamp = Util.isoToLocalDate(this.createdAt)
            var timeDiff = today.time - timeStamp.time
            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24
            val weekInMilli = daysInMilli * 7
            val yearInMilli = weekInMilli * 365

            val elapsedYear = timeDiff / yearInMilli
            timeDiff = timeDiff % yearInMilli

            val elapsedWeek = timeDiff / weekInMilli
            timeDiff = timeDiff % weekInMilli

            val elapsedDays = timeDiff / daysInMilli
            timeDiff = timeDiff % daysInMilli

            val elapsedHours = timeDiff / hoursInMilli
            timeDiff = timeDiff % hoursInMilli

            val elapsedMinutes = timeDiff / minutesInMilli
            timeDiff = timeDiff % minutesInMilli

            val elapsedSeconds = timeDiff / secondsInMilli

            val year = String.format("%d", elapsedYear)
            val week = String.format("%d", elapsedWeek)
            val days = String.format("%d", elapsedDays)
            val hours = String.format("%d", elapsedHours)
            val minutes = String.format("%d", elapsedMinutes)
            return if (Integer.valueOf(days) >= 7) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                val notificationDate = dateFormat.format(timeStamp)
                return notificationDate
            } else if (Integer.valueOf(week) > 0) {
                week + " week"
            }
            else if (Integer.valueOf(days) == 1) {
                "Yesterday"
            }
            else if (Integer.valueOf(days) > 0) {
                return SimpleDateFormat("EEEE", Locale.ENGLISH).format(timeStamp);
            }else if (Integer.valueOf(hours) > 0) {
                return hours+" hour ago";
            }
            else if (Integer.valueOf(minutes) > 0) {
                if (Integer.valueOf(minutes)==1){
                    return minutes+" minute ago";
                }else{
                    return minutes+" minutes ago";
                }

            }
            else {
                return "a moment ago";
            }
        } catch (e: Exception) {
            Log.v("DateFormat", e.message.toString())
            return ""
        }

    }

}