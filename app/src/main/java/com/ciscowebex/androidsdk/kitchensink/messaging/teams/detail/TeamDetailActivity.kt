package com.ciscowebex.androidsdk.kitchensink.messaging.teams.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityTeamDetailBinding
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.koin.android.ext.android.inject

class TeamDetailActivity : BaseActivity() {
    lateinit var binding: ActivityTeamDetailBinding

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

        DataBindingUtil.setContentView<ActivityTeamDetailBinding>(this, R.layout.activity_team_detail)
                .also { binding = it }
                .apply {
                    binding.progressLayout.visibility = View.VISIBLE

                    teamDetailViewModel.team.observe(this@TeamDetailActivity, Observer { model ->
                        model?.let {
                            progressLayout.visibility = View.GONE
                            binding.team = it
                        }
                    })
                }
    }

    override fun onResume() {
        super.onResume()
        teamDetailViewModel.getTeamById(teamId)
    }
}