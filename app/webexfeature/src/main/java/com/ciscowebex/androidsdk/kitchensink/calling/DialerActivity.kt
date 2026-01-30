package com.ciscowebex.androidsdk.kitchensink.calling

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ciscowebex.androidsdk.kitchensink.R
import com.google.android.play.core.splitcompat.SplitCompat

class DialerActivity : AppCompatActivity(){

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }
    private lateinit var toolbar: Toolbar

    companion object{
        const val IS_ADDING_CALL = "isAddingCall"
        fun getIntent(context: Context): Intent{
            return Intent(context, DialerActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialer)
        
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setDialerFragment()
        handleNavigationClickListener()
    }

    private fun handleNavigationClickListener() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setDialerFragment() {
        val dialFragment = DialFragment()
        val bundle = Bundle()
        bundle.putBoolean(IS_ADDING_CALL, true)
        dialFragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, dialFragment)
        transaction.commit()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}