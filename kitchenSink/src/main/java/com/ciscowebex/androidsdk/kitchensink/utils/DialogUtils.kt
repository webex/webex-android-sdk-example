package com.ciscowebex.androidsdk.kitchensink.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.ciscowebex.androidsdk.kitchensink.R

fun showDialogWithMessage(context: Context, titleResourceId: Int?, message: String, positiveButtonText: Int = android.R.string.ok) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)

    builder.setTitle(titleResourceId ?: R.string.message)
    builder.setMessage(message)

    builder.setPositiveButton(positiveButtonText) { dialog, _ ->
        dialog.dismiss()
    }

    builder.show()
}

fun showDialogWithMessage(context: Context, title: String, message: String, positiveButtonText: Int = R.string.yes,
                          onPositiveButtonClick: (DialogInterface, Int) -> Unit, negativeButtonText: Int = R.string.no, onNegativeButtonClick: (DialogInterface, Int) -> Unit) {

    AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText, onPositiveButtonClick)
            .setNegativeButton(negativeButtonText, onNegativeButtonClick)
            .show()
}

