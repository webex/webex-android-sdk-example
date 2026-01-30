package com.ciscowebex.androidsdk.kitchensink.messaging.teams.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.koin.android.ext.android.inject

class TeamDetailActivity : BaseActivity() {
    private lateinit var progressLayout: RelativeLayout
    private lateinit var teamsIdTextView: TextView
    private lateinit var teamsNameTextView: TextView
    private lateinit var teamsDateCreatedTextView: TextView

    private val teamDetailViewModel : TeamDetailViewModel by inject()
    private lateinit var teamId: String

    companion object {
        fun getIntent(context: Context, teamId: String): Intent {
            val intent = Intent(context, TeamDetailActivity::class.java)
            intent.putExtra(Constants.Intent.TEAM_ID, teamId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        teamId = intent.getStringExtra(Constants.Intent.TEAM_ID) ?: ""

        setContentView(R.layout.activity_team_detail)

        progressLayout = findViewById(R.id.progressLayout)
        teamsIdTextView = findViewById(R.id.teamsIdTextView)
        teamsNameTextView = findViewById(R.id.teamsNameTextView)
        teamsDateCreatedTextView = findViewById(R.id.teamsDateCreatedTextView)

        progressLayout.visibility = View.VISIBLE

        teamDetailViewModel.team.observe(this, Observer { model ->
            model?.let {
                progressLayout.visibility = View.GONE
                teamsIdTextView.text = it.id
                teamsNameTextView.text = it.name
                teamsDateCreatedTextView.text = it.createdDateTimeString
            }
        })
    }

    override fun onResume() {
        super.onResume()
        teamDetailViewModel.getTeamById(teamId)
    }
}