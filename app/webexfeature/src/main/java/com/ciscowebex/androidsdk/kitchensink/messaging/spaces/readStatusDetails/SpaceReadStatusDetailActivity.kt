package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.readStatusDetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
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
    private lateinit var progressLayout: RelativeLayout
    private lateinit var spaceIdTextView: TextView
    private lateinit var spaceTypeTextView: TextView
    private lateinit var spaceLastSeenTextView: TextView
    private lateinit var spaceLastActivityTextView: TextView
    private lateinit var spaceUnreadIndicator: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        spaceId = intent.getStringExtra(Constants.Intent.SPACE_ID) ?: ""

        setContentView(R.layout.activity_space_read_status_detail)

        progressLayout = findViewById(R.id.progressLayout)
        spaceIdTextView = findViewById(R.id.spaceIdTextView)
        spaceTypeTextView = findViewById(R.id.spaceTypeTextView)
        spaceLastSeenTextView = findViewById(R.id.spaceLastSeenTextView)
        spaceLastActivityTextView = findViewById(R.id.spaceLastActivityTextView)
        spaceUnreadIndicator = findViewById(R.id.spaceUnreadIndicator)

        progressLayout.visibility = View.VISIBLE

        spaceReadStatusDetailViewModel.spaceReadStatus.observe(this, Observer { model ->
            model?.let {
                progressLayout.visibility = View.GONE
                spaceIdTextView.text = it.spaceId
                spaceTypeTextView.text = it.spaceTypeString
                spaceLastSeenTextView.text = it.lastSeenDateTimeString
                spaceLastActivityTextView.text = it.lastActivityTimestampString
                spaceUnreadIndicator.visibility = if (it.isSpaceUnread) View.VISIBLE else View.GONE
            }
        })
    }

    override fun onResume() {
        super.onResume()
        spaceReadStatusDetailViewModel.getSpaceReadStatusById(spaceId)
    }
}