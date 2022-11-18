package com.rc.wellnestmodule.interfaces

import java.lang.Exception

interface IWellnestEcgFileListener {
    fun onSuccess(recordingData: ArrayList<List<Byte>>)
    fun onFailure(exception: Exception)
}