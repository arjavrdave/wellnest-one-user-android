package com.rc.wellnestmodule.graphview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
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
import com.rc.wellnestmodule.utils.Util
import java.util.*

class GraphAdapter(
    val mContext: Context,
    val ecgSetup: String,
    var chartData: ArrayList<ArrayList<Double>>,
    val iScrollChart: IScrollChart
) : RecyclerView.Adapter<GraphAdapter.MyViewHolder>() {
    private var showALL: Boolean = true
    var counter = 0
    var leads: ArrayList<String> = Util.instance.getLeadsArray(ecgSetup)

    var scroll = 0f
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GraphAdapter.MyViewHolder {
        val from = LayoutInflater.from(mContext)
        val inflate =
            DataBindingUtil.inflate<ItemGraphModuleBinding>(
                from,
                R.layout.item_graph_module,
                parent,
                false
            )
        return MyViewHolder(inflate)
    }


    override fun onBindViewHolder(holder: GraphAdapter.MyViewHolder, position: Int) {
        try {



        } catch (e: Exception) {
            Log.i("chartData", "\n" + chartData.size)

        }
    }

    override fun getItemCount(): Int = if (showALL) chartData.size - 1 else 3

    fun setGraphList(graphList: ArrayList<ArrayList<Double>>) {
        chartData = graphList
        notifyDataSetChanged()
    }

    fun showAll(showALL: Boolean) {
        this.showALL = showALL
        notifyDataSetChanged()
    }

    inner class MyViewHolder(binding: ItemGraphModuleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val rowRecordingGraphBinding = binding

        init {
//            binding.lineChart.onChartGestureListener = scrollCallBack
        }

    }



    fun scrollAllCharts(dx: Float) {
        iScrollChart.scrollChart(dx)
    }
}

