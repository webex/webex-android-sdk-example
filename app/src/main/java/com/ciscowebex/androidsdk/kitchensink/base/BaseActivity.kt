package com.ciscowebex.androidsdk.kitchensink.base

import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ciscowebex.androidsdk.kitchensink.base.R
import com.ciscowebex.androidsdk.kitchensink.base.utils.PermissionsHelper
import com.google.android.play.core.splitcompat.SplitCompat
import org.koin.android.ext.android.inject

/**
 * Base activity class that provides SplitCompat support for dynamic feature modules.
 * All activities in the app should extend this class to ensure proper dynamic feature support.
 */
open class BaseActivity : AppCompatActivity() {
    open var tag = "BaseActivity"
    protected val permissionsHelper: PermissionsHelper by inject()

    /**
     * Shows an error dialog with the given message.
     */
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

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        // Enable SplitCompat for dynamic feature module support in activities
        SplitCompat.installActivity(this)
    }
}
