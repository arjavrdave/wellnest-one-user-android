package com.wellnest.one.data.remote

import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.request.LoginRequest
import com.wellnest.one.model.request.ResendOtpRequest
import com.wellnest.one.model.request.VerifyOtpRequest
import com.wellnest.one.model.response.Token

/**
 * Created by Hussain on 07/11/22.
 */
interface AccountRepository {
    suspend fun loginAsUser(loginRequest: LoginRequest): ApiResult<Void>
    suspend fun verifyOtp(verifyOtpRequest: VerifyOtpRequest) : ApiResult<Token>
    suspend fun resendOtp(resendOtpRequest: ResendOtpRequest) : ApiResult<Void>
}