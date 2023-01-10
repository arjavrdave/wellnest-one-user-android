package com.wellnest.one.data.remote.implementation

import com.wellnest.one.data.remote.BaseRepository
import com.wellnest.one.data.remote.definition.ProfileRepository
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.request.EditBioRequest
import com.wellnest.one.model.request.LogoutRequest
import com.wellnest.one.model.request.UpdateMedHistoryRequest
import com.wellnest.one.model.request.UserProfileRequest
import com.wellnest.one.model.response.MedicalHistoryResponse
import com.wellnest.one.model.response.ProfileResponse
import com.wellnest.one.model.response.SasToken
import com.wellnest.one.network.api.IProfileApi

/**
 * Created by Hussain on 10/11/22.
 */
class ProfileRepositoryImpl(private val iProfileApi: IProfileApi) : BaseRepository() ,
    ProfileRepository {

    override suspend fun addProfile(user: UserProfileRequest): ApiResult<Void> {
        return safeApiCall { iProfileApi.addProfile(user) }
    }

    override suspend fun getProfile(): ApiResult<ProfileResponse> {
        return safeApiCall { iProfileApi.getAccount() }
    }

    override suspend fun editBio(editBioRequest: EditBioRequest): ApiResult<Void> {
        return safeApiCall { iProfileApi.editBio(editBioRequest) }
    }

    override suspend fun updateMedicalHistory(patientId : Int, updateMedHistoryRequest: UpdateMedHistoryRequest): ApiResult<Void> {
        return safeApiCall { iProfileApi.updateMedicalHistory(patientId, updateMedHistoryRequest) }
    }

    override suspend fun getMedicalHistory(): ApiResult<MedicalHistoryResponse> {
        return safeApiCall { iProfileApi.getMedicalHistory() }
    }

    override suspend fun getUploadTokenForUser(): ApiResult<SasToken> {
        return safeApiCall { iProfileApi.getUploadTokenForUser() }
    }

    override suspend fun getReadTokenForUser(): ApiResult<SasToken> {
        return safeApiCall { iProfileApi.getReadTokenForUser() }
    }

    override suspend fun logout(logoutRequest: LogoutRequest): ApiResult<Void> {
        return safeApiCall { iProfileApi.logoutUser(logoutRequest) }
    }

}