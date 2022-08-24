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
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.formatCallDurationTime
import com.ciscowebex.androidsdk.phone.CallHistoryRecord
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat


class SearchCommonFragment : Fragment() {
    private val searchViewModel: SearchViewModel by inject()
    private var adapter: CustomAdapter = CustomAdapter()
    private val itemModelList = mutableListOf<ItemModel>()
    lateinit var taskType: String
    lateinit var binding: FragmentCommonBinding

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
        return FragmentCommonBinding.inflate(inflater, container, false)
            .also { binding = it }
            .apply {
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
                    progressBar.visibility = View.VISIBLE
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
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        searchViewModel.loadData(taskType, Constants.DefaultMax.SPACE_MAX)
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

        searchViewModel.callHistoryRecords.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                if (it.isEmpty()) {
                    updateEmptyListUI(true)
                } else {
                    updateEmptyListUI(false)
                    itemModelList.clear()
                    for (i in it.indices) {
                        val itemModel = ItemModel()
                        val callRecord = it[i]
                        itemModel.name = callRecord.displayName.orEmpty()
                        itemModel.image = R.drawable.ic_call
                        itemModel.callerId = callRecord.callbackAddress.orEmpty()
                        itemModel.ongoing = searchViewModel.isSpaceCallStarted() && searchViewModel.spaceCallId() == callRecord.conversationId
//                        itemModel.isExternallyOwned = it[i].isExternallyOwned ?: false
                        itemModel.callDirection = callRecord.callDirection
                        var dateAndDurationString = SimpleDateFormat("dd/MM/yyyy hh:mm a").format(callRecord.startTime)
                        dateAndDurationString += " (" + formatCallDurationTime(callRecord.duration * 1000) + ")"
                        itemModel.dateAndDuration = dateAndDurationString
                        //add in array list
                        itemModelList.add(itemModel)
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
        binding.progressBar.visibility = View.GONE
        if (listEmpty) {
            binding.tvEmptyData.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.tvEmptyData.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateSearchInputViewVisibility() {
        when (taskType) {
            TaskType.TaskSearchSpace -> {
                binding.searchView.visibility = View.VISIBLE
            }
            else -> {
                binding.searchView.visibility = View.GONE
            }
        }
    }

    class ItemModel {
        var image = 0
        lateinit var name: String
        lateinit var callerId: String
        var ongoing = false
        var isExternallyOwned = false
        var dateAndDuration = ""
        var callDirection = CallHistoryRecord.CallDirection.UNDEFINED
    }

    class CustomAdapter() : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
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

                if (itemModel.callDirection == CallHistoryRecord.CallDirection.OUTGOING) {
                    binding.callDirection.visibility = View.VISIBLE
                    binding.callDirection.setImageResource(R.drawable.ic_call_outgoing)
                } else if (itemModel.callDirection == CallHistoryRecord.CallDirection.INCOMING) {
                    binding.callDirection.visibility = View.VISIBLE
                    binding.callDirection.setImageResource(R.drawable.ic_call_incoming)
                } else {
                    binding.callDirection.visibility = View.GONE
                }

                if (itemModel.dateAndDuration.isNotEmpty()) {
                    binding.startTimeAndDuration.visibility = View.VISIBLE
                    binding.startTimeAndDuration.text = itemModel.dateAndDuration
                } else {
                    binding.startTimeAndDuration.visibility = View.GONE
                }
                binding.executePendingBindings()
            }
        }

        fun getPositionById(spaceId: String): Int {
            return itemList.indexOfFirst { it.callerId == spaceId }
        }
    }
}