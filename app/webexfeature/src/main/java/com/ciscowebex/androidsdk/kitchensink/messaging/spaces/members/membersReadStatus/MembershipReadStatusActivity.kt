package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.membersReadStatus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.Constants

class MembershipReadStatusActivity : BaseActivity() {

    companion object {
        fun getIntent(context: Context, spaceId: String): Intent {
            val intent = Intent(context, MembershipReadStatusActivity::class.java)
            intent.putExtra(Constants.Intent.SPACE_ID, spaceId)
            return intent
        }
    }

    private lateinit var spaceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spaceId = intent.getStringExtra(Constants.Intent.SPACE_ID) ?: ""

        setContentView(R.layout.activity_membership_read_status)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val fragment = MembershipReadStatusFragment.newInstance(spaceId)
        fragmentTransaction.add(R.id.fragment, fragment)
        fragmentTransaction.commit()
    }
}