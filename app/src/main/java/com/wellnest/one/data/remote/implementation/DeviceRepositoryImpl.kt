package com.wellnest.one.data.remote.implementation

import com.wellnest.one.data.remote.BaseRepository
import com.wellnest.one.data.remote.definition.DeviceRepository
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.ECGPrivateKey
import com.wellnest.one.network.api.IDeviceApi

/**
 * Created by Hussain on 16/11/22.
 */
class DeviceRepositoryImpl(private val iDeviceApi: IDeviceApi) :  BaseRepository(),
    DeviceRepository {

    override suspend fun getDevicePrivateKey(deviceId: String): ApiResult<ECGPrivateKey> {
        return safeApiCall { iDeviceApi.getECGPrivateKey(deviceId) }
    }
}