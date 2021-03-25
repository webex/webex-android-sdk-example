package com.ciscowebex.androidsdk.kitchensink.person

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.*
import com.ciscowebex.androidsdk.kitchensink.messaging.composer.MessageComposerActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceActionBottomSheetFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.TeamActionBottomSheetFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.detail.TeamDetailActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership.TeamMembershipModel
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.hideKeyboard
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.isValidEmail
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.utils.EmailAddress
import kotlinx.android.synthetic.main.fragment_common.*
import org.koin.android.ext.android.inject


class PeopleFragment : Fragment() {
    private lateinit var binding: FragmentPersonBinding
    private lateinit var peopleClientAdapter: PeopleClientAdapter

    private val personViewModel : PersonViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPersonBinding.inflate(inflater, container, false).also { binding = it }.apply {
            val optionsDialogFragment = PeopleActionBottomSheetFragment (
                    { personId, _, model -> showPostMessageDialog(personId, null, model)},
                    { _, email, model -> showPostMessageDialog(null, email, model)},
                    { personId -> fetchDetailsById(personId)})

            peopleClientAdapter = PeopleClientAdapter(optionsDialogFragment, requireActivity().supportFragmentManager)

            recyclerView.adapter = peopleClientAdapter
            lifecycleOwner = this@PeopleFragment

            personViewModel.personList.observe(this@PeopleFragment.viewLifecycleOwner, Observer { list ->
                list?.let {
                    peopleClientAdapter.persons.clear()
                    peopleClientAdapter.persons.addAll(it)
                    peopleClientAdapter.notifyDataSetChanged()
                }
            })

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    progress_bar.visibility = View.VISIBLE
                    if (newText.isValidEmail()) {
                        personViewModel.getPeopleList(newText, null, null, null, resources.getInteger(R.integer.person_list_size))
                    }
                    else {
                        personViewModel.getPeopleList(null, newText, null, null, resources.getInteger(R.integer.person_list_size))
                    }
                    return false
                }

            })
        }.root
    }

    override fun onResume() {
        super.onResume()
        personViewModel.getPeopleList(null, null, null, null, resources.getInteger(R.integer.person_list_size))
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
            ContextCompat.startActivity(requireActivity(),
                    MessageComposerActivity.getIntent(requireActivity(), composerType, it, null), null)
        } ?: run {
            email?.let {
                val composerType = MessageComposerActivity.Companion.ComposerType.POST_PERSON_EMAIL
                ContextCompat.startActivity(requireActivity(),
                        MessageComposerActivity.getIntent(requireActivity(), composerType, it.toString(), null), null)
            }
        }
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
}

class PeopleClientViewHolder(private val binding: ListItemPersonBinding, private val optionsDialogFragment: PeopleActionBottomSheetFragment, private val fragmentManager: FragmentManager) : RecyclerView.ViewHolder(binding.root) {
    fun bind(person: PersonModel) {
        binding.person = person

        binding.personClientLayout.setOnLongClickListener { view ->
            optionsDialogFragment.personId = person.personId
            if (person.emails.isEmpty()) {
                optionsDialogFragment.email = EmailAddress.fromString("")
            }
            else {
                optionsDialogFragment.email = EmailAddress.fromString(person.emails[0])
            }
            optionsDialogFragment.model = person

            optionsDialogFragment.show(fragmentManager, "People Options")

            true
        }

        binding.executePendingBindings()
    }
}