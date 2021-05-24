package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogCreateSpaceBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentSpacesBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.search.MessagingSearchActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.adapters.SpaceReadStatusClientAdapter
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.adapters.SpacesClientAdapter
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.membersReadStatus.MembershipReadStatusActivity
import com.ciscowebex.androidsdk.kitchensink.person.PersonModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.space.Space
import org.koin.android.ext.android.inject

class SpacesFragment : Fragment() {
    private val TAG = SpacesFragment::class.java.simpleName
    private val requestCodeSearchPersonToAddToSpace = 1919
    private lateinit var binding: FragmentSpacesBinding
    private lateinit var spacesClientAdapter: SpacesClientAdapter
    private val spacesReadClientAdapter: SpaceReadStatusClientAdapter = SpaceReadStatusClientAdapter()

    private val spacesViewModel: SpacesViewModel by inject()

    private var selectedSpaceListItem: SpaceModel? = null
    private val addOnCallSuffix = "(On Call)"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentSpacesBinding.inflate(inflater, container, false).also { binding = it }.apply {
            val optionsDialogFragment = SpaceActionBottomSheetFragment({ id, title -> showEditSpaceDialog(id, title) }, { id -> spacesViewModel.getMeetingInfo(id) },
                    { id -> showMembersInSpace(id) }, { id, title -> showDeleteSpaceConfirmationDialog(id, title) }, { id -> markSpaceRead(id) }, { id -> showSpaceMembersWithReadStatus(id) })

            spacesClientAdapter = SpacesClientAdapter(optionsDialogFragment, requireActivity().supportFragmentManager) { listItem ->
                selectedSpaceListItem = listItem
                startActivityForResult(context?.let { MessagingSearchActivity.getIntent(it) }, requestCodeSearchPersonToAddToSpace)
            }

            setHasOptionsMenu(true)

            swipeContainer.setOnRefreshListener {
                spacesViewModel.getSpacesList(resources.getInteger(R.integer.space_list_size))
            }

            setUpObservers()

            addSpacesFAB.setOnClickListener {
                showAddSpaceDialog()
            }

            spacesViewModel.getSpaceEvent()?.observe(viewLifecycleOwner, Observer {
                when (it.first) {
                    WebexRepository.SpaceEvent.Updated -> {
                        if (it.second is Space) {
                            Log.d(TAG, "Space event ${(it.second as Space).title} is updated")
                            val space = SpaceModel.convertToSpaceModel(it.second as Space?)
                            val index = spacesClientAdapter.getPositionById(space.id)
                            if (!spacesClientAdapter.spaces.isNullOrEmpty() && index != -1) {
                                spacesClientAdapter.spaces[index] = space
                                spacesClientAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    WebexRepository.SpaceEvent.Created -> {
                        if (it.second is Space) {
                            val space = SpaceModel.convertToSpaceModel(it.second as Space?)
                            spacesClientAdapter.spaces.add(0, space)
                            spacesClientAdapter.notifyItemInserted(0)
                            Log.d(TAG, "Space event ${(it.second as Space).title} is created")
                        }
                    }
                    WebexRepository.SpaceEvent.CallStarted -> {
                        if (it.second is String?) {
                            val spaceId = it.second as String?
                            spaceId?.let {
                                val index = spacesClientAdapter.getPositionById(it)
                                if (!spacesClientAdapter.spaces.isNullOrEmpty() && index != -1) {
                                    val space = spacesClientAdapter.spaces[index]
                                    Log.d(TAG, "Space event ${space} is CallStarted")
                                    val inCallSpace = SpaceModel(spaceId, space.title + " " + addOnCallSuffix, space.spaceType, space.isLocked, space.lastActivity, space.created, space.teamId, space.sipAddress)
                                    spacesClientAdapter.spaces[index] = inCallSpace
                                    spacesClientAdapter.notifyItemChanged(index)
                                }
                            }
                        }
                    }
                    WebexRepository.SpaceEvent.CallEnded -> {
                        if (it.second is String?) {
                            val spaceId = it.second as String?
                            spaceId?.let {
                                val index = spacesClientAdapter.getPositionById(it)
                                if (!spacesClientAdapter.spaces.isNullOrEmpty() && index != -1) {
                                    val space = spacesClientAdapter.spaces[index]
                                    Log.d(TAG, "Space event ${space.title} is CallEnded")
                                    val inCallSpace = SpaceModel(spaceId, space.title.removeSuffix(addOnCallSuffix), space.spaceType, space.isLocked, space.lastActivity, space.created, space.teamId, space.sipAddress)
                                    spacesClientAdapter.spaces[index] = inCallSpace
                                    spacesClientAdapter.notifyItemChanged(index)
                                }
                            }
                        }
                    }
                }
            })
        }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.messaging_menu, menu)
        menu.getItem(0).isChecked = binding.spacesRecyclerView.adapter is SpaceReadStatusClientAdapter

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = !item.isChecked
        if (item.isChecked) {
            binding.spacesRecyclerView.adapter = spacesReadClientAdapter
        } else {
            binding.spacesRecyclerView.adapter = spacesClientAdapter
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val maxSpaces = resources.getInteger(R.integer.space_list_size)
        binding.spacesRecyclerView.adapter = spacesClientAdapter
        spacesViewModel.getSpacesList(maxSpaces)
        spacesViewModel.getSpaceReadStatusList(maxSpaces)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeSearchPersonToAddToSpace && resultCode == Activity.RESULT_OK) {
            val person = data?.getParcelableExtra<PersonModel>(Constants.Intent.PERSON)
            if (person != null) {
                showAddMembersOptionDialog(person)
            } else {
                Log.d(TAG, "No person selected!")
            }

        } else {
            Log.d(TAG, "Person could not be found!")
        }
    }

    private fun setUpObservers() {
        spacesViewModel.readStatusList.observe(this@SpacesFragment.viewLifecycleOwner, Observer { list ->
            list?.let {
                spacesReadClientAdapter.spaceReadStatusList = it
                spacesReadClientAdapter.notifyDataSetChanged()
            }
        })

        spacesViewModel.spaces.observe(this@SpacesFragment.viewLifecycleOwner, Observer { spaces ->
            spaces?.let {
                binding.swipeContainer.isRefreshing = false

                spacesClientAdapter.spaces.clear()
                spacesClientAdapter.spaces.addAll(it)
                spacesClientAdapter.notifyDataSetChanged()
            }
        })

        spacesViewModel.addSpace.observe(this@SpacesFragment.viewLifecycleOwner, Observer { addspace ->
            addspace?.let {
                spacesClientAdapter.spaces.add(it)
                spacesClientAdapter.notifyDataSetChanged()
            }
        })

        spacesViewModel.spaceMeetingInfo.observe(this@SpacesFragment.viewLifecycleOwner, Observer { info ->
            info?.let {
                showGetMeetingInfoDialog(it)
            }
        })

        spacesViewModel.spaceError.observe(this@SpacesFragment.viewLifecycleOwner, Observer { error ->
            error?.let {
                binding.progressLayout.visibility = View.GONE
                showDialogWithMessage(requireContext(), R.string.error_occurred, it)
            }
        })

        spacesViewModel.createMemberData.observe(this@SpacesFragment.viewLifecycleOwner, Observer { data ->
            data?.let {
                binding.progressLayout.visibility = View.GONE
                val message = "${it.personDisplayName} added to ${it.spaceId}"
                showDialogWithMessage(requireContext(), R.string.success, message)
            }
        })

        spacesViewModel.markSpaceRead.observe(this@SpacesFragment.viewLifecycleOwner, Observer {
            binding.progressLayout.visibility = View.GONE
            showDialogWithMessage(requireContext(), R.string.success, getString(R.string.space_marked_as_read))
        })

        spacesViewModel.deleteSpace.observe(this@SpacesFragment.viewLifecycleOwner, Observer { spaceId ->
            spaceId?.let {
                binding.progressLayout.visibility = View.GONE
                val index = spacesClientAdapter.getPositionById(it)
                spacesClientAdapter.spaces.removeAt(index)
                spacesClientAdapter.notifyItemRemoved(index)
            }
        })
    }

    // Dialog to display various options of adding person to space
    private fun showAddMembersOptionDialog(person: PersonModel) {
        val addMembersOptionDialog = AddPersonBottomSheetFragment { option ->
            when (option) {
                AddPersonBottomSheetFragment.Companion.Options.ADD_BY_PERSON_ID -> selectedSpaceListItem?.id?.let {
                    binding.progressLayout.visibility = View.VISIBLE
                    spacesViewModel.createMembershipWithId(it, person.personId)
                }
                AddPersonBottomSheetFragment.Companion.Options.ADD_BY_EMAIL_ID -> selectedSpaceListItem?.id?.let {
                    binding.progressLayout.visibility = View.VISIBLE
                    spacesViewModel.createMembershipWithEmailId(it, person.emails.first())
                }
            }
        }
        activity?.supportFragmentManager?.let { addMembersOptionDialog.show(it, AddPersonBottomSheetFragment.TAG) }
    }

    private fun showGetMeetingInfoDialog(spaceMeetingInfoModel: SpaceMeetingInfoModel) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.meeting_info)
        val message = TextView(requireContext())
        message.setPadding(10, 10, 10, 10)
        message.text = spaceMeetingInfoModel.toString()

