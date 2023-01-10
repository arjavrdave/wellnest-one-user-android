package com.wellnest.one.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Organisation(
    @SerializedName("city")
    val city: String?,
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
    @SerializedName("state")
    val state: String?,
    @SerializedName("street")
    val street: String?
) : Parcelable