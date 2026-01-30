package com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.Constants

class TeamMembershipActivity : BaseActivity() {

    companion object {
        fun getIntent(context: Context, teamId: String): Intent {
            val intent = Intent(context, TeamMembershipActivity::class.java)
            intent.putExtra(Constants.Intent.TEAM_ID, teamId)
            return intent
        }
    }

    private lateinit var teamId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teamId = intent.getStringExtra(Constants.Intent.TEAM_ID) ?: ""

        setContentView(R.layout.activity_membership)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val fragment = TeamMembershipFragment.newInstance(teamId)
        fragmentTransaction.add(R.id.membershipFragment, fragment)
        fragmentTransaction.commit()
    }
}

