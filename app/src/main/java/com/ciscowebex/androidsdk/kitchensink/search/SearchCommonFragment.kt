package com.ciscowebex.androidsdk.kitchensink.search

import android.Manifest
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.databinding.CommonFragmentItemListBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentCommonBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipViewModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.formatCallDurationTime
import com.ciscowebex.androidsdk.kitchensink.utils.stateToDrawable
import com.ciscowebex.androidsdk.phone.CallHistoryEvent
import com.ciscowebex.androidsdk.phone.CallHistoryRecord
import com.ciscowebex.androidsdk.space.Space
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat


class SearchCommonFragment : Fragment() {
    private val searchViewModel: SearchViewModel by inject()
    private val membershipViewModel: MembershipViewModel by inject()
    private var adapter: CustomAdapter = CustomAdapter()
    private val itemModelList = mutableListOf<ItemModel>()
    private val directMembers = mutableSetOf<MembershipModel>()
    lateinit var taskType: String
    lateinit var binding: FragmentCommonBinding
    private val callingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val allGranted = grants.values.all { it }
            val webexVM = (activity as? BaseActivity)?.webexViewModel
            if (allGranted) {
                webexVM?.retryPendingDialIfAny()
                webexVM?.retryPendingAnswerIfAny()
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_error), Toast.LENGTH_LONG).show()
            }
        }

    // Progress dialog for delete operations
    private var deleteDialogInfo: DeleteDialogInfo? = null

    // No local pre-checks: rely on SDK PERMISSION_REQUIRED and BaseActivity/CallControlsFragment handlers
    private fun startCall(callerId: String, isPhoneNumber: Boolean, moveMeeting: Boolean) {
        startActivity(CallActivity.getOutgoingIntent(requireContext(), callerId, isPhoneNumber, moveMeeting))
    }

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

            // Set fragment reference in adapter
            adapter.fragment = this@SearchCommonFragment
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
            // Fragment-local fallback observer to launch permission prompts
            (activity as? BaseActivity)?.webexViewModel?.callingLiveData?.observe(this@SearchCommonFragment.viewLifecycleOwner) { live ->
                val missing = live?.missingPermissions
                if (!missing.isNullOrEmpty()) {
                    val normalized = normalizePermissionsForApi(missing.toSet()).toTypedArray()
                    callingPermissionLauncher.launch(normalized)
                }
            }

        }.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateSearchInputViewVisibility()
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        checkForInitialSpacesSync()

        searchViewModel.setCallHistoryEventListener()
        searchViewModel.loadData(taskType, Constants.DefaultMax.SPACE_MAX)

    }

    private fun checkForInitialSpacesSync() {
        if (!searchViewModel.isSpacesSyncCompleted()) {
            Snackbar.make(binding.root, getString(R.string.syncing_spaces), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setUpViewModelObservers() {
        // TODO: Put common code inside a function
        membershipViewModel.presenceChangeLiveData.observe(viewLifecycleOwner) { presence ->
            directMembers.find { it.personId == presence.getContactId() }?.let { member ->
                val position = adapter.getPositionById(member.spaceId)
                if(position >= 0) {
                    adapter.itemList[position].presenceStatus =
                        stateToDrawable(this@SearchCommonFragment.requireContext(), presence.getStatus())
                    adapter.notifyItemChanged(position)
                }
            }
        }

        membershipViewModel.memberships.observe(viewLifecycleOwner) {
            if(it.size == 2) { // 1-1 space should have exactly 2 members
                directMembers.add(it[1])
                membershipViewModel.startWatchingPresence(mutableListOf(it[1].personId))
            }

        }

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
                            if(it[i].spaceType == Space.SpaceType.DIRECT) {
                                itemModel.image = R.drawable.ic_call
                            }
                            else {
                                itemModel.image = 0
                            }
                            itemModel.callerId = id
                            itemModel.ongoing = searchViewModel.isSpaceCallStarted() && searchViewModel.spaceCallId() == id
                            itemModel.isExternallyOwned = it[i].isExternallyOwned ?: false
                            //add in array list
                            itemModelList.add(itemModel)

                            //Fetch members for presence status for 1-1 space result
                            if (it[i].spaceType == Space.SpaceType.DIRECT) {
                                membershipViewModel.getMembersIn(id, 2)
                            }
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
                        itemModel.deleteImage = R.drawable.ic_baseline_delete_24
                        itemModel.callerId = callRecord.callbackAddress.orEmpty()
                        itemModel.ongoing = searchViewModel.isSpaceCallStarted() && searchViewModel.spaceCallId() == callRecord.conversationId
//                        itemModel.isExternallyOwned = it[i].isExternallyOwned ?: false
                        itemModel.callDirection = callRecord.callDirection
                        var dateAndDurationString = SimpleDateFormat("dd/MM/yyyy hh:mm a").format(callRecord.startTime)
                        dateAndDurationString += " (" + formatCallDurationTime(callRecord.duration * 1000) + ")"
                        itemModel.dateAndDuration = dateAndDurationString
                        itemModel.isMissedCall = callRecord.isMissedCall
                        itemModel.isPhoneNumber = callRecord.isPhoneNumber
                        itemModel.recordId = callRecord.recordId.orEmpty()
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
                        itemModel.isExternallyOwned = space.isExternallyOwned
                        itemModelList.add(itemModel)

                        //Fetch members for presence status for 1-1 space result
                        if (space.type == Space.SpaceType.DIRECT) {
                            membershipViewModel.getMembersIn(space.id, 2)
                        }

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

        searchViewModel.spacesSyncCompletedLiveData.observe(viewLifecycleOwner) { isSyncing ->
            if (isSyncing) {
                Snackbar.make(binding.root, getString(R.string.syncing_spaces), Snackbar.LENGTH_SHORT).show()
            } else {
                Log.d(tag, getString(R.string.not_syncing_spaces))
            }
        }


        searchViewModel.initialSpacesSyncCompletedLiveData.observe(viewLifecycleOwner) {
            Log.d(tag, getString(R.string.initial_spaces_sync_completed))
        }

        searchViewModel.callHistoryEventLiveData.observe(viewLifecycleOwner) { callHistoryEvent ->
            Log.d("SearchCommonFragment", "Call history event received: $callHistoryEvent")

            when (callHistoryEvent) {
                is CallHistoryEvent.SyncCompleted -> {
                    Log.d("SearchCommonFragment", "Call history sync completed, loading data...")
                    searchViewModel.loadData(taskType, Constants.DefaultMax.SPACE_MAX)
                }
                is CallHistoryEvent.Removed -> {
                    Log.d("SearchCommonFragment", "Call history records removed: ${callHistoryEvent.data}")

                    // Check if this is for our pending delete operation
                    deleteDialogInfo?.let { dialogInfo ->
                        if (callHistoryEvent.data.contains(dialogInfo.recordId)) {
                            // Dismiss the progress dialog
                            dialogInfo.progressDialog.dismiss()
                            deleteDialogInfo = null
                        }
                    }

                    searchViewModel.loadData(taskType, Constants.DefaultMax.SPACE_MAX)
                }
                is CallHistoryEvent.RemoveFailed -> {
                    Log.d("SearchCommonFragment", "Call history record removal failed")

                    // Dismiss dialog if present
                    deleteDialogInfo?.let { dialogInfo ->
                        dialogInfo.progressDialog.dismiss()

                        // Show failure toast
                        context?.let { ctx ->
                            android.widget.Toast.makeText(
                                ctx,
                                "Failed to delete call history record",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }

                        deleteDialogInfo = null
                    }
                }
            }
        }
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

    private fun normalizePermissionsForApi(perms: Set<String>): Set<String> {
        if (Build.VERSION.SDK_INT >= 31) {
            val mapped = perms.map {
                if (it == Manifest.permission.BLUETOOTH) Manifest.permission.BLUETOOTH_CONNECT else it
            }
            return mapped.toSet()
        }
        return perms
    }

    override fun onDestroyView() {
        super.onDestroyView()
        membershipViewModel.stopWatchingPresence()
    }

    class ItemModel {
        var image = 0
        var deleteImage = 0
        lateinit var name: String
        lateinit var callerId: String
        var ongoing = false
        var isExternallyOwned = false
        var dateAndDuration = ""
        var callDirection = CallHistoryRecord.CallDirection.UNDEFINED
        var isMissedCall = false
        var isPhoneNumber = false
        var presenceStatus : Drawable? = null
        var recordId = ""
    }

    class CustomAdapter() : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        var itemList: MutableList<ItemModel> = mutableListOf()
        // Reference to the SearchCommonFragment
        var fragment: SearchCommonFragment? = null

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
                if(itemModel.image == 0)
                    binding.image.visibility = View.GONE
                else
                    binding.image.visibility = View.VISIBLE
                binding.image.setOnClickListener { fragment?.startCall(itemModel.callerId, itemModel.isPhoneNumber, false) }

                if(itemModel.deleteImage == 0)
                    binding.deleteImage.visibility = View.GONE
                else
                    binding.deleteImage.visibility = View.VISIBLE

                binding.deleteImage.setOnClickListener {
                    // Handle delete action with the record ID
                    if (itemModel.recordId.isNotEmpty()) {
                        Log.d("SearchCommonFragment", "Delete clicked for record: ${itemModel.recordId}")

                        // Show progress dialog while deleting
                        val progressDialog = android.app.ProgressDialog(itemView.context).apply {
                            setMessage("Deleting call history record...")
                            setCancelable(false)
                            show()
                        }

                        // Save the dialog in fragment for later handling events
                        fragment?.let { frag ->
                            frag.deleteDialogInfo = DeleteDialogInfo(progressDialog, itemModel.recordId)
                            frag.searchViewModel.removeCallHistoryRecord(itemModel.recordId)
                        }
                    }
                }

                if (itemModel.ongoing) {
                    binding.ongoing.visibility = View.VISIBLE
                } else {
                    binding.ongoing.visibility = View.GONE
                }

                if (itemModel.callDirection == CallHistoryRecord.CallDirection.OUTGOING) {
                    binding.callDirection.visibility = View.VISIBLE
                    binding.callDirection.setImageResource(R.drawable.ic_call_outgoing)
                } else if(itemModel.isMissedCall) {
                    binding.callDirection.visibility = View.VISIBLE
                    binding.callDirection.setImageResource(R.drawable.ic_missed_call)
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

    // Class to hold progress dialog and record ID for delete operations
    private class DeleteDialogInfo(
        val progressDialog: android.app.ProgressDialog,
        val recordId: String
    )
}
