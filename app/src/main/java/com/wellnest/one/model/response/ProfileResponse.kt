package com.wellnest.one.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.wellnest.one.dto.UserProfile
import com.wellnest.one.model.request.MedicalHstory

@Parcelize
data class ProfileResponse(
    @SerializedName("canReadECG")
    val canReadECG: Boolean?,
    @SerializedName("countryCode")
    val countryCode: Int?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("exerciseLevel")
    val exerciseLevel: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("height")
    val height: Double?,
    @SerializedName("heightUnit")
    val heightUnit: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("mciNumber")
    val mciNumber: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("qualification")
    val qualification: String?,
    @SerializedName("smoking")
    val smoking: String?,
    @SerializedName("startOfPractice")
    val startOfPractice: String?,
    @SerializedName("tobaccoUse")
    val tobaccoUse: String?,
    @SerializedName("weight")
    val weight: Int?,
    @SerializedName("weightUnit")
    val weightUnit: String?,
    @SerializedName("bmi")
    val bmi : Double,
    @SerializedName("dateOfBirth")
    val dateOfBirth : String
) : Parcelable

fun ProfileResponse.toDto() : UserProfile {
    return UserProfile(countryCode,email, exerciseLevel, firstName, gender, height, heightUnit, id, lastName, phoneNumber, smoking, tobaccoUse, weight, weightUnit, bmi, dateOfBirth)
}

