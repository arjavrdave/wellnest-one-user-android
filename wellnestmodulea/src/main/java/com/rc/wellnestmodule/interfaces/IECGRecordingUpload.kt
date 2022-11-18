package com.rc.wellnestmodule.interfaces

interface IECGRecordingUpload {
    fun onSuccess()
    fun onFailure(message: String?)
}