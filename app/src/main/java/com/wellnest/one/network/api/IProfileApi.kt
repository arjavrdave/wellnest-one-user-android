package com.wellnest.one.network.api

import com.wellnest.one.model.request.EditBioRequest
import com.wellnest.one.model.request.LogoutRequest
import com.wellnest.one.model.request.UpdateMedHistoryRequest
import com.wellnest.one.model.request.UserProfileRequest
import com.wellnest.one.model.response.MedicalHistoryResponse
import com.wellnest.one.model.response.ProfileResponse
import com.wellnest.one.model.response.SasToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Created by Hussain on 10/11/22.
 */
interface IProfileApi {

    @PUT("Patient/Profile")
    suspend fun addProfile(@Body user : UserProfileRequest) : Response<Void>

    @GET("Account")
    suspend fun getAccount() : Response<ProfileResponse>

    @PUT("Account/EditBio")
    suspend fun editBio(@Body editBioRequest: EditBioRequest) : Response<Void>

    @PUT("Patient/UpdateMedicalHistory/{PatientId}")
    suspend fun updateMedicalHistory(@Path("PatientId") patientId : Int,@Body updateMedHistory : UpdateMedHistoryRequest) : Response<Void>

    @GET("Patient/Medicalhistory")
    suspend fun getMedicalHistory() : Response<MedicalHistoryResponse>

    @GET("Storage/GetUploadTokenForUser")
    suspend fun getUploadTokenForUser() : Response<SasToken>

    @GET("Storage/GetReadTokenForUser")
    suspend fun getReadTokenForUser() : Response<SasToken>

    @POST("Account/LogoutForUser")
    suspend fun logoutUser(@Body logoutRequest : LogoutRequest) : Response<Void>
}