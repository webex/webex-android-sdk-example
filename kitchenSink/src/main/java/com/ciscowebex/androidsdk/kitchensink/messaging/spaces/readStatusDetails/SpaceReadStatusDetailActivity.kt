package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.readStatusDetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivitySpaceReadStatusDetailBinding
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.koin.android.ext.android.inject

class SpaceReadStatusDetailActivity : BaseActivity() {

    companion object {
        fun getIntent(context: Context, spaceId: String) : Intent {
            val intent = Intent(context, SpaceReadStatusDetailActivity::class.java)
            intent.putExtra(Constants.Intent.SPACE_ID, spaceId)
            return intent
        }
    }

    private val spaceReadStatusDetailViewModel : SpaceReadStatusDetailViewModel by inject()
    private lateinit var spaceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        spaceId = intent.getStringExtra(Constants.Intent.SPACE_ID) ?: ""

        DataBindingUtil.setContentView<ActivitySpaceReadStatusDetailBinding>(this, R.layout.activity_space_read_status_detail)
                .apply {
                    progressLayout.visibility = View.VISIBLE

                    spaceReadStatusDetailViewModel.spaceReadStatus.observe(this@SpaceReadStatusDetailActivity, Observer { model ->
                        model?.let {
                            progressLayout.visibility = View.GONE
                            spaceReadStatus = it
                        }
                    })
                }
    }

    override fun onResume() {
        super.onResume()
        spaceReadStatusDetailViewModel.getSpaceReadStatusById(spaceId)
    }
}