        builder.setView(message)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showEditSpaceDialog(spaceId: String, title: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.edit_space)
        val input = EditText(requireContext())
        input.text = SpannableStringBuilder(title)
        input.requestFocus()

        builder.setView(input)

        builder.setPositiveButton(android.R.string.ok) { _, _ -> spacesViewModel.updateSpace(spaceId, input.text.toString()) }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showAddSpaceDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.add_space)

        DialogCreateSpaceBinding.inflate(layoutInflater)
                .apply {
                    spaceTeamIdText.visibility = View.GONE
                    spaceTeamIdLabel.visibility = View.GONE


                    builder.setView(this.root)
                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        spacesViewModel.addSpace(spaceTitleEditText.text.toString(), null)
                    }
                    builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

                    builder.show()
                }
    }

    private fun showDeleteSpaceConfirmationDialog(spaceId: String, spaceTitle: String) {
        showDialogWithMessage(requireContext(), getString(R.string.delete_space), String.format(getString(R.string.delete_space_message, spaceTitle)),
                onPositiveButtonClick = { dialog, _ ->
                    dialog.dismiss()
                    binding.progressLayout.visibility = View.VISIBLE
                    spacesViewModel.delete(spaceId)
                },
                onNegativeButtonClick = { dialog, _ ->
                    dialog.dismiss()
                })
    }

    private fun showMembersInSpace(spaceId: String) {
        startActivity(MembershipActivity.getIntent(requireContext(), spaceId))
    }

    private fun showSpaceMembersWithReadStatus(spaceId: String) {
        startActivity(MembershipReadStatusActivity.getIntent(requireContext(), spaceId))
    }

    private fun markSpaceRead(spaceId: String) {
        binding.progressLayout.visibility = View.VISIBLE
        spacesViewModel.markSpaceRead(spaceId)
    }
}