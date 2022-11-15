package com.wellnest.one.utils

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import com.wellnest.one.R

/**
 * Created by Hussain on 07/11/22.
 */
object ProgressHelper {
    var mDialog: Dialog? = null

    /*progress dialog*/
    fun showDialog(context: Activity) {
        try {
            if (!context.isFinishing) {
                mDialog = Dialog(context)
                mDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                mDialog!!.setCancelable(false)
                mDialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                mDialog!!.window!!.setBackgroundDrawableResource(R.color.white_film)
                mDialog!!.setContentView(R.layout.dialog_progress)
                mDialog!!.window!!
                    .setLayout(
                        ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT
                    )
                mDialog!!.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } // custom

    /* dismissDialog is used for close progressDialog */
    fun dismissDialog() {
        try {
            if (mDialog != null && mDialog!!.isShowing) {
                mDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}