package com.rc.wellnestmodule.interfaces

interface IWellNestData {
    fun onDeviceConnected()
    fun onDeviceDisconnected()
    fun onRecordingCompleted(graphList: ArrayList<ArrayList<Double>>)
}