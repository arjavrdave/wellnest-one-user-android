package com.wellnest.one.utils

import android.R
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager

/**
 * Created by Hussain on 07/11/22.
 */

object KeyboardHelper {


    fun hideKeyboard(context: Activity?) {
        if (context == null) {
            return
        }
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager != null && context.currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
        }
    }

    fun showKeyboard(context: Activity) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            ?: return
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}
