package com.rc.wellnestmodule.utils

import com.google.gson.Gson

/**
 * A singleton utility class to provide different utilities to the project
 */
class Util private constructor() {

    private var gson: Gson? = null

    /**
     * Gets a gson instance so that a new instance is not needed to be created everytime.
     *
     * @return a new Gson instance
     */
    val gsonInstance: Gson
        get() {
            if (gson == null) {
                gson = Gson()
            }
            return gson!!
        }

    companion object {
        /**
         * Get instance util.
         *
         * @return the util
         */
        val instance = Util()
    }

    fun getLeadsArray(setup: String): ArrayList<String> {
        var leads: ArrayList<String> = arrayListOf()

        when (setup) {
            "standard" -> {
                leads = arrayListOf(
                    "L1",
                    "L2",
                    "L3",
                    "aVR",
                    "aVL",
                    "aVF",
                    "V1",
                    "V2",
                    "V3",
                    "V4",
                    "V5",
                    "V6"
                )
            }

            "right side" -> {
                leads = arrayListOf(
                    "L1",
                    "L2",
                    "L3",
                    "aVR",
                    "aVL",
                    "aVF",
                    "V1R",
                    "V2R",
                    "V3R",
                    "V4R",
                    "V5R",
                    "V6R"
                )
            }

            "posterior" -> {
                leads = arrayListOf(
                    "L1",
                    "L2",
                    "L3",
                    "aVR",
                    "aVL",
                    "aVF",
                    "V1",
                    "V2",
                    "V3",
                    "V7",
                    "V8",
                    "V9"
                )
            }
        }

        return leads
    }

}
