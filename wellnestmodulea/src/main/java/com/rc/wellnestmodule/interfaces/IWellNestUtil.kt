package com.rc.wellnestmodule.interfaces

import android.app.Activity
import android.hardware.usb.UsbDevice
import java.io.File

interface IWellNestUtil {
    fun start(activity: Activity, requestCode: Int)
    fun startBleService(activity: Activity, iWellnestGraph: IWellnestGraph, iWellNestData: IWellNestData, iSendMessageToEcgDevice: ISendMessageToEcgDevice, iWellnestUsbData: IWellnestUsbData)
    fun connectBle(address: String)
    fun startBleConnection()
    fun deviceConnect()
    fun disConnectBleService()
    fun prepareRecording()
    fun onResume()
    fun onPause()
    fun onDestroy()
    fun setEcgSetup(setup:String)
    fun displayGraphs()
    fun clearData()
    fun obtainGraphList(byteArray: ByteArray)
    fun obtainGraphByteArray():ByteArray
    fun uploadFileViaWorkManager(byteArray:ByteArray,azureHOST:String,random: String,sasToken:String ,ecgRecordings: String):Boolean
    fun uploadFile(azureHOST:String,random: String,sasToken:String ,ecgRecordings: String, iECGRecordingUpload: IECGRecordingUpload)
    fun getFile(random: String, azureHOST: String, sasToken: String, ecgRecordings: String, iWellnestEcgFileListener: IWellnestEcgFileListener)
    fun setUpUsbConnection(device: UsbDevice)
    fun setUpUsbCallback()
    fun startUsbRecording(isUsbRecording: Boolean)
    fun stopUsbRecording(processRecording: Boolean)
    fun getBatteryStatusUsb()
    fun getDidUsb()
    fun startBluetoothLiveRecording()
    fun stopBluetoothLiveRecording(processRecording : Boolean)
    fun sendTstEndUsb()
    fun setUsbDataDelegate(iWellnestUsbData: IWellnestUsbData)
    fun stopProcessing()
    fun uploadPdfFile(source: File, uuid: String, containerName: String, sasToken: String, azureHOST: String)
    fun getRecordingData() : ByteArray
}