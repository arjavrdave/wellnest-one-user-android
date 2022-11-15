package com.wellnest.one.model.request

data class LoginRequest(
    val countryCode: Int,
    val phoneNumber: String
)