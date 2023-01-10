package com.wellnest.one.utils

/**
 * Created by Hussain on 28/11/22.
 */


import com.wellnest.one.R
import com.wellnest.one.databinding.LayoutPdfBinding
import com.wellnest.one.dto.EcgRecording
import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.rc.wellnestmodule.utils.RecordBytesFactory
import java.util.*
import kotlin.collections.ArrayList


/*
 *   {@author Chinmay Dighe}
 *
 *   <p>
 *   Description: This class helps us to generate ecg pdf layouts with different combinations e.g(5,10,20 mm/mV)
 *   in online mode.
 *   </p>
 *   {@param EcgRecording is the only parameter used to set data. They are made available from the remote database.}
 *
 */

class PrintBitmapGenerator {

    private var graphSetup = "L2"
    private var graphData = ArrayList<ArrayList<Double>>()
    private var graphLeadIndex: Int = 1
    private lateinit var mainlayout: LayoutPdfBinding
    private lateinit var mContext: Context
    private val recordBytes = RecordBytesFactory().getRecordBytes()
    private var mPaperSpeed = 25
    private var mAmplitude = 10
    private var mEcgSetup: String? = "standard"

    fun createPdf(
        ecgRecording: EcgRecording?,
        graphList: ArrayList<ArrayList<Double>>,
        context: Context,
        testDate: Date,
        config: String,
        signatureBitmap: Bitmap?,
        logoBitmap: Bitmap? = null,
        addSignature: Boolean,
        paperSpeed: Int = 25,
        amplitude: Int = 10,
        graphSetup: String,
    ) : Bitmap? {

        val inflater = LayoutInflater.from(context)
        mainlayout = LayoutPdfBinding.inflate(inflater,null,false)

        mContext = context

        this.graphSetup = graphSetup
        this.graphData = graphList
        this.graphLeadIndex = getGraphLeadIndex(graphSetup)
        val recordingData = recordBytes.getGraphList(
            graphList,
            ecgRecording?.bpm?.toInt()?: 0,
            false,
            paperSpeed = 25,
            graphLeadIndex
        )
        mPaperSpeed = paperSpeed
        mAmplitude = amplitude
        setupHeader(logoBitmap, ecgRecording, testDate)
        setupFooter(ecgRecording, signatureBitmap, addSignature)

        return fillLineCharts(recordingData,config)
    }

