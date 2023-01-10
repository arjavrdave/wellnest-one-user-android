package com.wellnest.one.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class ReportedBy(
    @SerializedName("countryCode")
    val countryCode: Int?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("qualification")
    val qualification: String?
) : Parcelable {
    override fun toString(): String {
        return "$firstName $lastName"
    }
}