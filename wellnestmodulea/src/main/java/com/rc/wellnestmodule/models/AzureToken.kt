package com.rc.wellnestmodule.models


import com.google.gson.annotations.SerializedName

data class AzureToken(
    @SerializedName("sasToken")
    val sasToken: String
)