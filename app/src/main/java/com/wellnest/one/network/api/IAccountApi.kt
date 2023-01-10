package com.wellnest.one.network.api

import com.wellnest.one.model.request.GetInTouchRequest
import com.wellnest.one.model.request.ResendOtpRequest
import com.wellnest.one.model.request.VerifyOtpRequest
import com.wellnest.one.model.request.LoginRequest
import com.wellnest.one.model.response.Token
import com.wellnest.one.ui.getintouch.GetInTouchActivity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Created by Hussain on 07/11/22.
 */
interface IAccountApi {

    @POST("Account/LoginAsUser")
    suspend fun loginUser(@Body loginRequest : LoginRequest) : Response<Void>

    @POST("Account/VerifyOtpForUser")
    suspend fun verifyOtpForUser(@Body verifyOtpRequest : VerifyOtpRequest) : Response<Token>

    @PUT("Account/ResendVerificationCode")
    suspend fun resendOtp(@Body resendOtpRequest : ResendOtpRequest) : Response<Void>

    @POST("Account/GetInTouch")
    suspend fun getInTouch(@Body getInTouchRequest: GetInTouchRequest) : Response<Void>

}