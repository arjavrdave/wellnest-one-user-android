package com.rc.wellnestmodule.interfaces

interface IRecordData {
    fun setRecordingData(recordingData: ByteArray):ArrayList<ArrayList<Double>>
    fun setUpDataForRecording(finalRecordingData:ArrayList<List<Byte>>):ArrayList<ArrayList<Double>>
    fun dataFromMSB(msb: Byte, data: Byte):Double
    fun dataFromLSB(lsb: Byte, data: Byte):Double
    fun getGraphList(chartData:List<ArrayList<Double>>,bpm : Int = 60,allData :Boolean = false,paperSpeed : Int = 25,longLead : Int = 1):ArrayList<ArrayList<Double>>
    fun encryptByteArray(array: ByteArray): String
    fun decryptByteArray(data: ByteArray, privateKey: String): ByteArray
    fun getLiveRecordedData(recordingData: ByteArray):ArrayList<ArrayList<Double>>
    fun smoothedZScore(y: List<Double>, lag: Int, threshold: Double, influence: Double): Triple<List<Int>, List<Double>, List<Double>>
}