    private fun setupHeader(logoBitmap: Bitmap?, ecgRecording: EcgRecording?, testDate: Date) {
        if (logoBitmap != null) {
            val logoW = logoBitmap.width
            val logoH = logoBitmap.height


            if (logoW > (logoH * 2)) {
                mainlayout.imgHeaderLogo.layoutParams.width = logoW;
                mainlayout.imgHeaderLogo.layoutParams.height = logoH;

                mainlayout.imgHeaderLogo.setImageBitmap(logoBitmap)
                mainlayout.imgHeaderLogo.requestLayout()
                mainlayout.orgLogo.visibility = View.INVISIBLE
                mainlayout.imgHeaderLogo.visibility = View.VISIBLE
            } else {

                mainlayout.orgLogo.setImageBitmap(logoBitmap)
                mainlayout.orgLogo.requestLayout()
                mainlayout.imgHeaderLogo.visibility = View.INVISIBLE
                mainlayout.orgLogo.visibility = View.VISIBLE
            }
        } else {
            mainlayout.imgHeaderLogo.visibility = View.VISIBLE
        }

        mainlayout.apply {
            if (ecgRecording != null) {

                with(ecgRecording) {

                    if (patient != null)
                        tvName.text = ecgRecording.getPatientName()
                    else
                        tvName.visibility = View.INVISIBLE

                    try {
                        if (!patient?.patientDateOfBirth.isNullOrEmpty()) {
                            val date =
                                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                    .parse(patient?.patientDateOfBirth!!)

                            val currentDate = Date()

                            if (!patient.patientGender.isNullOrEmpty())
                                tvGenderAge.text =
                                    Util.getGenderAge(
                                        patient.patientGender,
                                        patient.patientDateOfBirth
                                    )
                            else
                                tvGenderAge.visibility = View.INVISIBLE
                        }

                        val simpleDateFormat = java.text.SimpleDateFormat("dd MMM, yyyy")
                        val simpleDateFormat1 = java.text.SimpleDateFormat("hh:mm a")

                        tvDateTime.text =
                            Util.testDateAmPm(ecgRecording.createdAt ?:"")

                    } catch (e: Exception) {
                        android.util.Log.v("", "")
                    }

                    if (bpm == null) {
                        tvHr.text = "0 bpm"
                    } else {
                        bpm.let {
                            tvHr.text = "$bpm bpm"
                        }
                    }


                    if (qrs == null) {
                        tvQrs.text = "0.0 ms"
                    } else {
                        qrs.let {
                            tvQrs.text = "$qrs ms"
                        }
                    }

                    if (st == null) {
                        tvSt.text = "0.0 ms"
                    } else {
                        st.let {
                            tvSt.text = "$st ms"
                        }
                    }



                    if (qt == null) {
                        tvQt.text = "0.0 ms"
                    } else {
                        qt.let {
                            tvQt.text = "$qt ms"
                        }
                    }


                    if (pr == null) {
                        tvPr.text = "0.0 ms"
                    } else {
                        pr.let {
                            tvPr.text = "$pr ms"
                        }
                    }

                    if (qtc == null) {
                        tvQtc.text = "0.0 ms"
                    } else {
                        qtc.let {
                            tvQtc.text = "$qtc ms"
                        }
                    }
                }
            }
        }

    }

    private fun setupFooter(
        ecgRecording: EcgRecording?,
        signatureBitmap: Bitmap?,
        addSignature: Boolean
    ) {
        mainlayout.apply {

            when (mPaperSpeed) {
                12 -> tvPaperSpeed.text = "12.5 mm/s"
                25 -> tvPaperSpeed.text = "25 mm/s"
                50 -> tvPaperSpeed.text = "50 mm/s"
            }

            when (mAmplitude) {
                5 -> tvAmplitude.text = "5 mm/mV"
                10 -> tvAmplitude.text = "10 mm/mV"
                20 -> tvAmplitude.text = "20 mm/mV"
            }

            if (signatureBitmap != null) {
                /*set signature bitmap*/
                if (addSignature) {
                    imgSignature.setImageBitmap(signatureBitmap)
                }
            }


            if (ecgRecording != null) {

//                val user = SharedPreferenceHelper.getUser(mContext)
//                val technician = SharedPreferenceHelper.getTechnicianUser(mContext)



                with(ecgRecording) {


                    if (setup != null) {
                        mEcgSetup = setup
                        tvSetup.text = setup?.capitalize()
                    }


                    if (reportedBy != null) {
                        tvReportedBy.text =
                            reportedBy.toString() + ", " + (reportedBy.qualification ?: "")
                    }

//                    if (referredBy != null) {
//                        referredBy.let {
//                            tvReferredBy.text =
//                                "${getRefferDoctorName()}, ${it.qualification}, ${organisation.toString()}, ${organisation!!.street}, ${organisation.getCityState()}|${
//                                    ValidationHelper.formatPhone(
//                                        organisation.phoneNumber, ""
//                                    )
//                                }"
//                        }
//                    } else {
//
//                        if (organisation != null) {
//                            tvReferredBy.text =
//                                organisation.toString() + ", " + organisation.street + ", " + organisation.getCityState() + " | " + ValidationHelper.formatPhone(
//                                    organisation.phoneNumber, ""
//                                )
//                        } else if (user != null) {
//                            tvReferredBy.text =
//                                user.toString() + ", " + user.qualification + ", " + user.organisation!![0].toString() + ", " + user.organisation[0].street + ", " + user.organisation[0].getCityState() + " | " + ValidationHelper.formatPhone(
//                                    user.phoneNumber, ""
//                                )
//                        } else if (technician != null) {
//                            tvReferredBy.text =
//                                technician.organisation!![0].toString() + ", " + technician.organisation!![0].street + ", " + technician.organisation!![0].getCityState() + " | " + ValidationHelper.formatPhone(
//                                    technician.organisation[0].phoneNumber,
//                                    ""
//                                )
//                        }
//                    }




                    if (!ecgFindings.isNullOrEmpty()) {
                        if (ecgFindings.contains("\n")) {
                            val formattedFindings = ecgFindings.replace("\n", " ")
                            tvFindings.text = formattedFindings
                        } else {
                            tvFindings.text = ecgFindings
                        }
                    } else {
                        tvFindings.text = "-"
                    }

                    if (!interpretations.isNullOrEmpty()) {
                        if (interpretations.contains("\n")) {
                            val formattedInterpretation = interpretations.replace("\n", " ")
                            tvInterpretation.text = formattedInterpretation
                        } else {
                            tvInterpretation.text = interpretations
                        }
                    } else {
                        tvInterpretation.text = "-"
                    }

                    if (!recommendations.isNullOrEmpty()) {
                        if (recommendations.contains("\n")) {
                            val formattedRecommendations = recommendations.replace("\n", " ")
                            tvRecommendations.text = formattedRecommendations
                        } else {
                            tvRecommendations.text = recommendations
                        }
                    } else {
                        tvRecommendations.text = "-"
                    }


                }
            }
        }
    }

