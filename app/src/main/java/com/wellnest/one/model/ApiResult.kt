package com.wellnest.one.model

/**
 * Created by Hussain on 07/11/22.
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data : T?) : ApiResult<T>()
    data class Error<out T>(val errorCode : Int, val errorMsg : String) : ApiResult<T>()
}
