package com.rc.wellnestmodule.utils

import java.util.ArrayList

/**
 * The type Recording data helper.
 */
internal class RecordingDataHelper private constructor() {

    /**
     * The Recording bytes data.
     */
    private var recordingBytesData: ArrayList<List<Byte>>? = null


    /**
     * Gets recording bytes data.
     *
     * @return the recording bytes data
     */
    fun getRecordingBytesData(): ArrayList<List<Byte>> {
        if (recordingBytesData == null) {
            recordingBytesData = ArrayList()
        }
        return recordingBytesData as ArrayList<List<Byte>>
    }

    /**
     * Sets recording bytes data.
     *
     * @param recordingBytesData the recording bytes data
     */
    fun setRecordingBytesData(recordingBytesData: ArrayList<List<Byte>>) {
        this.recordingBytesData = recordingBytesData
    }


    /**
     * Clear chart data.
     */
    fun clearChartData() {
        getRecordingBytesData().clear()
        if (recordingBytesData != null) {
            recordingBytesData = null
        }
    }

    companion object {
        /**
         * Gets instance.
         *
         * @return the instance
         */
        val instance = RecordingDataHelper()
    }
}
