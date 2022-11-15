package com.wellnest.one.data.remote

import com.wellnest.one.model.ApiResult
import org.json.JSONObject
import retrofit2.Response
import java.net.UnknownHostException

/**
 * Created by Hussain on 07/11/22.
 */
open class BaseRepository {

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> = safeApiResult(call)

    private suspend fun <T: Any> safeApiResult(call: suspend ()-> Response<T>) : ApiResult<T> {
        try {
            val response = call.invoke()
            if(response.code() in 200..299) return ApiResult.Success(
                (response.body() ?: "") as T
            )



            val jObjError = JSONObject(response.errorBody()!!.string())


            return ApiResult.Error(
                response.code(),
                jObjError.getString("text").toString()
            )
        } catch (e: Exception) {
            if(e is UnknownHostException)
                return ApiResult.Error(
                    500,
                    "Not connected to the internet."
                )


            return ApiResult.Error(
                500,
                "Something went wrong please try again."
            )
        }

    }
}