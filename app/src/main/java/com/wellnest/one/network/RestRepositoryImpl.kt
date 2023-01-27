package com.wellnest.one.network

import com.rc.wellnestmodule.models.AzureToken
import com.wellnest.one.data.remote.BaseRepository
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.response.GetRecordingResponse
import com.wellnest.one.ui.home.HomeRepository

class RestRepositoryImpl(private val iRecordingApi: RestApiRequests) : BaseRepository(),
    HomeRepository {
    override suspend fun getReadTokenForUser(): ApiResult<AzureToken> {
        return safeApiCall { iRecordingApi.getReadTokenForUser() }
    }

    override suspend fun getRecording(
        patientName: String?,
        take: Int?,
        skip: Int?
    ): ApiResult<List<GetRecordingResponse>> {
        return safeApiCall { iRecordingApi.getRecordings(patientName, take, skip) }
    }
}