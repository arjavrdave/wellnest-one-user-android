package com.wellnest.one.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class MedicalHistoryResponse(
    @SerializedName("bloodPressure")
    val bloodPressure: Boolean?,
    @SerializedName("diabetes")
    val diabetes: Boolean?,
    @SerializedName("healthComment")
    val healthComment: String?,
    @SerializedName("heartProblem")
    val heartProblem: Boolean?,
    @SerializedName("hypothyroidism")
    val hypothyroidism: Boolean?,
    @SerializedName("kidneyProblem")
    val kidneyProblem: Boolean?,
    @SerializedName("lipidsIssue")
    val lipidsIssue: Boolean?,
    @SerializedName("siblingHeartProblem")
    val siblingHeartProblem: Boolean?,
    @SerializedName("stroke")
    val stroke: Boolean?
) : Parcelable