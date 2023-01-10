package com.wellnest.one.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import android.util.Log
import com.wellnest.one.dto.EcgRecording
import com.wellnest.one.utils.Util
import java.util.*

@Parcelize
data class EcgRecordingResponse(
    @SerializedName("acceptedByDoctor")
    val acceptedByDoctor: AcceptedByDoctor?,
    @SerializedName("addSignature")
    val addSignature: Boolean?,
    @SerializedName("age")
    val age: Int?,
    @SerializedName("aiAction")
    val aiAction: String?,
    @SerializedName("aiResponse")
    val aiResponse: String?,
    @SerializedName("bpm")
    val bpm: Int?,
    @SerializedName("canForward")
    val canForward: Boolean?,
    @SerializedName("cardiologyAdvice")
    val cardiologyAdvice: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("data")
    val `data`: List<List<Int?>?>?,
    @SerializedName("ecgFindings")
    val ecgFindings: String?,
    @SerializedName("fileName")
    val fileName: String?,
    @SerializedName("forwardCount")
    val forwardCount: Int?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("hasReported")
    val hasReported: Boolean?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("interpretations")
    val interpretations: String?,
    @SerializedName("organisation")
    val organisation: Organisation?,
    @SerializedName("patient")
    val patient: Patient?,
    @SerializedName("pendingApproval")
    val pendingApproval: Boolean?,
    @SerializedName("pr")
    val pr: Double?,
    @SerializedName("qrs")
    val qrs: Double?,
    @SerializedName("qt")
    val qt: Double?,
    @SerializedName("qtc")
    val qtc: Double?,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("recommendations")
    val recommendations: String?,
    @SerializedName("referredBy")
    val referredBy: ReferredBy?,
    @SerializedName("reportedBy")
    val reportedBy: ReportedBy?,
    @SerializedName("reportedById")
    val reportedById: Int?,
    @SerializedName("reviewStatus")
    val reviewStatus: String?,
    @SerializedName("risk")
    val risk: String?,
    @SerializedName("setup")
    val setup: String?,
    @SerializedName("st")
    val st: Double?,
) : Parcelable {
    fun getPatientName(): String {
        if (patient?.patientLastName != null && patient.patientLastName.isNotEmpty()) {
            return "${patient.patientFirstName!!.capitalize()} ${patient.patientLastName!!.capitalize()}"
        } else {
            return "${patient?.patientFirstName}"
        }
    }

    fun notificationTime(): String {
        if (createdAt == null) return ""
        try {
            val timeStamp = Util.isoToLocalDate(this.createdAt)

            val c1 = Calendar.getInstance(); // today
            val c2 = Calendar.getInstance();
            c2.time = timeStamp // this.createdAt

            return if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
            ) {
                "Today"
            } else {
                Util.isoToDobString(this.createdAt)
            }
        } catch (e: Exception) {
            Log.v("DateFormat", e.message.toString())
            return ""
        }
    }
}

fun EcgRecordingResponse.toDto(): EcgRecording {
    return EcgRecording(
        acceptedByDoctor,
        addSignature,
        age,
        aiAction,
        aiResponse,
        bpm,
        canForward,
        cardiologyAdvice,
        createdAt,
        ecgFindings,
        fileName,
        forwardCount,
        gender,
        hasReported,
        id,
        interpretations,
        patient,
        pendingApproval,
        pr,
        qrs,
        qt,
        qtc,
        reason,
        recommendations,
        referredBy,
        reportedBy,
        reportedById,
        reviewStatus,
        risk,
        setup,
        st
    )
}