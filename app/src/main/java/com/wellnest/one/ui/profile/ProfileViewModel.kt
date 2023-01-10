package com.wellnest.one.ui.profile

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wellnest.one.data.remote.definition.ProfileRepository
import com.wellnest.one.dto.UserProfile
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.request.EditBioRequest
import com.wellnest.one.model.request.LogoutRequest
import com.wellnest.one.model.request.UpdateMedHistoryRequest
import com.wellnest.one.model.request.UserProfileRequest
import com.wellnest.one.model.response.MedicalHistoryResponse
import com.wellnest.one.model.response.SasToken
import com.wellnest.one.model.response.toDto
import com.wellnest.one.utils.Constants
import com.wellnest.one.utils.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Created by Hussain on 10/11/22.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(private val profileRepository: ProfileRepository) :
    ViewModel() {

    private val _profileSuccess = MutableLiveData<Boolean>()
    val profileSuccess: LiveData<Boolean> get() = _profileSuccess

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    private val _profileData = MutableLiveData<UserProfile>()
    val profileData: LiveData<UserProfile> get() = _profileData

    private val _editBioSuccess = MutableLiveData<Boolean>()
    val editBioSuccess: LiveData<Boolean> get() = _editBioSuccess

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> get() = _updateSuccess

    private val _medicalHistoryData = MutableLiveData<MedicalHistoryResponse>()
    val medicalHistoryData: LiveData<MedicalHistoryResponse> get() = _medicalHistoryData

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> get() = _logoutSuccess

    private val _uploadImageToken = MutableLiveData<SasToken>()
    val uploadImageToken: LiveData<SasToken> get() = _uploadImageToken

    private val _readImageToken = MutableLiveData<SasToken>()
    val readImageToken: LiveData<SasToken> get() = _readImageToken

    private val _profileImgUploadSuccess = MutableLiveData<Boolean>()
    val profileImgUploadSuccess: LiveData<Boolean> get() = _profileImgUploadSuccess

    private val _userProfileImage = MutableLiveData<Bitmap?>()
    val userProfileImage: LiveData<Bitmap?> get() = _userProfileImage

    fun addProfile(profile: UserProfileRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = profileRepository.addProfile(profile)
            when (result) {
                is ApiResult.Success -> {
                    _profileSuccess.postValue(true)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = profileRepository.getProfile()
            when (result) {
                is ApiResult.Success -> {
                    result.data?.let {
                        _profileData.postValue(it.toDto())
                    }
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun editBio(editBioRequest: EditBioRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = profileRepository.editBio(editBioRequest)
            when (result) {
                is ApiResult.Success -> {
                    _editBioSuccess.postValue(true)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun updateMedicalHistory(patientId: Int, updateMedicalRequest: UpdateMedHistoryRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = profileRepository.updateMedicalHistory(patientId, updateMedicalRequest)
            when (result) {
                is ApiResult.Success -> {
                    _updateSuccess.postValue(true)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getMedicalHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = profileRepository.getMedicalHistory()
            when (result) {
                is ApiResult.Success -> {
                    result.data?.let {
                        _medicalHistoryData.postValue(it)
                    }
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun logout(logoutRequest: LogoutRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = profileRepository.logout(logoutRequest)
            when (result) {
                is ApiResult.Success -> {
                    _logoutSuccess.postValue(true)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getUploadImageSasToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = profileRepository.getUploadTokenForUser()
            when (result) {
                is ApiResult.Success -> {
                    result.data?.let {
                        _uploadImageToken.postValue(it)
                    }
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun getReadImageSasToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = profileRepository.getReadTokenForUser()
            when (result) {
                is ApiResult.Success -> {
                    result.data?.let {
                        _readImageToken.postValue(it)
                    }
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }

    fun uploadImage(profileBitmap: Bitmap, sasToken: SasToken?, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val outputStream = ByteArrayOutputStream()
            val compressed = profileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            if (compressed) {
                Util.UploadImage(
                    ByteArrayInputStream(outputStream.toByteArray()),
                    profileBitmap.byteCount,
                    Constants.PROFILE_IMAGES,
                    id,
                    sasToken?.sasToken
                )
                _profileImgUploadSuccess.postValue(true)
            } else {
                _errorMsg.postValue("Failed to compress bitmap")
            }
        }
    }

    fun getProfileImage(userId: String, containerName: String, sasToken: SasToken) {
        viewModelScope.launch(Dispatchers.IO) {
            val image = Util.GetImage(userId, containerName, sasToken.sasToken)
            _userProfileImage.postValue(image)
        }
    }

}