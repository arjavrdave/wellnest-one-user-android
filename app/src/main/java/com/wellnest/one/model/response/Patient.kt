package com.wellnest.one.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Patient(
    @SerializedName("countryCode")
    val countryCode: Int?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("patientDateOfBirth")
    val patientDateOfBirth: String?,
    @SerializedName("patientFirstName")
    val patientFirstName: String?,
    @SerializedName("patientGender")
    val patientGender: String?,
    @SerializedName("patientLastName")
    val patientLastName: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?
) : Parcelable {

    fun fullName() : String {
        return "${patientFirstName ?: ""} ${patientLastName ?: ""}"
    }
}