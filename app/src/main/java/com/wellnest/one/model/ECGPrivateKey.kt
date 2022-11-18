package com.wellnest.one.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Hussain on 16/11/22.
 */
data class ECGPrivateKey(
    @SerializedName("privateKey")
    val privateKey: String,
    @SerializedName("id")
    val id: Int
)