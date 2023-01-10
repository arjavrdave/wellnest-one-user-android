package com.wellnest.one.model.request


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class AddRecordingRequest(
    @SerializedName("age")
    val age: Int?,
    @SerializedName("breathlessnessOnExertion")
    val breathlessnessOnExertion: Boolean?,
    @SerializedName("breathlessnessWhileResting")
    val breathlessnessWhileResting: Boolean?,
    @SerializedName("chestPain")
    val chestPain: Boolean?,
    @SerializedName("ecgDeviceID")
    val ecgDeviceID: Int?,
    @SerializedName("familyMemberId")
    val familyMemberId: Int?,
    @SerializedName("fileName")
    val fileName: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("jawPain")
    val jawPain: Boolean?,
    @SerializedName("others")
    val others: String?,
    @SerializedName("palpitation")
    val palpitation: Boolean?,
    @SerializedName("patientId")
    val patientId: Int?,
    @SerializedName("preEmployment")
    val preEmployment: Boolean?,
    @SerializedName("preLifeInsurance")
    val preLifeInsurance: Boolean?,
    @SerializedName("preMediClaim")
    val preMediClaim: Boolean?,
    @SerializedName("preOperativeAssessment")
    val preOperativeAssessment: Boolean?,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("routineCheckUp")
    val routineCheckUp: Boolean?,
    @SerializedName("setup")
    val setup: String?,
    @SerializedName("symptomatic")
    val symptomatic: Boolean?,
    @SerializedName("uneasiness")
    val uneasiness: Boolean?,
    @SerializedName("unexplainedPerspiration")
    val unexplainedPerspiration: Boolean?,
    @SerializedName("upperBackPain")
    val upperBackPain: Boolean?,
    @SerializedName("vomiting")
    val vomiting: Boolean?
) : Parcelable