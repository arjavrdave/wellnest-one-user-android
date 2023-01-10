package com.wellnest.one.ui.recording.pair

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wellnest.one.data.remote.definition.DeviceRepository
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.ECGPrivateKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(private val deviceRepository : DeviceRepository) : ViewModel() {


    private val errorMsg = MutableLiveData<String>()
    val error: LiveData<String> get() = errorMsg

    private val privateKeyData = MutableLiveData<ECGPrivateKey?>()
    val privateKey: LiveData<ECGPrivateKey?> get() = privateKeyData

    private val verificationData = MutableLiveData<Boolean>()
    val verifySuccess: LiveData<Boolean> get() = verificationData

    fun getPrivateKey(deviceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = deviceRepository.getDevicePrivateKey(deviceId)
            when (result) {
                is ApiResult.Success -> {
                    privateKeyData.postValue(result.data)
                }
                is ApiResult.Error -> {
                    errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

}
