package com.rc.wellnestmodule.graphview

import android.content.Context
import android.graphics.Matrix
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.EntryXComparator
import com.rc.wellnestmodule.R
import com.rc.wellnestmodule.databinding.ItemGraphModuleBinding
import com.rc.wellnestmodule.databinding.LayoutGraphBinding
import com.rc.wellnestmodule.utils.Util
import kotlinx.android.synthetic.main.layout_graph.view.*
import java.util.*


class EcgView(context: Context?) : ParentEcgView(context) {

    private lateinit var binding: LayoutGraphBinding
    private lateinit var mGraphAdapter: GraphAdapter
    var leads: java.util.ArrayList<String> = arrayListOf()
    var chartData: ArrayList<ArrayList<Double>> = ArrayList()
    private var scroll = 0f
    private var graphs = mutableListOf<ItemGraphModuleBinding>()

    override fun initialize(context: Context?) {
        val inflator = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate<LayoutGraphBinding>(
            inflator,
            R.layout.layout_graph,
            this,
            false
        )
    }

    fun getGraphView(setup: String, graphList: ArrayList<ArrayList<Double>>): View {
        leads = Util.instance.getLeadsArray(setup)
        chartData = graphList



        graphs.add(binding.graphL1)
        graphs.add(binding.graphL2)
        graphs.add(binding.graphL3)
        graphs.add(binding.Avr)
        graphs.add(binding.Avl)
        graphs.add(binding.Avf)
        graphs.add(binding.V1)
        graphs.add(binding.V2)
        graphs.add(binding.V3)
        graphs.add(binding.V4)
        graphs.add(binding.V5)
        graphs.add(binding.V6)

        for (i in 0 until graphs.size) {
            if (i < chartData.size) {
                setupGraphAtPosition(i, leads[i], graphs[i], context)
            }
        }
//        mGraphAdapter = GraphAdapter(context!!, setup, graphList,this)

//        binding.recyclerView.recycledViewPool.setMaxRecycledViews(1, Int.MAX_VALUE)
//        binding.recyclerView.adapter = mGraphAdapter
//        binding.recyclerView.isNestedScrollingEnabled = false
        return binding.root
    }

//    override fun scrollChart(dx: Float) {
//        binding.recyclerView.forEachVisibleHolder<GraphAdapter.MyViewHolder> { myViewHolder ->
//            myViewHolder.rowRecordingGraphBinding.lineChart.moveViewToX(dx)
//        }
//    }


    private fun setupGraphAtPosition(
        position: Int,
        lead: String,
        binding: ItemGraphModuleBinding,
        mContext: Context
    ) {
        val lead = leads[position]
        binding.tvSr.setText(lead)
        val entries = java.util.ArrayList<Entry>()
        var index = 0
        val arrayList = chartData[position]
        for (data in arrayList) {
            index++
            val xVal = index.toFloat()
            val yVal = data.toFloat()
            entries.add(Entry(xVal, yVal))
        }

        // sort by x-value
        Collections.sort(entries, EntryXComparator())

        if (position == 11) {
            binding.lineChart.setViewPortOffsets(
                20 * (mContext.resources.displayMetrics).density,
                0f,
                20 * (mContext.resources.displayMetrics).density,
                20 * (mContext.resources.displayMetrics).density
            )
        } else {
            binding.lineChart.setViewPortOffsets(
                20 * (mContext.resources.displayMetrics).density,
                0f,
                20 * (mContext.resources.displayMetrics).density,
                0f
            )
        }

        // create a dataset and give it a type
//            val set = LineDataSet(entries, "ECG Graph " + counter)
        val set = LineDataSet(entries, "")

        set.lineWidth = 1.5f
        set.circleRadius = 4f
        set.color = ContextCompat.getColor(mContext, R.color.spike_line)
        // create a data object with the data sets
        val data = LineData(set)
//            holder.rowRecordingGraphBinding.lineChart.setVisibleXRange(-20F, 20F)
        binding.lineChart.axisLeft.setAxisMaxValue(20f);
        binding.lineChart.axisLeft.setAxisMinValue(-20f);
        binding.lineChart.axisRight.setAxisMaxValue(20f);
        binding.lineChart.axisRight.setAxisMinValue(-20f);

        binding.lineChart.legend.isEnabled = false


        val xAxis = binding.lineChart.xAxis
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)


        val axisLeft = binding.lineChart.axisLeft
        axisLeft.setDrawLabels(false)
        axisLeft.setDrawAxisLine(false)
        axisLeft.setDrawGridLines(false)

        val axisRight = binding.lineChart.axisRight
        axisRight.setDrawLabels(false)
        axisRight.setDrawAxisLine(false)
        axisRight.setDrawGridLines(false)


//            holder.rowRecordingGraphBinding.lineChart.isScaleXEnabled = true
//        binding.lineChart.isDragEnabled = true
        binding.lineChart.description.isEnabled = false

        // set data
        binding.lineChart.data = data
        data.isHighlightEnabled = !data.isHighlightEnabled

        val sets = binding.lineChart.data.dataSets

        for (iSet in sets) {
            val set = iSet as LineDataSet
            set.setDrawValues(!set.isDrawValuesEnabled)
        }


        for (iSet in sets) {

            val set = iSet as LineDataSet
            if (set.isDrawCirclesEnabled)
                set.setDrawCircles(false)
            else
                set.setDrawCircles(true)
        }

        binding.lineChart.onChartGestureListener = ScrollCallback().setPosition(position)

        binding.lineChart.setVisibleXRangeMaximum(1000f)
        binding.lineChart.isScaleYEnabled = false
        binding.lineChart.isScaleXEnabled = false
        binding.lineChart.setScaleMinima(1f, 1f)
        binding.lineChart.animateX(1000)
    }

    inner class ScrollCallback : OnChartGestureListener {

        private var position = -1
        private val TAG = "GraphAdapter"

        override fun onChartGestureStart(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {

        }

        override fun onChartGestureEnd(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
        }

        override fun onChartLongPressed(me: MotionEvent?) {
        }

        override fun onChartDoubleTapped(me: MotionEvent?) {
        }

        override fun onChartSingleTapped(me: MotionEvent?) {
        }

        override fun onChartFling(
            me1: MotionEvent?,
            me2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ) {
        }

        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        }

        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
            syncCharts(position)
        }

        fun setPosition(position: Int): ScrollCallback {
            this.position = position
            return this
        }

    }



    fun syncCharts(position: Int) {
        val srcVals = FloatArray(9)
        var dstMatrix: Matrix
        val dstVals = FloatArray(9)

        // get src chart translation matrix:
        val srcMatrix: Matrix = graphs[position].lineChart.viewPortHandler.matrixTouch
        srcMatrix.getValues(srcVals)

        // apply X axis scaling and position to dst charts:
        for (dstChart in graphs) {
            dstMatrix = dstChart.lineChart.viewPortHandler.matrixTouch
            dstMatrix.getValues(dstVals)
            dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X]
            dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X]
            dstMatrix.setValues(dstVals)
            dstChart.lineChart.viewPortHandler.refresh(dstMatrix, dstChart.lineChart, true)

        }
    }
}

interface IScrollChart {
    fun scrollChart(dx: Float)
}
