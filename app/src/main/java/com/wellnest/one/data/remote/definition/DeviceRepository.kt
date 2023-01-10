package com.wellnest.one.data.remote.definition

import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.ECGPrivateKey

/**
 * Created by Hussain on 16/11/22.
 */
interface DeviceRepository {

    suspend fun getDevicePrivateKey(deviceId : String ) : ApiResult<ECGPrivateKey>

}