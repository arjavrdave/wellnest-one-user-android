package com.wellnest.one.data.remote.implementation

import com.rc.wellnestmodule.models.AzureToken
import com.wellnest.one.data.remote.BaseRepository
import com.wellnest.one.data.remote.definition.RecordingRepository
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.request.AddRecordingRequest
import com.wellnest.one.model.request.LinkMemberRequest
import com.wellnest.one.model.response.AddRecordingResponse
import com.wellnest.one.model.response.EcgRecordingResponse
import com.wellnest.one.model.response.GetRecordingResponse
import com.wellnest.one.model.response.SasToken
import com.wellnest.one.network.api.IRecordApi

/**
 * Created by Hussain on 23/11/22.
 */
class RecordingRepositoryImpl(private val iRecordingApi : IRecordApi) : BaseRepository(), RecordingRepository {
    override suspend fun getUploadTokenForEcg(filename: String): ApiResult<SasToken> {
        return safeApiCall { iRecordingApi.getUploadTokenForECG(filename) }
    }

    override suspend fun getReadTokenForEcg(filename: String): ApiResult<SasToken> {
        return safeApiCall { iRecordingApi.getReadTokenForECG(filename) }
    }

    override suspend fun getEcgRecordingForId(id: Int): ApiResult<EcgRecordingResponse> {
        return safeApiCall { iRecordingApi.getRecordingForId(id) }
    }

    override suspend fun addRecording(addRecordingRequest: AddRecordingRequest): ApiResult<AddRecordingResponse> {
        return safeApiCall { iRecordingApi.addRecording(addRecordingRequest) }
    }

    override suspend fun linkFamilyMember(recordingId: Int, member : LinkMemberRequest): ApiResult<Void> {
        return safeApiCall { iRecordingApi.linkFamilyMember(recordingId,member) }
    }

    override suspend fun getRecording(
        patientName: String?,
        take: Int?,
        skip: Int?
    ): ApiResult<List<GetRecordingResponse>> {
        return safeApiCall { iRecordingApi.getRecordings(patientName, take, skip) }
    }

    override suspend fun getReadTokenForUser(): ApiResult<AzureToken> {
        return safeApiCall { iRecordingApi.getReadTokenForUser() }
    }

    override suspend fun sendForFeedback(id: Int): ApiResult<Void> {
        return safeApiCall { iRecordingApi.sendForFeedback(id, listOf(0)) }
    }

    override suspend fun getReadTokenForSignature(id: Int): ApiResult<SasToken> {
        return safeApiCall { iRecordingApi.getReadTokenForSignature(id) }
    }
}