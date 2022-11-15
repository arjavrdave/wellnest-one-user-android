package com.wellnest.one.network

import android.content.SharedPreferences
import com.wellnest.one.BuildConfig
import com.wellnest.one.data.local.user_pref.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationInterceptor @Inject constructor(private val prefManager : PreferenceManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authRequest = authRequest(request)
        val response = chain.proceed(authRequest)
        when(response.code) {
            401 -> {}// Handle Expired Token
        }
        return response
    }

    private fun authRequest(request: Request): Request {
        val token = prefManager.getToken()
        val newRequest = request.newBuilder()
        newRequest.addHeader("apiKey", BuildConfig.apiKey)
        newRequest.addHeader("apiPassword", BuildConfig.apiPassword)

        if (token?.token != null) {
            newRequest.addHeader("Authorization", "Bearer ${token.token}")
        }

        return newRequest.build()
    }

}
