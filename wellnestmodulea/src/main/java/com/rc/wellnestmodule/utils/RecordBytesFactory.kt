package com.rc.wellnestmodule.utils

import com.rc.wellnestmodule.interfaces.IRecordData

class RecordBytesFactory {
    /**
     * TODO
     *
     * @return RecordingBytesHandler object
     */

    fun getRecordBytes():IRecordData{
        return RecordingBytesHandler()
    }
}