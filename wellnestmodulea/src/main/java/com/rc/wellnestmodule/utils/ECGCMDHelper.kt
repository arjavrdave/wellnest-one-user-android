package com.rc.wellnestmodule.utils

/**
 * The type Ecgcmd helper.
 */
class ECGCMDHelper private constructor() {


    /**
     * Ecg getdidcmd string.
     *
     * @return the string
     */
    fun ecgGETDIDCMD(): String {
        return "+GETDID\r\n"
    }

    /**
     * Ecg test cmd string.
     *
     * @return the string
     */
    fun ecgTestCMD(): String {
        return "+TSTEND\r\n"
    }

    /**
     * Ecg record cmd string.
     *
     * @return the string
     */
    fun ecgRecordCMD(): String {
        return "+RECORD\r\n"
    }

    /**
     * Ecg stream 1 cmd string.
     *
     * @return the string
     */
    fun ecgStream1CMD(): String {
        return "+STREAM=1\r\n"
    }

    /**
     * Ecg stream 10 cmd string.
     *
     * @return the string
     */
    fun ecgStream10CMD(): String {
        return "+STREAM=10\r\n"
    }

    /**
     * Ecg stream 40 cmd string.
     *
     * @return the string
     */
    fun ecgStream40CMD(): String {
        return "+STREAM=40\r\n"
    }

    /**
     * Get auth string.
     *
     * @return the string
     */
    fun GETAuth(): String {
        return "+GETAUTH\r\n"
    }

    /**
     * Get stat string.
     *
     * @return the string
     */
    fun GETStat(): String {
        return "+GETSTAT\r\n"
    }

    /**
     * Get dat string.
     *
     * @param start      the start
     * @param sampleSize the sample size
     * @param delay      the delay
     * @return the string
     */
    fun getDat(start: Int, sampleSize: Int, delay: Int): String {
        return "+GETDAT=$start,$sampleSize,$delay\r\n"
        //        return "+GETDAT("+start+", "+sampleSize+", "+delay+")";
    }

    companion object {
        /**
         * Gets instance.
         *
         * @return the instance
         */
        val instance = ECGCMDHelper()
    }
}
