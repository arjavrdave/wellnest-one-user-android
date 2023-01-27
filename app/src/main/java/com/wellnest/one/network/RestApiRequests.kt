package com.wellnest.one.network

import com.rc.wellnestmodule.models.AzureToken
import com.wellnest.one.model.response.GetRecordingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RestApiRequests {

    @GET("Storage/GetReadTokenForOneUser")
    suspend fun getReadTokenForUser(): Response<AzureToken>

    @GET("ECGRecording")
    suspend fun getRecordings(
        @Query("PatientName") patientName: String?,
        @Query("Take") take: Int?,
        @Query("Skip") skip: Int?
    ): Response<List<GetRecordingResponse>>
}