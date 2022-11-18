package com.wellnest.one.model.request


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class UserProfileRequest(
    @SerializedName("countryCode")
    val countryCode: Int?,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("height")
    val height: Int?,
    @SerializedName("heightUnit")
    val heightUnit: String?,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("medicalHstory")
    val medicalHstory: MedicalHstory?,
    @SerializedName("phonenumber")
    val phonenumber: String?,
    @SerializedName("weight")
    val weight: Int?,
    @SerializedName("weightUnit")
    val weightUnit: String?,
    @SerializedName("smoking")
    val smoking: String,
    @SerializedName("tobaccoUse")
    val tobaccoUse: String,
    @SerializedName("exerciseLevel")
    val exerciseLevel: String
) : Parcelable