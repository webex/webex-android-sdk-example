package com.ciscowebex.androidsdk.kitchensink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.search.SearchActivity
import com.ciscowebex.androidsdk.kitchensink.utils.PermissionsHelper
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

open class BaseActivity : AppCompatActivity() {
    var tag = "BaseActivity"
    private var outgoingCallPermissionCheck = false
    private val permissionsHelper: PermissionsHelper by inject()
    val webexViewModel: WebexViewModel by viewModel()

    fun showErrorDialog(errorMessage: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.error_occurred)
        val message = TextView(this)
        message.setPadding(10, 10, 10, 10)
        message.text = errorMessage

        builder.setView(message)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    protected fun checkCallingFeature(incomingCall: Boolean) {
        val status = checkCallPermissions()

        outgoingCallPermissionCheck = false

        if (!status) {
            startCallingActivities(incomingCall)
        } else {
            if (!incomingCall) {
                outgoingCallPermissionCheck = true
            }
        }
    }

    private fun checkCallPermissions(): Boolean {
        if (!permissionsHelper.hasCameraPermission() || !permissionsHelper.hasMicrophonePermission() || !permissionsHelper.hasPhoneStatePermission()) {
            Log.d(tag, "HomeActivity requesting call permissions")
            requestPermissions(PermissionsHelper.permissionsForCalling(), PermissionsHelper.PERMISSIONS_CALLING_REQUEST)
            return true
        } else {
            Log.d(tag, "HomeActivity all call permissions granted")
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PermissionsHelper.PERMISSIONS_CALLING_REQUEST -> {
                if (PermissionsHelper.resultForCallingPermissions(permissions, grantResults)) {
                    Log.d(tag, "HomeActivity all call permissions granted")
                    startCallingActivities(!outgoingCallPermissionCheck)
                } else {
                    checkCallingFeature(!outgoingCallPermissionCheck)
                }
            }
        }
    }

    private fun startCallingActivities(incomingCall: Boolean) {
        if (incomingCall) {
            startActivity(CallActivity.getIncomingIntent(this))
        } else {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }
}