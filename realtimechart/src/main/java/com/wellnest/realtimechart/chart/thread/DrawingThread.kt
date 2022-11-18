package com.wellnest.realtimechart.chart.thread

import android.graphics.Canvas
import android.os.Build
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import com.wellnest.realtimechart.chart.renderer.RealTimeVitalRenderer
import kotlin.Exception

/**
 * SurfaceView의 렌더링 스레드
 *
 * @author Dahun Kim
 */
class DrawingThread(
    private val surfaceHolder: SurfaceHolder,
    private val renderer: RealTimeVitalRenderer
) : Thread() {
    private var isRunning = false
    private var previousTime: Long
    private val fps = 70
    fun setRunning(run: Boolean) {
        isRunning = run
    }

    override fun run() {
        var canvas: Canvas?
        var nowTime: Long
        var elapsedTime: Long
        var sleepTime: Long
        while (isRunning) {
            nowTime = System.currentTimeMillis()
            elapsedTime = nowTime - previousTime
            sleepTime = (1000f / fps - elapsedTime).toLong()
            canvas = null
            try {
//                canvas = if (surfaceHolder is SurfaceHolder && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    surfaceHolder.lockHardwareCanvas()
//                } else {
//                    surfaceHolder.lockCanvas()
//                }

//                if (canvas == null) {
//                    //Thread.sleep(1);
//                    continue
//                } else if (sleepTime > 0) {
                    sleep(100);
//                }

                canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    lockHardwareCanvas()
                } else {
                    lockCanvas()
                }

                synchronized(surfaceHolder) {
                    // draw
                    renderer.drawVitalValue(canvas)
                }
            } catch (e: Exception) {
                unlockCanvas(canvas)
            } finally {
                if (canvas != null) {
                    unlockCanvas(canvas)
                    previousTime = System.currentTimeMillis()
                }
            }
        }
    }

    private fun unlockCanvas(canvas: Canvas?) {
        if (canvas != null) {
            try {
                surfaceHolder.unlockCanvasAndPost(canvas)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun lockCanvas()  : Canvas? {
        return try {
            surfaceHolder.lockCanvas()
        } catch (e : Exception) {
            e.printStackTrace()
            null
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun lockHardwareCanvas() : Canvas? {
       return try {
            surfaceHolder.lockHardwareCanvas()
        } catch (e : Exception) {
            e.printStackTrace()
           null
        }
    }

    init {
        previousTime = System.currentTimeMillis()
    }
}