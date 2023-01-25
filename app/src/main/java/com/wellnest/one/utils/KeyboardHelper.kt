package com.wellnest.one.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

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
        if (context.currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
        }
    }

    fun showKeyboard(context: Activity, edtSearch: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edtSearch, 0)
    }
}
