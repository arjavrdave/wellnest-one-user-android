package com.wellnest.one.ui.home

import com.rc.wellnestmodule.models.AzureToken
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.response.GetRecordingResponse

interface HomeRepository  {

    suspend fun getReadTokenForUser(): ApiResult<AzureToken>

    suspend fun getRecording(
        patientName: String?,
        take: Int?,
        skip: Int?
    ): ApiResult<List<GetRecordingResponse>>

}