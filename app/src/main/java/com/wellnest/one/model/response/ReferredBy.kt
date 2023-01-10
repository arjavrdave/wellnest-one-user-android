package com.wellnest.one.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class ReferredBy(
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("isPending")
    val isPending: Boolean?,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("qualification")
    val qualification: String?
) : Parcelable