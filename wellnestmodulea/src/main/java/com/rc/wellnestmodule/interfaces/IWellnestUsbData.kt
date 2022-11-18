package com.rc.wellnestmodule.interfaces

interface IWellnestUsbData {
    fun setBatteryStatus(batteryLevel: String)
    fun plotGraphPoint(list: ArrayList<ArrayList<Double>>)
    fun setDidUsb(id : String)
    fun addRawData(data : ByteArray)
}