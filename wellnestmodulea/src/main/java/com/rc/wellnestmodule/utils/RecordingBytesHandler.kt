package com.rc.wellnestmodule.utils

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import com.rc.wellnestmodule.interfaces.IRecordData
import java.lang.IndexOutOfBoundsException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.math.roundToInt
import org.apache.commons.math3.stat.descriptive.SummaryStatistics

class RecordingBytesHandler : IRecordData {
    private val TAG: String = "RecordingBytesHandler"
    private var x = Array(9) { DoubleArray(10) { 0.0 } }
    private var y = Array(9) { DoubleArray(10) { 0.0 } }
    private val a: Array<Double> = arrayOf(
        1.0, -1.5789478550001697, 1.0120725453024577, -0.22712139734776327
    )
    private val b: Array<Double> = arrayOf(
        0.025750411619315607, 0.07725123485794683, 0.07725123485794683, 0.025750411619315607
    )
    val byteArray = byteArrayOf(
        0x60.toByte(),
        0x3d.toByte(),
        0xeb.toByte(),
        0x10.toByte(),
        0x15.toByte(),
        0xca.toByte(),
        0x71.toByte(),
        0xbe.toByte(),
        0x2b.toByte(),
        0x73.toByte(),
        0xae.toByte(),
        0xf0.toByte(),
        0x85.toByte(),
        0x7d.toByte(),
        0x77.toByte(),
        0x81.toByte(),
        0x1f.toByte(),
        0x35.toByte(),
        0x2c.toByte(),
        0x07.toByte(),
        0x3b.toByte(),
        0x61.toByte(),
        0x08.toByte(),
        0xd7.toByte(),
        0x2d.toByte(),
        0x98.toByte(),
        0x10.toByte(),
        0xa3.toByte(),
        0x09.toByte(),
        0x14.toByte(),
        0xdf.toByte(),
        0xf4.toByte()
    )

    /**
     * TODO
     *
     * @param recordingData
     * @return
     */
    override fun setRecordingData(recordingData: ByteArray): ArrayList<ArrayList<Double>> {
        val finalRecordingData = ArrayList<List<Byte>>()

        var byteIndex = 0
        for (i in 0..recordingData.size - 2) {
            if (byteIndex <= recordingData.size && byteIndex + 15 <= recordingData.size - 1) {
                if (byteIndex > i) continue
                if (recordingData[byteIndex].toInt() == -70) {
                    try {
                        finalRecordingData.add(
                            recordingData.slice(
                                IntRange(
                                    byteIndex,
                                    byteIndex + 15
                                )
                            )
                        )
                        byteIndex += 16
                    } catch (e: IndexOutOfBoundsException) {
                        return ArrayList()
                    }
                } else {
                    byteIndex += 1
                }
            } else {
                break
            }
        }

        RecordingDataHelper.instance.setRecordingBytesData(finalRecordingData)
        return setUpDataForRecording(finalRecordingData)
    }

    override fun getLiveRecordedData(recordingData: ByteArray): ArrayList<ArrayList<Double>> {
        val finalRecordingData = ArrayList<List<Byte>>()


        var byteIndex = 0
        Log.i("DataHandlerRunnable", "${recordingData.toHex()}")
        for (i in 0..recordingData.size - 1) {
            if (byteIndex <= recordingData.size && byteIndex + 15 <= recordingData.size - 1) {
                if (byteIndex > i) continue
                if (recordingData[byteIndex].toInt() == -70) {
                    finalRecordingData.add(recordingData.slice(IntRange(byteIndex, byteIndex + 15)))
                    byteIndex += 16
                } else {
                    byteIndex += 1
                }
            } else {
                break
            }
        }

//
//        val bytes = recordingData.asList()
//        finalRecordingData.add(bytes)
        return setUpDataForRecording(finalRecordingData)
    }

