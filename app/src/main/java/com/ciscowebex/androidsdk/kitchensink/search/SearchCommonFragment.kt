package com.ciscowebex.androidsdk.kitchensink.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.databinding.CommonFragmentItemListBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentCommonBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.space.Space
import kotlinx.android.synthetic.main.fragment_common.*
import org.koin.android.ext.android.inject


class SearchCommonFragment : Fragment() {
    private val searchViewModel: SearchViewModel by inject()
    private var adapter: CustomAdapter = CustomAdapter()
    private val itemModelList = mutableListOf<ItemModel>()
    lateinit var taskType: String

    companion object {
        object TaskType {
            const val TaskSearchSpace = "SearchSpace"
            const val TaskCallHistory = "CallHistory"
            const val TaskListSpaces = "ListSpaces"
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return FragmentCommonBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@SearchCommonFragment

            recyclerView.itemAnimator = DefaultItemAnimator()

            recyclerView.adapter = adapter

            taskType = arguments?.getString(Constants.Bundle.KEY_TASK_TYPE)
                    ?: TaskType.TaskListSpaces

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    progress_bar.visibility = View.VISIBLE
                    searchViewModel.search(newText)
                    return false
                }

            })

            setUpViewModelObservers()

        }.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateSearchInputViewVisibility()
        progress_bar.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        searchViewModel.loadData(taskType, resources.getInteger(R.integer.space_list_size))
    }

    private fun setUpViewModelObservers() {
        // TODO: Put common code inside a function
        searchViewModel.spaces.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                if (taskType == TaskType.TaskCallHistory) it.sortedBy { it.created } else it.sortedByDescending { it.lastActivity }

                if (it.isEmpty()) {
                    updateEmptyListUI(true)
                } else {
                    updateEmptyListUI(false)
                    itemModelList.clear()
                    for (i in it.indices) {
                        val id = it[i].id
                        val item = itemModelList.find { listItem -> listItem.callerId == id }
                        if (item == null) {
                            val itemModel = ItemModel()
                            itemModel.name = it[i].title
                            itemModel.image = R.drawable.ic_call
                            itemModel.callerId = id
                            itemModel.ongoing = searchViewModel.isSpaceCallStarted() && searchViewModel.spaceCallId() == id
                            //add in array list
                            itemModelList.add(itemModel)
                        }
                    }
                    adapter.itemList = itemModelList
                    adapter.notifyDataSetChanged()
                }
            }
        })

        searchViewModel.searchResult.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                if (it.isEmpty()) {
                    updateEmptyListUI(true)
                } else {
                    updateEmptyListUI(false)
                    itemModelList.clear()
                    for (i in it.indices) {
                        val itemModel = ItemModel()
                        val space = it[i]
                        itemModel.name = space.title.orEmpty()
                        itemModel.image = R.drawable.ic_call
                        itemModel.callerId = space.id.orEmpty()
                        itemModelList.add(itemModel)
                    }
                    adapter.itemList = itemModelList
                    adapter.notifyDataSetChanged()
                }
            }
        })

        searchViewModel.getSpaceEvent()?.observe(viewLifecycleOwner, Observer {
            when (it.first) {
                WebexRepository.SpaceEvent.CallStarted -> {
                    if (it.second is String?) {
                        val spaceId = it.second as String?
                        spaceId?.let { id ->
                            updateSpaceCallStatus(id, true)
                        }
                    }
                }
                WebexRepository.SpaceEvent.CallEnded -> {
                    if (it.second is String?) {
                        val spaceId = it.second as String?
                        spaceId?.let { id ->
                            updateSpaceCallStatus(id, false)
                        }
                    }
                }
                else -> {}
            }
        })
    }

    private fun updateSpaceCallStatus(spaceId: String, callStarted: Boolean) {
        val index = adapter.getPositionById(spaceId)
        if (index != -1) {
            val model = adapter.itemList[index]
            model.ongoing = callStarted
            adapter.notifyItemChanged(index)
        }
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

    private fun updateSearchInputViewVisibility() {
        when (taskType) {
            TaskType.TaskSearchSpace -> {
                search_view.visibility = View.VISIBLE
            }
            else -> {
                search_view.visibility = View.GONE
            }
        }
    }

    class ItemModel {
        var image = 0
        lateinit var name: String
        lateinit var callerId: String
        var ongoing = false
    }

    class CustomAdapter() :
            RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        var itemList: MutableList<ItemModel> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
            return ViewHolder(CommonFragmentItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(itemList[position])
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

        inner class ViewHolder(val binding: CommonFragmentItemListBinding) :
                RecyclerView.ViewHolder(binding.root) {

            fun bind(itemModel: ItemModel) {
                binding.listItem = itemModel
                binding.image.setOnClickListener {
                    it.context.startActivity(CallActivity.getOutgoingIntent(it.context, itemModel.callerId))
                }

                if (itemModel.ongoing) {
                    binding.ongoing.visibility = View.VISIBLE
                } else {
                    binding.ongoing.visibility = View.GONE
                }
                binding.executePendingBindings()
            }
        }

        fun getPositionById(spaceId: String): Int {
            return itemList.indexOfFirst { it.callerId == spaceId }
        }
    }
}