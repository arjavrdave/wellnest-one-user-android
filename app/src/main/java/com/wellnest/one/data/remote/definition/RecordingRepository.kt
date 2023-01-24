package com.wellnest.one.data.remote.definition

import com.rc.wellnestmodule.models.AzureToken
import com.wellnest.one.model.ApiResult
import com.wellnest.one.model.request.AddRecordingRequest
import com.wellnest.one.model.request.LinkMemberRequest
import com.wellnest.one.model.response.AddRecordingResponse
import com.wellnest.one.model.response.EcgRecordingResponse
import com.wellnest.one.model.response.GetRecordingResponse
import com.wellnest.one.model.response.SasToken

/**
 * Created by Hussain on 23/11/22.
 */
interface RecordingRepository {

    suspend fun getUploadTokenForEcg(filename : String) : ApiResult<SasToken>

    suspend fun getReadTokenForEcg(filename: String) : ApiResult<SasToken>

    suspend fun getEcgRecordingForId(id : Int) : ApiResult<EcgRecordingResponse>

    suspend fun addRecording(addRecordingRequest: AddRecordingRequest) : ApiResult<AddRecordingResponse>

    suspend fun linkFamilyMember(recordingId : Int,member : LinkMemberRequest) : ApiResult<Void>

    suspend fun getRecording(patientName : String?,take: Int?, skip: Int?) : ApiResult<List<GetRecordingResponse>>

    suspend fun getReadTokenForUser(): ApiResult<AzureToken>

    suspend fun sendForFeedback(id : Int) : ApiResult<Void>

    suspend fun getReadTokenForSignature(id : Int) : ApiResult<SasToken>

}