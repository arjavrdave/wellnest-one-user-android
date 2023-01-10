package com.wellnest.one.utils;

import java.util.ArrayList;

/**
 * Created by Hussain on 28/11/22.
 */
public class RecordingGraphHelper {
    private static final RecordingGraphHelper ourInstance = new RecordingGraphHelper();

    public static RecordingGraphHelper getInstance() {
        return ourInstance;
    }

    private RecordingGraphHelper() {
    }

    private ArrayList<ArrayList<Double>> chartData;


    public ArrayList<ArrayList<Double>> getChartData() {
        if (chartData == null){
            return new ArrayList<>();
        }
        return chartData;
    }

    public void setChartData(ArrayList<ArrayList<Double>> chartData) {
        this.chartData = chartData;
    }


    public ArrayList<ArrayList<Double>> getStoredChartData(){
        return chartData;
    }


    public void clearChartData(){
        getChartData().clear();
        if (chartData!=null){
            chartData = null;
        }
    }

}
