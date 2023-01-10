package com.wellnest.one.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.wellnest.one.R
import com.wellnest.one.ui.home.HomeActivity

/**
 * Created by Hussain on 16/11/22.
 */
object DialogHelper {
    @JvmOverloads
    fun showDialog(
        title: String?,
        message: String?,
        context: Context?,
        listener: DialogInterface.OnClickListener? = null
    ): AlertDialog {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        val builder = AlertDialog.Builder(context)

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
            .setTitle(title)

        // 3. Get the AlertDialog from create()
        builder.setPositiveButton("Ok", listener)
        val dialog = builder.create()
        val activity = scanForActivity(context)
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            dialog.setCancelable(false)
            dialog.show()
        }
        return dialog
    }

    fun showDialogWithCancelButton(
        title: String?,
        cancelButtonTitle: String?,
        message: String?,
        context: Context?,
        listener: DialogInterface.OnClickListener?
    ): AlertDialog {
        return showDialog(title, "Continue", cancelButtonTitle, message, context, listener)
    }

    fun showDialog(
        title: String?,
        okTitle: String?,
        cancelButtonTitle: String?,
        message: String?,
        context: Context?,
        listener: DialogInterface.OnClickListener?
    ): AlertDialog {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        val builder = AlertDialog.Builder(context)

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
            .setTitle(title)

        // 3. Get the AlertDialog from create()
        builder.setPositiveButton(okTitle, listener)
        builder.setNegativeButton(cancelButtonTitle, null)
        val dialog = builder.create()
        val activity = scanForActivity(context)
        if (activity != null && !activity.isFinishing) {
            dialog.setCancelable(false)
            dialog.show()
        }
        return dialog
    }

    fun showDialog(
        title: String?,
        okTitle: String?,
        cancelButton: String?,
        message: String?,
        context: Context?,
        listener: DialogInterface.OnClickListener?,
        cancelListener: DialogInterface.OnClickListener?
    ): AlertDialog {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        val builder = AlertDialog.Builder(context)

        // 2. Chain together various setter methods to set the dialog characteristics
        builder
            .setTitle(title)
            .setMessage(message)

        // 3. Get the AlertDialog from create()
        builder.setPositiveButton(okTitle, listener)
        builder.setNegativeButton(cancelButton, cancelListener)
        val dialog = builder.create()
        val activity = scanForActivity(context)
        if (activity != null && !activity.isFinishing) {
            dialog.setCancelable(false)
            dialog.show()
        }
        return dialog
    }

    fun showDialogWithNeutral(
        title: String?,
        okTitle: String?,
        cancelButton: String?,
        neutralButton: String?,
        message: String?,
        context: Context?,
        listener: DialogInterface.OnClickListener?,
        cancelListener: DialogInterface.OnClickListener?
    ): AlertDialog {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        val builder = AlertDialog.Builder(context)

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
            .setTitle(title)

        // 3. Get the AlertDialog from create()
        builder.setPositiveButton(okTitle, listener)
        builder.setNegativeButton(cancelButton, cancelListener)
        builder.setNeutralButton(neutralButton, null)
        val dialog = builder.create()
        val activity = scanForActivity(context)
        if (activity != null && !activity.isFinishing) {
            dialog.setCancelable(false)
            dialog.show()
        }
        return dialog
    }

    fun showsDialog(
        title: String?,
        OkButton: String?,
        message: String?,
        context: Context?,
        listener: DialogInterface.OnClickListener?
    ): AlertDialog {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        val builder = AlertDialog.Builder(context)

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
            .setTitle(title)

        // 3. Get the AlertDialog from create()
        builder.setPositiveButton(OkButton, listener)
        val dialog = builder.create()
        val activity = scanForActivity(context)
        if (activity != null && !activity.isFinishing) {
            dialog.setCancelable(false)
            dialog.show()
        }
        return dialog
    }

    private fun scanForActivity(cont: Context?): Activity? {
        if (cont == null) return null else if (cont is Activity) return cont else if (cont is ContextWrapper) return scanForActivity(
            cont.baseContext
        )
        return null
    }

    fun showSuccessDialog(context: Context?, message: String?): AlertDialog {
        val dialogView: View =
            LayoutInflater.from(context).inflate(R.layout.dialog_feedback_success, null)
        val mBuilder = AlertDialog.Builder(context, R.style.SuccessDialogTheme)
            .setView(dialogView)
            .setTitle("")
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = message
        val mAlertDialog = mBuilder.show()
        mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return mAlertDialog
    }

//    fun showTextDialog(
//        context: Context?,
//        title: String?,
//        listener: IOtherStringListener
//    ): AlertDialog {
//        val dialogView: View =
//            LayoutInflater.from(context).inflate(R.layout.item_addeditprofile_other, null)
//        val mBuilder = AlertDialog.Builder(context)
//            .setView(dialogView)
//            .setTitle("")
//        val tvMessage = dialogView.findViewById<TextView>(R.id.tvHeader)
//        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
//        val edtOther = dialogView.findViewById<EditText>(R.id.edtOtherTxt)
//        tvMessage.text = title
//        val mAlertDialog = mBuilder.show()
//        mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        btnOk.setOnClickListener { view: View? ->
//            if (edtOther.text.toString().isEmpty()) {
//                Toast.makeText(
//                    context,
//                    "Field cannot be empty.",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@setOnClickListener
//            }
//            listener.getOtherSetString(edtOther.text.toString())
//            mAlertDialog.dismiss()
//        }
//        mAlertDialog.setOnDismissListener { dialogInterface: DialogInterface? ->
//            if (edtOther.text.toString().isEmpty()) {
//                listener.getOtherSetString("Other")
//            }
//        }
//        return mAlertDialog
//    }

    fun showBluetoothDisconnectedDialog(activity: Activity?) {
        showDialog(
            "Error", "Device Disconnected", activity
        ) { _: DialogInterface?, _: Int ->
            val homeIntent = Intent(activity, HomeActivity::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity?.startActivity(homeIntent)
            activity?.finish()
        }
    }
}

