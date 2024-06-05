package com.ciscowebex.androidsdk.kitchensink.messaging.teams

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogCreateSpaceBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentTeamsBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemTeamsClientBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.search.MessagingSearchActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.AddPersonBottomSheetFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.detail.TeamDetailActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership.TeamMembershipActivity
import com.ciscowebex.androidsdk.kitchensink.person.PersonModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.koin.android.ext.android.inject


class TeamsFragment : Fragment() {
    private lateinit var binding: FragmentTeamsBinding
    private lateinit var teamsClientAdapter: TeamsClientAdapter

    private val teamsViewModel: TeamsViewModel by inject()
    private val TAG = TeamsFragment::class.java.name
    private val requestCodeSearchPersonToAddToTeam = 31321
    private var selectedTeamListItem: TeamModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentTeamsBinding.inflate(inflater, container, false).also { binding = it }.apply {
            val optionsDialogFragment = TeamActionBottomSheetFragment(
                    { id, title -> showEditTeamDialog(id, title) },
                    { id -> showAddSpaceDialog(id) },
                    { id, title -> showDeleteTeamConfirmationDialog(id, title) },
                    { id -> showMembers(id) }, { item ->
                    selectedTeamListItem = item
                    startActivityForResult(context?.let { MessagingSearchActivity.getIntent(it) }, requestCodeSearchPersonToAddToTeam)
                })
            teamsClientAdapter = TeamsClientAdapter(optionsDialogFragment)

            teamsRecyclerView.adapter = teamsClientAdapter
            lifecycleOwner = this@TeamsFragment

            swipeContainer.setOnRefreshListener {
                teamsViewModel.getTeamsList(Constants.DefaultMax.TEAM_MAX)
            }

            teamsViewModel.teams.observe(this@TeamsFragment.viewLifecycleOwner, Observer { list ->
                list?.let {
                    swipeContainer.isRefreshing = false

                    teamsClientAdapter.teams.clear()
                    teamsClientAdapter.teams.addAll(it)
                    teamsClientAdapter.notifyDataSetChanged()
                }
            })

            teamsViewModel.teamAdded.observe(this@TeamsFragment.viewLifecycleOwner, Observer { model ->
                model?.let {
                    teamsClientAdapter.teams.add(it)
                    teamsClientAdapter.notifyDataSetChanged()
                }
            })

            teamsViewModel.teamError.observe(this@TeamsFragment.viewLifecycleOwner, Observer { error ->
                error?.let {
                    showErrorDialog(it)
                }
            })

            addTeamsFAB.setOnClickListener {
                showAddTeamDialog()
            }

        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamsViewModel.getTeamsList(Constants.DefaultMax.TEAM_MAX)
    }

    private fun showAddTeamDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.add_team)
        val input = EditText(requireContext())
        input.hint = getString(R.string.team_name_hint)
        input.requestFocus()

        builder.setView(input)

        builder.setPositiveButton(android.R.string.ok) { _, _ -> teamsViewModel.addTeam(input.text.toString()) }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showErrorDialog(errorMessage: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.error_occurred)
        val message = TextView(requireContext())
        message.setPadding(10, 10, 10, 10)
        message.text = errorMessage

        builder.setView(message)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showEditTeamDialog(teamID: String, title: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.edit_team)
        val input = EditText(requireContext())
        input.text = SpannableStringBuilder(title)
        input.requestFocus()

        builder.setView(input)

        builder.setPositiveButton(android.R.string.ok) { _, _ -> teamsViewModel.updateTeam(teamID, input.text.toString()) }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showAddSpaceDialog(teamId: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.add_space)

        DialogCreateSpaceBinding.inflate(layoutInflater)
                .apply {
                    spaceTeamIdText.text = teamId

                    spaceTitleEditText.requestFocus()

                    builder.setView(this.root)
                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        teamsViewModel.addSpaceFromTeam(spaceTitleEditText.text.toString(), teamId)
                    }
                    builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

                    builder.show()
                }
    }

    private fun showMembers(teamId: String) {
        startActivity(TeamMembershipActivity.getIntent(requireContext(), teamId))
    }

    private fun showDeleteTeamConfirmationDialog(teamId: String, teamTitle: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.delete_team_confirm)
        val message = TextView(requireContext())
        message.setPadding(10, 10, 10, 10)
        message.text = String.format(getString(R.string.delete_team_message), teamTitle)

        builder.setView(message)

        builder.setPositiveButton(android.R.string.ok) { _, _ -> teamsViewModel.deleteTeamWithId(teamId) }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleActivityResult(requestCode, resultCode, data)
    }

    private fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == requestCodeSearchPersonToAddToTeam && resultCode == Activity.RESULT_OK) {
            val person = data?.getParcelableExtra<PersonModel>(Constants.Intent.PERSON)
            if (person != null) {
                showAddMembersOptionDialog(person)
            } else {
                Log.d(TAG, "Person data is null ")
            }

        } else {
            Log.d(TAG, "Person could not be found!")
        }
    }

    private fun showAddMembersOptionDialog(person: PersonModel) {
        val addMembersOptionDialog = AddPersonBottomSheetFragment { option ->
            when (option) {
                AddPersonBottomSheetFragment.Companion.Options.ADD_BY_PERSON_ID -> selectedTeamListItem?.id?.let {
                    teamsViewModel.createMembershipWithId(it, person.personId, false)
                }
                AddPersonBottomSheetFragment.Companion.Options.ADD_BY_EMAIL_ID -> selectedTeamListItem?.id?.let {
                    teamsViewModel.createMembershipWithEmailId(it, person.emails.first(), false)
                }
            }
        }
        activity?.supportFragmentManager?.let { addMembersOptionDialog.show(it, AddPersonBottomSheetFragment.TAG) }
    }
}

class TeamsClientAdapter(private val optionsDialogFragment: TeamActionBottomSheetFragment) : RecyclerView.Adapter<TeamsClientViewHolder>() {
    var teams: MutableList<TeamModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamsClientViewHolder {
        return TeamsClientViewHolder(ListItemTeamsClientBinding.inflate(LayoutInflater.from(parent.context), parent, false), optionsDialogFragment)
    }

    override fun getItemCount(): Int = teams.size

    override fun onBindViewHolder(holder: TeamsClientViewHolder, position: Int) {
        holder.bind(teams[position])
    }
}

class TeamsClientViewHolder(private val binding: ListItemTeamsClientBinding, private val optionsDialogFragment: TeamActionBottomSheetFragment) : RecyclerView.ViewHolder(binding.root) {

    fun bind(team: TeamModel) {
        binding.team = team

        binding.teamsClientLayout.setOnClickListener { view ->
            ContextCompat.startActivity(view.context, TeamDetailActivity.getIntent(view.context, team.id), null)
        }

        binding.teamsClientLayout.setOnLongClickListener { view ->
            optionsDialogFragment.teamId = team.id
            optionsDialogFragment.teamTitle = team.name
            optionsDialogFragment.team = team
            val activity = view.context as AppCompatActivity
            activity.supportFragmentManager.let { optionsDialogFragment.show(it, "Team Options") }

            true
        }

        binding.executePendingBindings()
    }
}