    private fun fillLineCharts(
        graphList: ArrayList<ArrayList<Double>>,
        setting: String
    ): Bitmap {
        var index = 0
        val leadL1 = ArrayList<Double>()
        val leadL2 = ArrayList<Double>()
        val leadL3 = ArrayList<Double>()
        val leadAvr = ArrayList<Double>()
        val leadAvl = ArrayList<Double>()
        val leadAvf = ArrayList<Double>()
        val leadV1 = ArrayList<Double>()
        val leadV2 = ArrayList<Double>()
        val leadV3 = ArrayList<Double>()
        val leadV4 = ArrayList<Double>()
        val leadV5 = ArrayList<Double>()
        val leadV6 = ArrayList<Double>()
        val leadBig = ArrayList<Double>()

        for (leads in graphList) {
            if (index == 0) {
                leadL1.addAll(leads)
            } else if (index == 1) {
                leadL2.addAll(leads)
            } else if (index == 2) {
                leadL3.addAll(leads)
            } else if (index == 3) {
                leadAvr.addAll(leads)
            } else if (index == 4) {
                leadAvl.addAll(leads)
            } else if (index == 5) {
                leadAvf.addAll(leads)
            } else if (index == 6) {
                leadV1.addAll(leads)
            } else if (index == 7) {
                leadV2.addAll(leads)
            } else if (index == 8) {
                leadV3.addAll(leads)
            } else if (index == 9) {
                leadV4.addAll(leads)
            } else if (index == 10) {
                leadV5.addAll(leads)
            } else if (index == 11) {
                leadV6.addAll(leads)
            } else if (index == 12) {
                leadBig.addAll(graphList[12])
            }
            index++
        }

        when (mEcgSetup) {

            "right side" -> {
                mainlayout.labelV1.text = "V1R"
                mainlayout.labelV2.text = "V2R"
                mainlayout.labelV3.text = "V3R"
                mainlayout.labelV4.text = "V4R"
                mainlayout.labelV5.text = "V5R"
                mainlayout.labelV6.text = "V6R"
            }

            "posterior" -> {
                mainlayout.labelV4.text = "V7"
                mainlayout.labelV5.text = "V8"
                mainlayout.labelV6.text = "V9"
            }
        }

        //62,109,210

        when (mAmplitude) {
            5 -> setLayoutHeights(mainlayout.root as ViewGroup, 62)
            10 -> setLayoutHeights(mainlayout.root as ViewGroup, 94)
            20 -> {
                setLayoutHeights(mainlayout.root as ViewGroup, 210)
                mainlayout.thirdLineLayout.visibility = View.GONE
                mainlayout.layoutL2Big.visibility = View.GONE
            }
        }

        //setting - plain
        // setting - any graph paper
        // wellnest graph paper

        if (setting == "any_graph") {
            mainlayout.imgGraph.visibility = View.INVISIBLE
        }

        if (setting == "wellnest_graph") {
            mainlayout.imgGraph.visibility = View.INVISIBLE

            mainlayout.labelL1.visibility = View.INVISIBLE
            mainlayout.labelL2.visibility = View.INVISIBLE
            mainlayout.labelL3.visibility = View.INVISIBLE

            mainlayout.labelAvr.visibility = View.INVISIBLE
            mainlayout.labelAvf.visibility = View.INVISIBLE
            mainlayout.labelAvl.visibility = View.INVISIBLE

            mainlayout.labelV1.visibility = View.INVISIBLE
            mainlayout.labelV2.visibility = View.INVISIBLE
            mainlayout.labelV3.visibility = View.INVISIBLE

            mainlayout.labelV4.visibility = View.INVISIBLE
            mainlayout.labelV5.visibility = View.INVISIBLE
            mainlayout.labelV6.visibility = View.INVISIBLE

            mainlayout.labelII.visibility = View.VISIBLE

        }

        mainlayout.labelII.text = when (graphSetup) {
            mContext.getString(R.string.L1) -> {
                mContext.getString(R.string.L1)
            }
            mContext.getString(R.string.L2) -> {
                mContext.getString(R.string.L2)
            }
            mContext.getString(R.string.L3) -> {
                mContext.getString(R.string.L3)
            }
            mContext.getString(R.string.avR) -> {
                mContext.getString(R.string.avR)
            }
            mContext.getString(R.string.avL) -> {
                mContext.getString(R.string.avL)
            }
            mContext.getString(R.string.avF) -> {
                mContext.getString(R.string.avF)
            }
            mContext.getString(R.string.v1) -> {
                mContext.getString(R.string.v1)
            }
            mContext.getString(R.string.v2) -> {
                mContext.getString(R.string.v2)
            }
            mContext.getString(R.string.v3) -> {
                mContext.getString(R.string.v3)
            }
            mContext.getString(R.string.v4) -> {
                mContext.getString(R.string.v4)
            }
            mContext.getString(R.string.v5) -> {
                mContext.getString(R.string.v5)
            }
            mContext.getString(R.string.v6) -> {
                mContext.getString(R.string.v6)
            }
            else -> {
                mContext.getString(R.string.L2)
            }
        }


        val entries1 = ArrayList<Entry>()
        val entries2 = ArrayList<Entry>()
        val entries3 = ArrayList<Entry>()
        val entries4 = ArrayList<Entry>()
        val entries5 = ArrayList<Entry>()
        val entries6 = ArrayList<Entry>()
        val entries7 = ArrayList<Entry>()
        val entries8 = ArrayList<Entry>()
        val entries9 = ArrayList<Entry>()
        val entries10 = ArrayList<Entry>()
        val entries11 = ArrayList<Entry>()
        val entries12 = ArrayList<Entry>()
        val entries13 = ArrayList<Entry>()


        //Lead 1
        var index1 = 0
        for (data in leadL1) {
            index1++
            val xVal = index1.toFloat()
            val yVal = data.toFloat()
            entries1.add(Entry(xVal, yVal))
        }

        setEntries(entries1, mainlayout.lineChartL1)


        //Lead 2
        var index2 = 0
        for (data in leadL2) {
            index2++
            val xVal = index2.toFloat()
            val yVal = data.toFloat()
            entries2.add(Entry(xVal, yVal))
        }

        setEntries(entries2, mainlayout.lineChartL2)

        //Lead 3
        var index3 = 0
        for (data in leadL3) {
            index3++
            val xVal = index3.toFloat()
            val yVal = data.toFloat()
            entries3.add(Entry(xVal, yVal))
        }

        setEntries(entries3, mainlayout.lineChartL3)


        //Lead 4
        var index4 = 0
        for (data in leadAvr) {
            index4++
            val xVal = index4.toFloat()
            val yVal = data.toFloat()
            entries4.add(Entry(xVal, yVal))
        }

        setEntries(entries4, mainlayout.lineChartAVR)


        //Lead 5
        var index5 = 0
        for (data in leadAvl) {
            index5++
            val xVal = index5.toFloat()
            val yVal = data.toFloat()
            entries5.add(Entry(xVal, yVal))
        }

        setEntries(entries5, mainlayout.lineChartAVL)


        //Lead 6
        var index6 = 0
        for (data in leadAvf) {
            index6++
            val xVal = index6.toFloat()
            val yVal = data.toFloat()
            entries6.add(Entry(xVal, yVal))
        }

        setEntries(entries6, mainlayout.lineChartAVF)

        //Lead 7
        var index7 = 0
        for (data in leadV1) {
            index7++
            val xVal = index7.toFloat()
            val yVal = data.toFloat()
            entries7.add(Entry(xVal, yVal))
        }

        setEntries(entries7, mainlayout.lineChartV1)


        //Lead 8
        var index8 = 0
        for (data in leadV2) {
            index8++
            val xVal = index8.toFloat()
            val yVal = data.toFloat()
            entries8.add(Entry(xVal, yVal))
        }

        setEntries(entries8, mainlayout.lineChartV2)


        //Lead 9
        var index9 = 0
        for (data in leadV3) {
            index9++
            val xVal = index9.toFloat()
            val yVal = data.toFloat()
            entries9.add(Entry(xVal, yVal))
        }

        setEntries(entries9, mainlayout.lineChartV3)


        //Lead 10
        var index10 = 0
        for (data in leadV4) {
            index10++
            val xVal = index10.toFloat()
            val yVal = data.toFloat()
            entries10.add(Entry(xVal, yVal))
        }

        setEntries(entries10, mainlayout.lineChartV4)

        //Lead 11
        var index11 = 0
        for (data in leadV5) {
            index11++
            val xVal = index11.toFloat()
            val yVal = data.toFloat()
            entries11.add(Entry(xVal, yVal))
        }

        setEntries(entries11, mainlayout.lineChartV5)


        //Lead 12
        var index12 = 0
        for (data in leadV6) {
            index12++
            val xVal = index12.toFloat()
            val yVal = data.toFloat()
            entries12.add(Entry(xVal, yVal))
        }


        setEntries(entries12, mainlayout.lineChartV6)


        //Lead 13
        var index13 = 0
        for (data in leadBig) {
            index13++
            val xVal = index13.toFloat()
            val yVal = data.toFloat()
            entries13.add(Entry(xVal, yVal))
        }


        setEntries(entries13, mainlayout.lineChartII)

        var height = 0
        when (mAmplitude) {
            5 -> height = 62
            10 -> height = 94
            20 -> height = 210
        }

        if (mPaperSpeed == 12) {
            val params = FrameLayout.LayoutParams(
                Util.toDP(mContext, 370),
                Util.toDP(mContext, height)
            )

            mainlayout.lineChartII.layoutParams = params
        } else if (mPaperSpeed == 25 && setting != "wellnest_graph") { // set 50 boxes

            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                Util.toDP(mContext, height)
            )

            params.setMargins(Util.toDP(mContext, 35), 0, Util.toDP(mContext, 35), 0)

            mainlayout.lineChartII.layoutParams = params
        } else if (mPaperSpeed == 50) { //take all boxes
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                Util.toDP(mContext, height)
            )


