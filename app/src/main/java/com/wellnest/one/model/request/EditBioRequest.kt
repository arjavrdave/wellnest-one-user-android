package com.wellnest.one.model.request


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class EditBioRequest(
    @SerializedName("exerciseLevel")
    val exerciseLevel: String?,
    @SerializedName("height")
    val height: Int?,
    @SerializedName("heightUnit")
    val heightUnit: String?,
    @SerializedName("smoking")
    val smoking: String?,
    @SerializedName("tobaccoUse")
    val tobaccoUse: String?,
    @SerializedName("weight")
    val weight: Int?,
    @SerializedName("weightUnit")
    val weightUnit: String?
) : Parcelable