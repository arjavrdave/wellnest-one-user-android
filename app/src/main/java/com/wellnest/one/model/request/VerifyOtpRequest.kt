package com.wellnest.one.model.request

data class VerifyOtpRequest(
    val countryCode: Int,
    val phoneNumber: String,
    val code: String,
    val fcmToken: String
)