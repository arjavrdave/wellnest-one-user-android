package com.wellnest.one.ui.recording

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rc.wellnestmodule.models.AzureToken
import com.wellnest.one.data.remote.definition.RecordingRepository
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.request.AddRecordingRequest
import com.wellnest.one.model.request.LinkMemberRequest
import com.wellnest.one.model.response.AddRecordingResponse
import com.wellnest.one.model.response.EcgRecordingResponse
import com.wellnest.one.model.response.GetRecordingResponse
import com.wellnest.one.model.response.SasToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Hussain on 23/11/22.
 */
@HiltViewModel
class RecordingViewModel @Inject constructor(private val recordingRepository: RecordingRepository) :
    ViewModel() {

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    private val _addRecordingSuccess = MutableLiveData<AddRecordingResponse>()
    val addRecordingSuccess: LiveData<AddRecordingResponse> get() = _addRecordingSuccess

    private val _ecgUploadToken = MutableLiveData<SasToken>()
    val ecgUploadToken: LiveData<SasToken> get() = _ecgUploadToken

    private val _ecgReadToken = MutableLiveData<SasToken>()
    val ecgReadToken: LiveData<SasToken> get() = _ecgReadToken

    private val _ecgRecording = MutableLiveData<EcgRecordingResponse>()
    val ecgRecording: LiveData<EcgRecordingResponse> get() = _ecgRecording

    private val _linkSuccess = MutableLiveData<Boolean>()
    val linkSuccess: LiveData<Boolean> get() = _linkSuccess

    private val _recordings = MutableLiveData<List<GetRecordingResponse>>()
    val recordings: LiveData<List<GetRecordingResponse>> get() = _recordings

    private val _readTokenUser = MutableLiveData<AzureToken>()
    val readTokenUser: LiveData<AzureToken> get() = _readTokenUser

    private val _sendForFeedbackSuccess = MutableLiveData<Boolean>()
    val sendForFeedbackSuccess: LiveData<Boolean> get() = _sendForFeedbackSuccess

    private val _readSignatureToken = MutableLiveData<SasToken>()
    val readSignatureToken: LiveData<SasToken> get() = _readSignatureToken

    fun addRecording(addRecordingRequest: AddRecordingRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = recordingRepository.addRecording(addRecordingRequest)
            when (result) {
                is ApiResult.Success -> {
                    _addRecordingSuccess.postValue(result.data!!)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getUploadTokenForEcg(filename: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = recordingRepository.getUploadTokenForEcg(filename)
            when (result) {
                is ApiResult.Success -> {
                    _ecgUploadToken.postValue(result.data!!)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getReadTokenForEcg(filename: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = recordingRepository.getReadTokenForEcg(filename)
            when (result) {
                is ApiResult.Success -> {
                    _ecgReadToken.postValue(result.data!!)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getEcgRecordingForId(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = recordingRepository.getEcgRecordingForId(id)
            when (result) {
                is ApiResult.Success -> {
                    _ecgRecording.postValue(result.data!!)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun linkRecording(ecgRecordingId: Int, member: LinkMemberRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = recordingRepository.linkFamilyMember(ecgRecordingId, member)
            when (result) {
                is ApiResult.Success -> {
                    _linkSuccess.postValue(true)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getRecordings(patientName: String? = null, take: Int? = 30, skip: Int? = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = recordingRepository.getRecording(patientName, take, skip)
            Log.e(
                "PASSDATA::::",
                "patientName:::" + patientName + "\n" + "take:::" + take + "\n" + "skip:::" + skip
            )
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

    fun getReadTokenForUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = recordingRepository.getReadTokenForUser()
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

    fun sendForFeedback(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = recordingRepository.sendForFeedback(id)
            when (result) {
                is ApiResult.Success -> {
                    _sendForFeedbackSuccess.postValue(true)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getReadTokenForSignature(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = recordingRepository.getReadTokenForSignature(id)
            when (result) {
                is ApiResult.Success -> {
                    result.data?.let {
                        _readSignatureToken.postValue(it)
                    }
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }


}