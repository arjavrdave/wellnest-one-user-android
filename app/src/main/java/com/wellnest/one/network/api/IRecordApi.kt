package com.wellnest.one.network.api

import com.rc.wellnestmodule.models.AzureToken
import com.wellnest.one.model.request.AddRecordingRequest
import com.wellnest.one.model.request.LinkMemberRequest
import com.wellnest.one.model.response.AddRecordingResponse
import com.wellnest.one.model.response.EcgRecordingResponse
import com.wellnest.one.model.response.GetRecordingResponse
import com.wellnest.one.model.response.SasToken
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by Hussain on 23/11/22.
 */
interface IRecordApi {

    @POST("ECGRecording")
    suspend fun addRecording(@Body recording: AddRecordingRequest): Response<AddRecordingResponse>

    @GET("ECGRecording/{ECGRecordingId}")
    suspend fun getRecordingForId(@Path("ECGRecordingId") recordingId: Int): Response<EcgRecordingResponse>

    @GET("Storage/GetUploadTokenForECG/{Id}")
    suspend fun getUploadTokenForECG(@Path("Id") id: String): Response<SasToken>

    @GET("Storage/GetReadTokenForECG/{Id}")
    suspend fun getReadTokenForECG(@Path("Id") id: String): Response<SasToken>

    @PUT("Patient/LinkFamilyMember/{ECGRecordingId}")
    suspend fun linkFamilyMember(
        @Path("ECGRecordingId") recordingId: Int,
        @Body member: LinkMemberRequest
    ): Response<Void>

    @GET("ECGRecording")
    suspend fun getRecordings(
        @Query("PatientName") patientName: String?,
        @Query("Take") take: Int?,
        @Query("Skip") skip: Int?
    ): Response<List<GetRecordingResponse>>

    @GET("Storage/GetReadTokenForOneUser")
    suspend fun getReadTokenForUser(): Response<AzureToken>

    @POST("ECGRecording/{ECGRecordingId}/SendForFeedback")
    suspend fun sendForFeedback(@Path("ECGRecordingId") id : Int, @Body emptybody : List<Int>) : Response<Void>

    @GET("Storage/GetReadTokenForSignature/{Id}")
    suspend fun getReadTokenForSignature(@Path("Id")id:Int): Response<SasToken>

}