package com.wellnest.one.model.request


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class GetInTouchRequest(
    @SerializedName("countryCode")
    val countryCode: Int?,
    @SerializedName("emailAddress")
    val emailAddress: String?,
    @SerializedName("fullName")
    val fullName: String?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("subject")
    val subject: String?,
    @SerializedName("source")
    val source: String? = "Android"
) : Parcelable