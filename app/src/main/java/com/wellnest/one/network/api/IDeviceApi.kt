package com.wellnest.one.network.api

import com.wellnest.one.model.ECGPrivateKey
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by Hussain on 16/11/22.
 */
interface IDeviceApi {

    @GET("Device/{UDID}")
    suspend fun getECGPrivateKey(@Path("UDID") deviceId:String): Response<ECGPrivateKey>

    @GET("Device/DeviceSerialNumber/{DeviceId}")
    suspend fun verifyDeviceId(@Path("DeviceId") deviceId:String): Response<Void>
}