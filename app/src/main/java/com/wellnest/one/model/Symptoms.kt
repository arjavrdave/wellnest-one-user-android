package com.wellnest.one.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by Hussain on 16/11/22.
 */
@Parcelize
data class Symptoms(
    @SerializedName("breathlessnessOnExertion")
    val breathlessnessOnExertion: Boolean,
    @SerializedName("breathlessnessWhileResting")
    val breathlessnessWhileResting: Boolean,
    @SerializedName("chestPain")
    val chestPain: Boolean,
    @SerializedName("jawPain")
    val jawPain: Boolean,
    @SerializedName("palpitation")
    val palpitation: Boolean,
    @SerializedName("preEmployment")
    val preEmployment: Boolean,
    @SerializedName("preLifeInsurance")
    val preLifeInsurance: Boolean,
    @SerializedName("preMediClaim")
    val preMediClaim: Boolean,
    @SerializedName("preOperativeAssessment")
    val preOperativeAssessment: Boolean,
    @SerializedName("routineCheckUp")
    val routineCheckUp: Boolean,
    @SerializedName("uneasiness")
    val uneasiness: Boolean,
    @SerializedName("unexplainedPerspiration")
    val unexplainedPerspiration: Boolean,
    @SerializedName("upperBackPain")
    val upperBackPain: Boolean,
    @SerializedName("vomiting")
    val vomiting: Boolean,
    @SerializedName("symptomatic")
    val symptomatic: Boolean
): Parcelable