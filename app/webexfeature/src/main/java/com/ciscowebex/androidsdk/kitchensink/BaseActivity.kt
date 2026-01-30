package com.ciscowebex.androidsdk.kitchensink

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity
import com.ciscowebex.androidsdk.kitchensink.base.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.base.KitchenSinkForegroundService
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.ciscowebex.androidsdk.kitchensink.base.BaseActivity as BaseBaseActivity

/**
 * Feature module's BaseActivity that extends the base module's BaseActivity.
 * Adds WebexViewModel and permission handling specific to the Webex feature.
 */
open class BaseActivity : BaseBaseActivity() {
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
}
