package com.ciscowebex.androidsdk.kitchensink.person

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogCreatePersonBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentPersonBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemPersonBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.composer.MessageComposerActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipViewModel
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.isValidEmail
import com.ciscowebex.androidsdk.kitchensink.utils.getCurrentDate
import com.ciscowebex.androidsdk.kitchensink.utils.stateToDrawable
import com.ciscowebex.androidsdk.kitchensink.utils.stateToString
import com.ciscowebex.androidsdk.people.PersonRole
import com.ciscowebex.androidsdk.people.PresenceStatus
import com.ciscowebex.androidsdk.utils.EmailAddress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject


class PeopleFragment : Fragment() {
    private lateinit var binding: FragmentPersonBinding
    private lateinit var peopleClientAdapter: PeopleClientAdapter

    private val personViewModel: PersonViewModel by inject()
    private val membershipViewModel: MembershipViewModel by inject()
    private val commaDelimiter = ","

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPersonBinding.inflate(inflater, container, false).also { binding = it }.apply {
            val optionsDialogFragment = PeopleActionBottomSheetFragment(
                { personId, _, model -> showPostMessageDialog(personId, null, model) },
                { _, email, model -> showPostMessageDialog(null, email, model) },
                { personId -> fetchDetailsById(personId) },
                { personId, model -> updatePersonDialog(personId, model) },
                { personId -> deletePersonDialog(personId) })

            peopleClientAdapter = PeopleClientAdapter(optionsDialogFragment, requireActivity().supportFragmentManager)

            recyclerView.adapter = peopleClientAdapter
            lifecycleOwner = this@PeopleFragment

            membershipViewModel.presenceChangeLiveData.observe(this@PeopleFragment.viewLifecycleOwner) {
                val position = peopleClientAdapter.getPositionById(it.getContactId())
                if(position >= 0) {
                    peopleClientAdapter.persons[position].apply {
                        presenceStatusDrawable = stateToDrawable(
                            this@PeopleFragment.requireContext(),
                            it.getStatus()
                        )
                        presenceStatusText = stateToString(
                            this@PeopleFragment.requireContext(),
                            it.getStatus()
                        )

                        if (PresenceStatus.Inactive == it.getStatus()) {
                            if (it.getLastActiveTime() > 0) {
                                presenceStatusText =
                                    presenceStatusText + " | last seen: " + getCurrentDate(
                                        it.getLastActiveTime()
                                    )
                            }
                        } else {
                            if (it.getExpiresTime() > 0) {
                                presenceStatusText =
                                    presenceStatusText + " | till: " + getCurrentDate(it.getExpiresTime())
                            }
                        }

                        if (!it.getCustomStatus().isNullOrEmpty()) {
                            presenceStatusText =
                                presenceStatusText + " | " + it.getCustomStatus()
                        }
                    }
                    peopleClientAdapter.notifyItemChanged(position)
                }
            }

            personViewModel.personList.observe(this@PeopleFragment.viewLifecycleOwner, Observer { list ->
                list?.let {
                    peopleClientAdapter.persons.clear()
                    peopleClientAdapter.persons.addAll(it)
                    peopleClientAdapter.notifyDataSetChanged()

                    membershipViewModel.stopWatchingPresence()
                    val personList = peopleClientAdapter.persons.map { data -> data.personId }
                    // Posting on main thread as, previous stop call was also on main thread. So putting
                    //  start watching to the last in main thread task queue
                    Handler(Looper.getMainLooper()).post(){
                        membershipViewModel.startWatchingPresence(personList)
                    }
                }
            })

            personViewModel.error.observe(this@PeopleFragment.viewLifecycleOwner) { errorText ->
                Toast.makeText(requireContext(), errorText, Toast.LENGTH_SHORT).show()
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isValidEmail()) {
                        personViewModel.getPeopleList(newText, null, null, null, resources.getInteger(R.integer.person_list_size))
                    } else {
                        personViewModel.getPeopleList(null, newText, null, null, resources.getInteger(R.integer.person_list_size))
                    }
                    return false
                }

            })

            addPersonsFAB.setOnClickListener {
                createPersonDialog()
            }
        }.root
    }

    override fun onResume() {
        super.onResume()
        personViewModel.getPeopleList(null, null, null, null, resources.getInteger(R.integer.person_list_size))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        membershipViewModel.stopWatchingPresence()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun fetchDetailsById(personId: String) {
        PersonDialogFragment.newInstance(personId).show(childFragmentManager, getString(R.string.person_detail))
    }

    private fun showPostMessageDialog(id: String?, email: EmailAddress?, model: PersonModel) {
        id?.let {
            val composerType = MessageComposerActivity.Companion.ComposerType.POST_PERSON_ID
            ContextCompat.startActivity(
                requireActivity(),
                MessageComposerActivity.getIntent(requireActivity(), composerType, it, null), null
            )
        } ?: run {
            email?.let {
                val composerType = MessageComposerActivity.Companion.ComposerType.POST_PERSON_EMAIL
                ContextCompat.startActivity(
                    requireActivity(),
                    MessageComposerActivity.getIntent(requireActivity(), composerType, it.toString(), null), null
                )
            }
        }
    }

    private fun createPersonDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        DialogCreatePersonBinding.inflate(layoutInflater)
            .apply {
                builder.setView(this.root)
                builder.setTitle(getString(R.string.create_person))
                builder.setPositiveButton(getString(R.string.create)) { dialog, _ ->
                    createOrUpdatePerson(TaskType.CREATE, this, null, null)
                    dialog.dismiss()
                }
                builder.show()
            }
    }

    private fun updatePersonDialog(personId: String, model: PersonModel) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        DialogCreatePersonBinding.inflate(layoutInflater)
            .apply {
                builder.setView(this.root)
                builder.setTitle(getString(R.string.update_person))
                licensesEditText.inputType = InputType.TYPE_NULL
                orgIdEditText.inputType = InputType.TYPE_NULL

                model.let { person ->
                    emailEditText.text = Editable.Factory.getInstance().newEditable(person.emails[0])
                    displayNameEditText.text = Editable.Factory.getInstance().newEditable(person.displayName)
                    firstNameEditText.text = Editable.Factory.getInstance().newEditable(person.firstName)
                    lastNameEditText.text = Editable.Factory.getInstance().newEditable(person.lastName)
                    avatarEditText.text = Editable.Factory.getInstance().newEditable(person.avatar)
                    orgIdEditText.text = Editable.Factory.getInstance().newEditable(person.orgId)
                    rolesEditText.text = Editable.Factory.getInstance().newEditable(formatListToString(person.roles.map { it.name }))
                    licensesEditText.text = Editable.Factory.getInstance().newEditable(formatListToString(person.licenses))
                    siteUrlsEditText.text = Editable.Factory.getInstance().newEditable(formatListToString(person.siteUrls))
                }

                builder.setPositiveButton(getString(R.string.update)) { dialog, _ ->
                    createOrUpdatePerson(TaskType.UPDATE, this, personId, model)
                    dialog.dismiss()
                }
                builder.show()
            }
    }

    private fun formatListToString(list: List<String>): String {
        var resultStr = ""
        for(item in list) {
            resultStr += if (resultStr.isNotEmpty()) ",$item" else item
        }
        return resultStr
    }

    private enum class TaskType {
        CREATE,
        UPDATE
    }

    private fun createOrUpdatePerson(taskType: TaskType, viewBinding: ViewBinding, personId: String?, model: PersonModel?) {
        if (viewBinding is DialogCreatePersonBinding) {
            viewBinding.apply {
                val email = emailEditText.text.toString()
                val displayName = displayNameEditText.text.toString()
                val firstName = firstNameEditText.text.toString()
                val lastName = lastNameEditText.text.toString()
                val avatar = avatarEditText.text.toString()
                val avatarStr = avatar.ifEmpty { null }
                val role = rolesEditText.text.toString()
                val roles = role.split(commaDelimiter)
                val finalRoles = mutableListOf<PersonRole>()
                for (i in roles.indices) {
                    PersonRole.values().forEach {
                        if (it.toString().lowercase() == roles[i].lowercase()) {
                            finalRoles.add(it)
                        }
                    }
                }

                val licensesText = licensesEditText.text.toString()

                val siteUrl = siteUrlsEditText.text.toString()
                val siteUrls = if (siteUrl.isNotEmpty()) siteUrl.split(commaDelimiter) else emptyList()
                if (taskType == TaskType.CREATE) {
                    val licenses = if (licensesText.isEmpty()) emptyList() else licensesText.split(commaDelimiter)
                    val orgId = orgIdEditText.text.toString()

                    personViewModel.createPerson(email, displayName, firstName, lastName, avatarStr, orgId, finalRoles, licenses, siteUrls)
                } else {
                    val licenses = model?.licenses ?: emptyList()
                    val orgId = model?.orgId.orEmpty()
                    personViewModel.updatePerson(personId.orEmpty(), email, displayName, firstName, lastName, avatarStr, orgId, finalRoles, licenses, siteUrls)
                }
            }
        }
    }

    private fun deletePersonDialog(personId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert").setMessage(getString(R.string.want_to_delete_the_person))
            .setNegativeButton("No") { _, _ ->

            }
            .setPositiveButton("Yes") { _, _ ->
                personViewModel.deletePerson(personId)
            }
            .show()
    }
}

