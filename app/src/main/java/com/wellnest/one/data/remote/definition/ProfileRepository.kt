package com.wellnest.one.data.remote.definition

import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.request.EditBioRequest
import com.wellnest.one.model.request.LogoutRequest
import com.wellnest.one.model.request.UpdateMedHistoryRequest
import com.wellnest.one.model.request.UserProfileRequest
import com.wellnest.one.model.response.MedicalHistoryResponse
import com.wellnest.one.model.response.ProfileResponse
import com.wellnest.one.model.response.SasToken

/**
 * Created by Hussain on 10/11/22.
 */
interface ProfileRepository {
    suspend fun addProfile(user : UserProfileRequest) : ApiResult<Void>
    suspend fun getProfile() : ApiResult<ProfileResponse>
    suspend fun editBio(editBioRequest: EditBioRequest) : ApiResult<Void>
    suspend fun updateMedicalHistory(patientId : Int,updateMedHistoryRequest: UpdateMedHistoryRequest) : ApiResult<Void>
    suspend fun getMedicalHistory() : ApiResult<MedicalHistoryResponse>
    suspend fun getUploadTokenForUser() : ApiResult<SasToken>
    suspend fun getReadTokenForUser() : ApiResult<SasToken>
    suspend fun logout(logoutRequest: LogoutRequest) : ApiResult<Void>
}