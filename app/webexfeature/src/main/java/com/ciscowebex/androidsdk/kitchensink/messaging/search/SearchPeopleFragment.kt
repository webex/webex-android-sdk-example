package com.ciscowebex.androidsdk.kitchensink.messaging.search

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import androidx.constraintlayout.widget.ConstraintLayout
import com.ciscowebex.androidsdk.kitchensink.person.PersonModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
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

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyData: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_common, container, false)
        
        // Initialize views using findViewById
        recyclerView = view.findViewById(R.id.recycler_view)
        searchView = view.findViewById(R.id.search_view)
        progressBar = view.findViewById(R.id.progress_bar)
        tvEmptyData = view.findViewById(R.id.tv_empty_data)
        
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

        return view
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
        progressBar.visibility = View.GONE
        if (listEmpty) {
            tvEmptyData.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmptyData.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    class SearchPersonAdapter(private val listItemClick: (PersonModel) -> Unit) :
            RecyclerView.Adapter<SearchPersonAdapter.ViewHolder>() {
        var personsList: List<PersonModel> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_persons, parent, false)
            return ViewHolder(view) { position ->
                listItemClick(personsList[position])
            }
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(personsList[position])
        }

        override fun getItemCount(): Int {
            return personsList.size
        }

        inner class ViewHolder(itemView: View, val listItemClicked: (Int) -> Unit) :
                RecyclerView.ViewHolder(itemView) {
            private val rootView: ConstraintLayout = itemView.findViewById(R.id.rootListItemPersonsView)
            private val nameTextView: TextView = itemView.findViewById(R.id.name)
            
            init {
                rootView.setOnClickListener {
                    listItemClicked(adapterPosition)
                }
            }

            fun bind(itemModel: PersonModel) {
                nameTextView.text = itemModel.displayName ?: ""
            }
        }
    }
}