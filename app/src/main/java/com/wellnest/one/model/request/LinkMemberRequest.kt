package com.wellnest.one.model.request


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class LinkMemberRequest(
    @SerializedName("age")
    val age: Int?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?
) : Parcelable