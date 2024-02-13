package com.ciscowebex.androidsdk.kitchensink.utils

import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.widget.EditText
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

fun showDialogWithMessage(context: Context, title: String, message: String, positiveButtonText: Int = R.string.yes, cancelable: Boolean = true,
                          onPositiveButtonClick: (DialogInterface, Int) -> Unit, negativeButtonText: Int = R.string.no, onNegativeButtonClick: (DialogInterface, Int) -> Unit) {

    AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(cancelable)
            .setPositiveButton(positiveButtonText, onPositiveButtonClick)
            .setNegativeButton(negativeButtonText, onNegativeButtonClick)
            .show()
}

fun showDialogForInputEmail(context: Context, title: String, positiveButtonText: Int = android.R.string.ok,
                       onPositiveButtonClick: (DialogInterface, String) -> Unit, negativeButtonText: Int = android.R.string.cancel,
                       onNegativeButtonClick: (DialogInterface, Int) -> Unit) {
    val input = EditText(context)
    input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    AlertDialog.Builder(context)
            .setTitle(title)
            .setView(input)
            .setPositiveButton(positiveButtonText) { dialogInterface: DialogInterface, i: Int ->
                val email = input.text.toString()
                onPositiveButtonClick(dialogInterface, email)
            }
            .setNegativeButton(negativeButtonText, onNegativeButtonClick)
            .show()
}

fun showDialogForDTMF(context: Context, title: String, positiveButtonText: Int = android.R.string.ok,
                            onPositiveButtonClick: (DialogInterface, String) -> Unit, negativeButtonText: Int = android.R.string.cancel,
                            onNegativeButtonClick: (DialogInterface, Int) -> Unit) {
    val input = EditText(context)
    AlertDialog.Builder(context)
        .setTitle(title)
        .setView(input)
        .setPositiveButton(positiveButtonText) { dialogInterface: DialogInterface, i: Int ->
            val number = input.text.toString()
            onPositiveButtonClick(dialogInterface, number)
        }
        .setNegativeButton(negativeButtonText, onNegativeButtonClick)
        .show()
}

fun showDialogForHostKey(context: Context, title: String, positiveButtonText: Int = android.R.string.ok,
                         onPositiveButtonClick: (DialogInterface, String) -> Unit, negativeButtonText: Int = android.R.string.cancel,
                         onNegativeButtonClick: (DialogInterface, Int) -> Unit) {
    val input = EditText(context)
    AlertDialog.Builder(context)
        .setTitle(title)
        .setView(input)
        .setPositiveButton(positiveButtonText) { dialogInterface: DialogInterface, i: Int ->
            val hostKey = input.text.toString()
            onPositiveButtonClick(dialogInterface, hostKey)
        }
        .setNegativeButton(negativeButtonText, onNegativeButtonClick)
        .show()
}

fun showDialogForTextBox(context: Context, title: String, positiveButtonText: Int = android.R.string.ok,
                         onPositiveButtonClick: (DialogInterface, String) -> Unit, negativeButtonText: Int = android.R.string.cancel,
                         onNegativeButtonClick: (DialogInterface, Int) -> Unit) {
    val input = EditText(context)
    AlertDialog.Builder(context)
        .setTitle(title)
        .setView(input)
        .setPositiveButton(positiveButtonText) { dialogInterface: DialogInterface, i: Int ->
            val text = input.text.toString()
            onPositiveButtonClick(dialogInterface, text)
        }
        .setNegativeButton(negativeButtonText, onNegativeButtonClick)
        .show()
}

