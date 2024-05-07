package com.ciscowebex.androidsdk.kitchensink.extras

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityExtrasBinding
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import org.koin.android.ext.android.inject

class ExtrasActivity : BaseActivity() {

    lateinit var binding: ActivityExtrasBinding

    private val extrasViewModel: ExtrasViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "ExtrasActivity"
        DataBindingUtil.setContentView<ActivityExtrasBinding>(this, R.layout.activity_extras)
                .also { binding = it }
                .apply {
                    btnViewAccessToken.setOnClickListener {
                        extrasViewModel.getAccessToken()
                    }
                    btnRefreshAccessToken.setOnClickListener {
                        extrasViewModel.getRefreshToken()
                    }
                    btnGetJwtTokenExpiry.setOnClickListener {
                        val expiryDate = extrasViewModel.getJwtAccessTokenExpiration()
                        val message = expiryDate?.toString()?: getString(R.string.expiry_date_not_available)
                        showDialogWithMessage(this@ExtrasActivity, R.string.access_token_expiration, message)
                    }

                    setUpObservers()
                }
    }

    private fun setUpObservers() {
        val observer: Observer<String?> = Observer {
            val accessToken = it?: getString(R.string.no_access_token_yet)
            showDialogWithMessage(this, R.string.access_token, accessToken)
        }

        extrasViewModel.accessToken.observe(this, observer)
        extrasViewModel.refreshToken.observe(this, observer)
    }
}