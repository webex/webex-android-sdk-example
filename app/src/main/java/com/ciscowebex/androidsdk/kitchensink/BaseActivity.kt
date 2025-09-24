package com.ciscowebex.androidsdk.kitchensink

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.search.SearchActivity
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.PermissionsHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseActivity : AppCompatActivity() {
    var tag = "BaseActivity"
    private val permissionsHelper: PermissionsHelper by inject()
    val webexViewModel: WebexViewModel by viewModel()
    protected var isForeground: Boolean = false

    private val callingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val allGranted = grants.values.all { it }
            if (allGranted) {
                webexViewModel.retryPendingDialIfAny()
                webexViewModel.retryPendingAnswerIfAny()
            } else {
                Toast.makeText(this, getString(R.string.permission_error), Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webexViewModel.authLiveData.observe(this@BaseActivity, Observer {
            if (it != null && it == Constants.Callbacks.RE_LOGIN_REQUIRED) {
                Log.d(tag, "onReAuthRequired Re login is required by user.")
                onSignedOut()
            }
        })

        // Centralized permission handling for all Activities extending BaseActivity
        webexViewModel.callingLiveData.observe(this@BaseActivity) { live ->
            live?.let {
                val missing = it.missingPermissions
                if (!missing.isNullOrEmpty()) {
                    val normalized = normalizePermissionsForApi(missing.toSet()).toTypedArray()
                    Toast.makeText(this, "Missing permissions: ${normalized.joinToString()}", Toast.LENGTH_LONG).show()
                    callingPermissionLauncher.launch(normalized)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isForeground = true
    }

    override fun onPause() {
        isForeground = false
        super.onPause()
    }

    private fun normalizePermissionsForApi(perms: Set<String>): Set<String> {
        if (Build.VERSION.SDK_INT >= 31) {
            val mapped = perms.map {
                if (it == android.Manifest.permission.BLUETOOTH) android.Manifest.permission.BLUETOOTH_CONNECT else it
            }
            return mapped.toSet()
        }
        return perms
    }

    fun onSignedOut() {
        clearLoginTypePref(this)
        (application as KitchenSinkApp).unloadKoinModules()
        KitchenSinkForegroundService.stopForegroundService(this)
        openLoginActivity()
    }

    private fun clearLoginTypePref(context: Context) {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)
        pref?.let {
            it.edit().remove(Constants.Keys.LoginType).apply()
        }
    }

    // Open login activity and clear all previous activities
    private fun openLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

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
}