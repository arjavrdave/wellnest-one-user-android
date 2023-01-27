package com.wellnest.one.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rc.wellnestmodule.models.AzureToken
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.response.GetRecordingResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val homeRepository: HomeRepository) :
    ViewModel() {

    private val _readTokenUser = MutableLiveData<AzureToken>()
    val readTokenUser: LiveData<AzureToken> get() = _readTokenUser

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    private val _recordings = MutableLiveData<List<GetRecordingResponse>>()
    val recordings: LiveData<List<GetRecordingResponse>> get() = _recordings

    fun getReadTokenForUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = homeRepository.getReadTokenForUser()
            when (result) {
                is ApiResult.Success -> {
                    _readTokenUser.postValue(result.data!!)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getRecordings(patientName: String? = null, take: Int? = 30, skip: Int? = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = homeRepository.getRecording(patientName, take, skip)
            when (result) {
                is ApiResult.Success -> {
                    result.data?.let {
                        _recordings.postValue(it)
                    }
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

}