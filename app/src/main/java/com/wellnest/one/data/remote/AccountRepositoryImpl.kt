package com.wellnest.one.data.remote

import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.request.ResendOtpRequest
import com.wellnest.one.model.request.VerifyOtpRequest
import com.wellnest.one.model.request.LoginRequest
import com.wellnest.one.model.response.Token
import com.wellnest.one.network.api.IAccountApi

/**
 * Created by Hussain on 07/11/22.
 */

class AccountRepositoryImpl(private val iAccountApi: IAccountApi) : BaseRepository(), AccountRepository {
    override suspend fun loginAsUser(loginRequest: LoginRequest): ApiResult<Void> {
        return safeApiCall {
            iAccountApi.loginUser(loginRequest)
        }
    }

    override suspend fun verifyOtp(verifyOtpRequest: VerifyOtpRequest): ApiResult<Token> {
        return safeApiCall(call = {
            iAccountApi.verifyOtpForUser(verifyOtpRequest)
        })
    }

    override suspend fun resendOtp(resendOtpRequest: ResendOtpRequest): ApiResult<Void> {
        return safeApiCall {
            iAccountApi.resendOtp(resendOtpRequest)
        }
    }


}