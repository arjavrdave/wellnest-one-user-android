package com.rc.wellnestmodule.utils

object RecordingDataHandler {

    val instance = RecordingDataHandler

    var recordingData:ArrayList<ArrayList<Double>>? = null
    get() = field
    set(value) {
        field = value
    }

}