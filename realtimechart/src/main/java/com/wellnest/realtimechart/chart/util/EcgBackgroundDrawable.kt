package com.wellnest.realtimechart.chart.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue

/**
 * Created by Hussain on 17/11/22.
 */

class EcgBackgroundDrawable(
    private val context: Context
) : Drawable() {

    private val TAG = "CustomDrawable"

    val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {

        val width = bounds.width()
        val height = bounds.height()

        canvas.drawColor(Color.WHITE)

        val px1mm = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_MM, 1f,
            context.resources.displayMetrics
        )

        val px5mm = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_MM, 5f,
            context.resources.displayMetrics
        )

        Log.i(TAG, "Pixels per 1 mm : $px1mm")


        Log.i(TAG, "Pixels per 5 mm : $px5mm")

        // draw 5mm lines
        val total5mmHorizontalLine = width / px5mm

        Log.i(TAG, "HorizontalLines : $total5mmHorizontalLine")

        var currentX = px5mm/2
        redPaint.strokeWidth = 2f
        for (i in 0 until total5mmHorizontalLine.toInt() + 1) {
            canvas.drawLine(currentX, 0f, currentX, height.toFloat(), redPaint)
            currentX += px5mm
        }

        val total5mmVerticalLines = height / px5mm

        var currentY = px5mm/2

        for (i in 0 until total5mmVerticalLines.toInt() + 1) {
            canvas.drawLine(0f, currentY, width.toFloat(), currentY, redPaint)
            currentY += px5mm
        }


        // draw 1mm lines

        val total1mmHorizontalLine = (width / px1mm)

        currentX = px1mm/2
        redPaint.strokeWidth = 0.5f
        for(i in 0 until total1mmHorizontalLine.toInt() + 1) {
            canvas.drawLine(currentX,0f,currentX,height.toFloat(),redPaint)
            currentX += px1mm
        }

        val total1mmVerticalLine = (height / px1mm)
        currentY = px1mm/2

        for (i in 0 until total1mmVerticalLine.toInt() + 1) {
            canvas.drawLine(0f,currentY,width.toFloat(),currentY,redPaint)
            currentY += px1mm
        }
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(p0: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }
}