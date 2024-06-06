package com.ciscowebex.androidsdk.kitchensink.messaging.search

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentCommonBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemPersonsBinding
import com.ciscowebex.androidsdk.kitchensink.person.PersonModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import kotlinx.android.synthetic.main.fragment_common.*
import org.koin.android.ext.android.inject

class SearchPeopleFragment : Fragment() {
    private val searchPeopleViewModel: SearchPeopleViewModel by inject()
    lateinit var personAdapter: SearchPersonAdapter
    var listItemSize: Int = 0

    companion object {
        val TAG = SearchPeopleFragment::class.java.simpleName

        fun getInstance(): SearchPeopleFragment {
            return SearchPeopleFragment()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return FragmentCommonBinding.inflate(inflater, container, false).apply {
            recyclerView.itemAnimator = DefaultItemAnimator()
            personAdapter = SearchPersonAdapter { selectedPerson ->
                finishActivityAndReturnValue(selectedPerson)
            }
            recyclerView.adapter = personAdapter
            listItemSize = resources.getInteger(R.integer.space_list_size)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    progressBar.visibility = View.VISIBLE
                    searchPeopleViewModel.loadData(newText, listItemSize)
                    return false
                }

            })

            setUpViewModelObservers()

        }.root

    }

    private fun finishActivityAndReturnValue(selectedPerson: PersonModel) {
        val returnIntent = Intent()
        returnIntent.putExtra(Constants.Intent.PERSON, selectedPerson)
        activity?.setResult(RESULT_OK, returnIntent)
        activity?.finish()
    }

    private fun setUpViewModelObservers() {
        // TODO: Put common code inside a function
        searchPeopleViewModel.persons.observe(viewLifecycleOwner, Observer { personsList ->
            personsList?.let {
                if (it.isNotEmpty()) {
                    updateEmptyListUI(false)
                    personAdapter.personsList = it
                    personAdapter.notifyDataSetChanged()
                } else {
                    updateEmptyListUI(true)
                    personAdapter.personsList = emptyList()
                    personAdapter.notifyDataSetChanged()
                }
            }
        })
        searchPeopleViewModel.peopleError.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                personAdapter.personsList = emptyList()
                personAdapter.notifyDataSetChanged()
                showDialogWithMessage(R.string.error_occurred, it)
            }
        })
    }

    private fun showDialogWithMessage(titleResourceId: Int?, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(titleResourceId ?: R.string.message)
        val tvMessage = TextView(requireContext())
        tvMessage.setPadding(10, 10, 10, 10)
        tvMessage.text = message

        builder.setView(tvMessage)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun updateEmptyListUI(listEmpty: Boolean) {
        progress_bar.visibility = View.GONE
        if (listEmpty) {
            tv_empty_data.visibility = View.VISIBLE
            recycler_view.visibility = View.GONE
        } else {
            tv_empty_data.visibility = View.GONE
            recycler_view.visibility = View.VISIBLE
        }
    }

    class SearchPersonAdapter(private val listItemClick: (PersonModel) -> Unit) :
            RecyclerView.Adapter<SearchPersonAdapter.ViewHolder>() {
        var personsList: List<PersonModel> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
            return ViewHolder(ListItemPersonsBinding.inflate(LayoutInflater.from(parent.context), parent, false)) { position ->
                listItemClick(personsList[position])
            }
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(personsList[position])
        }

        override fun getItemCount(): Int {
            return personsList.size
        }

        inner class ViewHolder(val binding: ListItemPersonsBinding, val listItemClicked: (Int) -> Unit) :
                RecyclerView.ViewHolder(binding.root) {
            init {
                binding.rootListItemPersonsView.setOnClickListener {
                    listItemClicked(adapterPosition)
                }
            }

            fun bind(itemModel: PersonModel) {
                binding.listItem = itemModel
                binding.executePendingBindings()
            }
        }
    }
}