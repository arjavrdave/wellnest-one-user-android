package com.wellnest.one.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.wellnest.one.dto.EcgRecording

@Parcelize
data class AddRecordingResponse(
    @SerializedName("bpm")
    val bpm: Int?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("patient")
    val patient: Patient?,
    @SerializedName("reason")
    val reason: String?=null,
    @SerializedName("reviewStatus")
    val reviewStatus: String,
    @SerializedName("forwardCount")
    val forwardCount:Int,
    @SerializedName("risk")
    val risk: String? = null,
    @SerializedName("hasReported")
    val hasReported: Boolean = false,
    @SerializedName("canForward")
    val canForward: Boolean = false,
    @SerializedName("qrs")
    val qrs: Double?,
    @SerializedName("st")
    val st: Double?,
    @SerializedName("qtc")
    val qtc: Double?,
    @SerializedName("pr")
    val pr: Double?,
    @SerializedName("qt")
    val qt: Double?,
    @SerializedName("referredBy")
    val referredBy: ReferredBy?,
    @SerializedName("reportedById")
    val reportedById : Int?,
    @SerializedName("pendingApproval")
    val pendingApproval : Boolean?
) : Parcelable

fun AddRecordingResponse.toDto() : EcgRecording {
    return EcgRecording(
        bpm = bpm,
        createdAt = createdAt,
        id = id,
        patient = patient,
        reason = reason,
        reviewStatus = reviewStatus,
        forwardCount = forwardCount,
        risk = risk,
        hasReported = hasReported,
        canForward = canForward,
        qrs = qrs,
        st = st,
        qt = qt,
        qtc = qtc,
        pr = pr,
        referredBy = referredBy,
        reportedById = reportedById,
        pendingApproval = pendingApproval
    )
}