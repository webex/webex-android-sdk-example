package com.ciscowebex.androidsdk.kitchensink

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webexViewModel.authLiveData.observe(this@BaseActivity, Observer {
            if (it != null && it == Constants.Callbacks.RE_LOGIN_REQUIRED) {
                Log.d(tag, "onReAuthRequired Re login is required by user.")
                onSignedOut()
            }
        })
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