            mainlayout.lineChartII.layoutParams = params
        }




        mainlayout.root.isDrawingCacheEnabled = true;


        mainlayout.root.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        mainlayout.root.layout(
            0,
            0,
            mainlayout.root.measuredWidth,
            mainlayout.root.measuredHeight
        );

        if (mPaperSpeed == 25 && setting != "wellnest_graph") {
            if (leadBig.size < 5000) {
                val l13Width = (leadBig.size * mainlayout.layoutL2Big.width) / 5000
                val params = mainlayout.layoutL2Big.layoutParams
                params.width = l13Width

                mainlayout.layoutL2Big.layoutParams = params
                mainlayout.root.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                mainlayout.root.layout(
                    0,
                    0,
                    mainlayout.root.measuredWidth,
                    mainlayout.root.measuredHeight
                );
            }
        }

        return mainlayout.root.generateBitmap()
    }

    private fun setEntries(
        entries: ArrayList<Entry>,
        lineChart: LineChart,
    ) {

        lineChart.setViewPortOffsets(0f, 0f, 0f, 0f)

        // create a dataset and give it a type
        val set = LineDataSet(entries, "")

        set.lineWidth = 1.0f
        set.circleRadius = 0.2f
        set.setCircleColor(Color.WHITE)
        set.highLightColor = Color.WHITE
        set.color = Color.BLACK


        // create a data object with the data sets
        val data = LineData(set)
        lineChart.axisLeft.axisMaximum = entries.maxOf {
            it.y
        }.plus(4.8f)
        lineChart.axisLeft.axisMinimum = entries.minOf {
            it.y
        }.minus(4.8f)
        lineChart.axisRight.axisMaximum = entries.maxOf {
            it.y
        }.plus(4.8f)
        lineChart.axisRight.axisMinimum = entries.minOf {
            it.y
        }.minus(4.8f)

        lineChart.data = data

        lineChart.isDragEnabled = true

        setRomoveZoomAndOtherDisable(lineChart, data)
    }

    private fun setRomoveZoomAndOtherDisable(chart: LineChart, data: LineData) {

        data.setHighlightEnabled(!data.isHighlightEnabled())

        chart.setDrawGridBackground(false)

        chart.setDrawBorders(false)


//        chart.setGridBackgroundColor(ContextCompat.getColor(this, R.color.ecg_grid_light))

        chart.getLegend().setEnabled(false)

        val xAxis = chart.getXAxis()
        xAxis.setDrawLabels(false)
        xAxis.setDrawGridLines(false)
        xAxis.setAxisLineColor(Color.TRANSPARENT);

        val axisLeft = chart.getAxisLeft()
        axisLeft.setDrawLabels(false)
        axisLeft.setDrawGridLines(false)
        axisLeft.setAxisLineColor(Color.TRANSPARENT);


        val axisRight = chart.getAxisRight()
        axisRight.setDrawLabels(false)
        axisRight.setDrawGridLines(false)
        axisRight.setAxisLineColor(Color.TRANSPARENT);

        chart.getDescription().setEnabled(false)


        if (chart.getData() != null) {
            chart.getData().setHighlightEnabled(!chart.getData().isHighlightEnabled())
            chart.invalidate()
        }

        val sets = chart.getData()
            .getDataSets()

        for (iSet in sets) {

            val set = iSet as LineDataSet
            set.setDrawValues(false)
        }
        chart.invalidate()
        for (iSet in sets) {
            val set = iSet as LineDataSet
            set.setDrawCircles(false)
        }
    }


    private fun setLayoutHeights(mainlayout: ViewGroup, height: Int) {

        val layoutL1 = mainlayout.findViewById<LineChart>(R.id.lineChartL1)
        val layoutL2 = mainlayout.findViewById<LineChart>(R.id.lineChartL2)
        val layoutL3 = mainlayout.findViewById<LineChart>(R.id.lineChartL3)

        val layoutAvr = mainlayout.findViewById<LineChart>(R.id.lineChartAVR)
        val layoutAvl = mainlayout.findViewById<LineChart>(R.id.lineChartAVL)
        val layoutAvf = mainlayout.findViewById<LineChart>(R.id.lineChartAVF)

        val layoutV1 = mainlayout.findViewById<LineChart>(R.id.lineChartV1)
        val layoutV2 = mainlayout.findViewById<LineChart>(R.id.lineChartV2)

        val layoutV3 = mainlayout.findViewById<LineChart>(R.id.lineChartV3)
        val layoutV4 = mainlayout.findViewById<LineChart>(R.id.lineChartV4)

        val layoutV5 = mainlayout.findViewById<LineChart>(R.id.lineChartV5)
        val layoutV6 = mainlayout.findViewById<LineChart>(R.id.lineChartV6)
        val layoutL2Big = mainlayout.findViewById<LineChart>(R.id.lineChartII)


        var params: ViewGroup.LayoutParams? = null

        if (mAmplitude == 5) { // set outer layout height  to 94

            val firstlayout = mainlayout.findViewById<LinearLayout>(R.id.firstLineLayout)
            val secondLayout = mainlayout.findViewById<LinearLayout>(R.id.secondLineLayout)
            val thirdlayout = mainlayout.findViewById<LinearLayout>(R.id.thirdLineLayout)
            val fourthLayout = mainlayout.findViewById<FrameLayout>(R.id.layoutL2Big)

            params = firstlayout.layoutParams
            params!!.height = Util.toDP(mContext, 94)
            firstlayout.layoutParams = params

            params = secondLayout.layoutParams
            params.height = Util.toDP(mContext, 94)
            secondLayout.layoutParams = params

            params = thirdlayout.layoutParams
            params.height = Util.toDP(mContext, 94)
            thirdlayout.layoutParams = params

            params = fourthLayout.layoutParams
            params.height = Util.toDP(mContext, 94)
            fourthLayout.layoutParams = params
        }

        params = layoutL1.layoutParams
        params!!.height = Util.toDP(mContext, height)
        layoutL1.layoutParams = params

        params = layoutL2.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutL2.layoutParams = params

        params = layoutL3.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutL3.layoutParams = params

        params = layoutAvr.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutAvr.layoutParams = params

        params = layoutAvl.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutAvl.layoutParams = params

        params = layoutAvf.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutAvf.layoutParams = params

        params = layoutV1.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutV1.layoutParams = params

        params = layoutV2.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutV2.layoutParams = params

        params = layoutV3.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutV3.layoutParams = params

        params = layoutV4.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutV4.layoutParams = params

        params = layoutV5.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutL1.layoutParams = params

        params = layoutV6.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutV6.layoutParams = params

        params = layoutL2Big.layoutParams
        params.height = Util.toDP(mContext, height)
        layoutL2Big.layoutParams = params
    }


    private fun getGraphLeadIndex(graphSetup: String): Int {
        return when (graphSetup) {
            mContext.getString(R.string.L1) -> {
                0
            }
            mContext.getString(R.string.L2) -> {
                1
            }
            mContext.getString(R.string.L3) -> {
                2
            }
            mContext.getString(R.string.avR) -> {
                10
            }
            mContext.getString(R.string.avL) -> {
                9
            }
            mContext.getString(R.string.avF) -> {
                11
            }
            mContext.getString(R.string.v1) -> {
                3
            }
            mContext.getString(R.string.v2) -> {
                4
            }
            mContext.getString(R.string.v3) -> {
                5
            }
            mContext.getString(R.string.v4) -> {
                6
            }
            mContext.getString(R.string.v5) -> {
                7
            }
            mContext.getString(R.string.v6) -> {
                8
            }
            else -> {
                1
            }
        }
    }


}