class PeopleClientAdapter(private val optionsDialogFragment: PeopleActionBottomSheetFragment, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<PeopleClientViewHolder>() {
    var persons: MutableList<PersonModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleClientViewHolder {
        return PeopleClientViewHolder(ListItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false), optionsDialogFragment, fragmentManager)
    }

    override fun getItemCount(): Int = persons.size

    override fun onBindViewHolder(holder: PeopleClientViewHolder, position: Int) {
        holder.bind(persons[position])
    }

    fun getPositionById(personId: String): Int {
        return persons.indexOfFirst { it.personId == personId }
    }
}

class PeopleClientViewHolder(private val binding: ListItemPersonBinding, private val optionsDialogFragment: PeopleActionBottomSheetFragment, private val fragmentManager: FragmentManager) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(person: PersonModel) {
        binding.person = person

        binding.personClientLayout.setOnLongClickListener { view ->
            optionsDialogFragment.personId = person.personId
            if (person.emails.isEmpty()) {
                optionsDialogFragment.email = EmailAddress.fromString("")
            } else {
                optionsDialogFragment.email = EmailAddress.fromString(person.emails[0])
            }
            optionsDialogFragment.model = person

            optionsDialogFragment.show(fragmentManager, "People Options")

            true
        }

        binding.executePendingBindings()
    }
}