    /**
     * TODO
     *
     * @param finalRecordingData
     * @return
     */
    override fun setUpDataForRecording(finalRecordingData: ArrayList<List<Byte>>): ArrayList<ArrayList<Double>> {
        val chartData = ArrayList<ArrayList<Double>>()
        var n = 0

        try {
            for (reading in finalRecordingData) {
                val finalData = ArrayList<Double>()

                finalData.add(calculateXY(0, dataFromMSB(reading[11], reading[2]), n)) //L1
                finalData.add(calculateXY(1, dataFromLSB(reading[11], reading[3]), n)) //L2

                finalData.add(calculateXY(2, dataFromMSB(reading[12], reading[4]), n)) //L3
                finalData.add(calculateXY(3, dataFromLSB(reading[12], reading[5]), n)) //V1

                finalData.add(calculateXY(4, dataFromMSB(reading[13], reading[6]), n)) //V2
                finalData.add(calculateXY(5, dataFromLSB(reading[13], reading[7]), n)) //V3

                finalData.add(calculateXY(6, dataFromMSB(reading[14], reading[8]), n)) //V4
                finalData.add(calculateXY(7, dataFromLSB(reading[14], reading[9]), n)) //V5

                finalData.add(calculateXY(8, dataFromMSB(reading[15], reading[10]), n)) //V6

                val dataDouble = finalData
                val lead10 = (dataDouble[0] - dataDouble[2]) / 2
                val lead11 = (dataDouble[0] + dataDouble[1]) / -2
                val lead12 = (dataDouble[1] + dataDouble[2]) / 2
                dataDouble.add(lead10) // Avl
                dataDouble.add(lead11) // Avr
                dataDouble.add(lead12) // Avf

                chartData.add(dataDouble)

                n = (n + 1) % 10

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return chartData
    }

    /**
     * TODO
     *
     * @param msb
     * @param data
     * @return
     */
    override fun dataFromMSB(msb: Byte, data: Byte): Double {
        val temp1 = ((msb.toUByte().toInt() and 0xff) and 240) shr 4
        val value = data.toUByte().toDouble() + (256 * temp1.toDouble())
        return (value - 2048) / 124.1
    }


    /**
     * @param lsb
     * @param data
     * @return
     */
    override fun dataFromLSB(lsb: Byte, data: Byte): Double {
        val temp1 = ((lsb.toUByte().toInt() and 0xff) and 15)
        val value = data.toUByte().toDouble() + (256 * temp1.toDouble())
        return (value - 2048) / 124.1
    }

    fun calculateXY(index: Int, value: Double, n: Int): Double {
        x[index][n] = value
        val m = 10
        val temp: Double =
            b[0] * x[index][n] + b[1] * x[index][(n - 1 + m) % m] + b[2] * x[index][(n - 2 + m) % m] + b[3] * x[index][(n - 3 + m) % m] - a[1] * y[index][(n - 1 + m) % m] - a[2] * y[index][(n - 2 + m) % m] - a[3] * y[index][(n - 3 + m) % m];
        y[index][n] = temp
        return temp
    }


    /**
     * @param chartData
     * @return
     */
    override fun getGraphList(
        chartData: List<ArrayList<Double>>,
        bpm: Int,
        allData: Boolean,
        paperSpeed: Int,
        longLead : Int
    ): ArrayList<ArrayList<Double>> {

        if (chartData.isEmpty()) return ArrayList()

        var data = ArrayList<ArrayList<Double>>()

        // no data or not enough points
        if (chartData.isEmpty() || chartData.size < 2500) {
            data = chartData as ArrayList<ArrayList<Double>>
        } else {
            if (!allData) {
                val increment = when (paperSpeed) {
                    12 -> 2600
                    25 -> 1250
                    50 -> 600
                    else -> 1250
                }
                val secondsBetweenPeaks = 60f / if (bpm == 0) 60 else bpm
                val points = (5000 * secondsBetweenPeaks).toInt() / 10
                val recordingData = chartData.subList(1250, 1250 + points)
                val lead1 = ArrayList<Double>()
                for (i in 0..recordingData.size - 1) {
                    lead1.add(recordingData[i][0])
                }
                val maxIndex = lead1.indexOf(lead1.maxOrNull())
                val start = maxIndex - (points / 2) + 1250

                val sliceRange = if (chartData.size > 2500) // 2.4 sec
                    IntRange(start, start + increment)
                else
                    IntRange(start, (chartData.size * 0.2).roundToInt() - 1)

                data = try {
                    chartData.slice(
                        sliceRange
                    ) as java.util.ArrayList<java.util.ArrayList<Double>>
                } catch (e: java.lang.Exception) {
                    if (e is IndexOutOfBoundsException) {
                        chartData.slice(
                            IntRange(
                                start,
                                chartData.size - 2
                            )
                        ) as java.util.ArrayList<java.util.ArrayList<Double>>
                    } else {
                        return ArrayList()
                    }
                }

            } else {
                data = chartData as ArrayList<ArrayList<Double>>
            }

        }

        val graphList = ArrayList<ArrayList<Double>>()

        val channelList1 = ArrayList<Double>()
        val channelList2 = ArrayList<Double>()
        val channelList3 = ArrayList<Double>()
        val channelList4 = ArrayList<Double>()
        val channelList5 = ArrayList<Double>()
        val channelList6 = ArrayList<Double>()
        val channelList7 = ArrayList<Double>()
        val channelList8 = ArrayList<Double>()
        val channelList9 = ArrayList<Double>()
        val channelList10 = ArrayList<Double>()
        val channelList11 = ArrayList<Double>()
        val channelList12 = ArrayList<Double>()
        val channelList13 = ArrayList<Double>()
        try {
            for (d in data) { // use chartData directly to show 1000 points
                for (i in 0 until d.size) {
                    if (i == 0) {
                        channelList1.add(d[0])
                    } else if (i == 1) {
                        channelList2.add(d[1])
                    } else if (i == 2) {
                        channelList3.add(d[2])
                    } else if (i == 3) {
                        channelList4.add(d[10])
                    } else if (i == 4) {
                        channelList5.add(d[9])
                    } else if (i == 5) {
                        channelList6.add(d[11])
                    } else if (i == 6) {
                        channelList7.add(d[3])
                    } else if (i == 7) {
                        channelList8.add(d[4])
                    } else if (i == 8) {
                        channelList9.add(d[5])
                    } else if (i == 9) {
                        channelList10.add(d[6])
                    } else if (i == 10) {
                        channelList11.add(d[7])
                    } else if (i == 11) {
                        channelList12.add(d[8])
                    }

                }
            }
        } catch (e: Exception) {
            Log.i("chartData", "\n" + chartData.size)
        }


        val filteredL1 = smoothedZScore(channelList1.toList(),10,3.0,0.2)
        val filteredL2 = smoothedZScore(channelList2.toList(),10,3.0,0.2)
        val filteredL3 = smoothedZScore(channelList3.toList(),10,3.0,0.2)
        val filteredAvr = smoothedZScore(channelList4.toList(),10,3.0,0.2)
        val filteredAvl = smoothedZScore(channelList5.toList(),10,3.0,0.2)
        val filteredAvf = smoothedZScore(channelList6.toList(),10,3.0,0.2)
        val filteredV1 = smoothedZScore(channelList7.toList(),10,3.0,0.2)
        val filteredV2 = smoothedZScore(channelList8.toList(),10,3.0,0.2)
        val filteredV3 = smoothedZScore(channelList9.toList(),10,3.0,0.2)
        val filteredV4 = smoothedZScore(channelList10.toList(),10,3.0,0.2)
        val filteredV5 = smoothedZScore(channelList11.toList(),10,3.0,0.2)
        val filteredV6 = smoothedZScore(channelList12.toList(),10,3.0,0.2)


        graphList.add(filteredL1.second as ArrayList<Double>)
        graphList.add(filteredL2.second as ArrayList<Double>)
        graphList.add(filteredL3.second as ArrayList<Double>)

        try {
            val increment = when (paperSpeed) {
                12 -> if (chartData.size > 5000) 5000 else chartData.size - 1
                25 -> if (chartData.size > 5000) 5000 else chartData.size - 1
                50 -> {
                    2600
                }
                else -> {
                    if (chartData.size > 5000) 5000 else chartData.size - 1
                }
            }
            val slicedData = chartData.slice(IntRange(0, increment))
            for (d in slicedData) {
                for (i in 0 until d.size) {
                    if (i == longLead) {
                        channelList13.add(d[longLead])
                        break
                    }
                }
            }

        } catch (e: Exception) {
            Log.i("chartData", "\n")
        }

        val filteredL13 = smoothedZScore(channelList13.toList(),10,3.0,0.2)

        graphList.add(filteredAvr.second as ArrayList<Double>)
        graphList.add(filteredAvl.second as ArrayList<Double>)
        graphList.add(filteredAvf.second as ArrayList<Double>)
        graphList.add(filteredV1.second as ArrayList<Double>)
        graphList.add(filteredV2.second as ArrayList<Double>)
        graphList.add(filteredV3.second as ArrayList<Double>)
        graphList.add(filteredV4.second as ArrayList<Double>)
        graphList.add(filteredV5.second as ArrayList<Double>)
        graphList.add(filteredV6.second as ArrayList<Double>)
        graphList.add(filteredL13.second as ArrayList<Double>)

        return graphList


    }


    /**
     * TODO
     *
     * @param array
     * @return
     */
    @SuppressLint("GetInstance")
    @Throws(Exception::class)
    override fun encryptByteArray(array: ByteArray): String {

        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        val secretKey = SecretKeySpec(byteArray, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val doFinal = cipher.doFinal(array)
        return Base64.encodeToString(doFinal!!, Base64.DEFAULT)
    }

    /**
     * TODO
     * @param data
     * @param privateKey
     * @return
     */
    @SuppressLint("GetInstance")
    @Throws(Exception::class)
    override fun decryptByteArray(data: ByteArray, privateKey: String): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        val key = getPrivateKey(privateKey)
        val secretKey = SecretKeySpec(key, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(data)
    }

    /**
     * TODO
     *
     * @param privateKey
     * @return
     */
    private fun getPrivateKey(privateKey: String?): ByteArray {
        var byteArray = byteArrayOf(0)
        if (privateKey != null) {
            byteArray = Base64.decode(privateKey, Base64.DEFAULT)
        }
        return byteArray
    }

    /**
     * Smoothed zero-score alogrithm
     * Uses a rolling mean and a rolling deviation (separate) to identify peaks in a vector
     *
     * @param y - The input vector to analyze
     * @param lag - The lag of the moving window (i.e. how big the window is)
     * @param threshold - The z-score at which the algorithm signals (i.e. how many standard deviations away from the moving mean a peak (or signal) is)
     * @param influence - The influence (between 0 and 1) of new signals on the mean and standard deviation (how much a peak (or signal) should affect other values near it)
     * @return - The calculated averages (avgFilter) and deviations (stdFilter), and the signals (signals)
     */
    override fun smoothedZScore(y: List<Double>, lag: Int, threshold: Double, influence: Double): Triple<List<Int>, List<Double>, List<Double>> {
        val stats = SummaryStatistics()
        // the results (peaks, 1 or -1) of our algorithm
        val signals = MutableList<Int>(y.size, { 0 })
        // filter out the signals (peaks) from our original list (using influence arg)
        val filteredY = ArrayList<Double>(y)
        // the current average of the rolling window
        val avgFilter = MutableList<Double>(y.size, { 0.0 })
        // the current standard deviation of the rolling window
        val stdFilter = MutableList<Double>(y.size, { 0.0 })
        // init avgFilter and stdFilter
        y.take(lag).forEach { s -> stats.addValue(s) }
        avgFilter[lag - 1] = stats.mean
        stdFilter[lag - 1] = Math.sqrt(stats.populationVariance) // getStandardDeviation() uses sample variance (not what we want)
        stats.clear()
        //loop input starting at end of rolling window
        (lag..y.size - 1).forEach { i ->
            //if the distance between the current value and average is enough standard deviations (threshold) away
            if (Math.abs(y[i] - avgFilter[i - 1]) > threshold * stdFilter[i - 1]) {
                //this is a signal (i.e. peak), determine if it is a positive or negative signal
                signals[i] = if (y[i] > avgFilter[i - 1]) 1 else -1
                //filter this signal out using influence
                filteredY[i] = (influence * y[i]) + ((1 - influence) * filteredY[i - 1])
            } else {
                //ensure this signal remains a zero
                signals[i] = 0
                //ensure this value is not filtered
                filteredY[i] = y[i]
            }
            //update rolling average and deviation
            (i - lag..i - 1).forEach { stats.addValue(filteredY[it]) }
            avgFilter[i] = stats.getMean()
            stdFilter[i] = Math.sqrt(stats.getPopulationVariance()) //getStandardDeviation() uses sample variance (not what we want)
            stats.clear()
        }
        return Triple(signals, avgFilter, stdFilter)
    }

}