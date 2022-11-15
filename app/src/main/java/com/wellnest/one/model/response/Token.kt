package com.wellnest.one.model.response

data class Token(
    val token: String?,
    val validity: String?,
    val refreshToken: String?,
    var isNewUser: Boolean?
)

