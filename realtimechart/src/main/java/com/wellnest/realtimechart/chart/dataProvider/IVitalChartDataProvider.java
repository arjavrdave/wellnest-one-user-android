package com.wellnest.realtimechart.chart.dataProvider;


import android.graphics.drawable.Drawable;
import com.wellnest.realtimechart.chart.chart.RealTimeVitalChart;
import com.wellnest.realtimechart.chart.util.Transformer;

/**
 * 실시간 차트의 설정값 및 정보 전달자
 * @author Dahun Kim
 */
public interface IVitalChartDataProvider {

    int getOneSecondDataCount();

    int getVisibleSecondRange();

    int getTotalRangeCount();

    float getRefreshGraphInterval();

    float[] getRealTimeDataList();

    int getLineColor();

    float getLineWidth();

    RealTimeVitalChart.LineMode getLineMode();

    boolean getEnabledValueCircleIndicator();

    float getValueCircleIndicatorRadius();

    int getValueCircleIndicatorColor();

    float getVitalMaxValue();

    float getVitalMinValue();

    Transformer getTransformer();

    void dequeueRealTimeData(float value);

    Drawable getChartBackgroundDrawable();

    Integer getChartBackgroundColor();

    int getChartLeft();

    int getChartTop();

    int getChartRight();

    int getChartBottom();

    int getChartHeight();

    int getChartWidth();

}
