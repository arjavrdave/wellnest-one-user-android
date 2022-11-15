package com.wellnest.one.model.request

data class ResendOtpRequest(
    val countryCode: Int,
    val phoneNumber: String
)
