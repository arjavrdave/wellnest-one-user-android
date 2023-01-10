package com.wellnest.one.ui.login_signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wellnest.one.data.remote.definition.AccountRepository
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.request.GetInTouchRequest
import com.wellnest.one.model.request.LoginRequest
import com.wellnest.one.model.request.ResendOtpRequest
import com.wellnest.one.model.request.VerifyOtpRequest
import com.wellnest.one.model.response.Token
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Hussain on 07/11/22.
 */

@HiltViewModel
class LoginViewModel @Inject constructor(private val accountRepository: AccountRepository) : ViewModel() {


    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess : LiveData<Boolean> get() = _loginSuccess

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg : LiveData<String> get() = _errorMsg

    private val _otpSuccess = MutableLiveData<Token?>()
    val otpSuccess : LiveData<Token?> get() = _otpSuccess

    private val _resendOtpSuccess = MutableLiveData<Boolean>()
    val resendOtpSuccess : LiveData<Boolean> get() = _resendOtpSuccess

    private val _getInTouchSuccess = MutableLiveData<Boolean>()
    val getInTouchSuccess : LiveData<Boolean> get() = _getInTouchSuccess

    fun login(countryCode : Int, phoneNumber : String) {
        val loginRequest = LoginRequest(countryCode,phoneNumber)
        viewModelScope.launch(Dispatchers.IO) {
            val result = accountRepository.loginAsUser(loginRequest)
            when(result) {
                is ApiResult.Success -> {
                    _loginSuccess.postValue(true)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue("${result.errorMsg}")
                }
            }
        }
    }

    fun verifyOtp(countryCode: Int, phoneNumber: String?, code: String, fcmToken: String) {
        val verifyOtpRequest = VerifyOtpRequest(countryCode,phoneNumber?:"",code,fcmToken)
        viewModelScope.launch(Dispatchers.IO) {
            val result = accountRepository.verifyOtp(verifyOtpRequest)
            when(result) {
                is ApiResult.Success -> {
                    _otpSuccess.postValue(result.data)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue("${result.errorMsg}")
                }
            }
        }
    }

    fun resendOtp(countryCode: Int,phoneNumber: String) {
        val loginRequest = ResendOtpRequest(countryCode,phoneNumber)
        viewModelScope.launch(Dispatchers.IO) {
            val result = accountRepository.resendOtp(loginRequest)
            when(result) {
                is ApiResult.Success -> {
                    _resendOtpSuccess.postValue(true)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue("${result.errorMsg}")
                }
            }
        }
    }

    fun getInTouch(getInTouchRequest: GetInTouchRequest) {
        viewModelScope.launch {
            val result =  accountRepository.getInTouch(getInTouchRequest)
            when(result) {
                is ApiResult.Success -> {
                    _getInTouchSuccess.postValue(true)
                }
                is ApiResult.Error -> {
                    _errorMsg.postValue(result.errorMsg)
                }
            }
        }
    }
}