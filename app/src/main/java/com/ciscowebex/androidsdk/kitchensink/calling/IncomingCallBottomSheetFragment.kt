package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetWithRecyclerViewBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemCallMeetingBinding
import com.ciscowebex.androidsdk.phone.Call
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class IncomingCallBottomSheetFragment(val onBottomSheetDismissed: (BottomSheetDialogFragment) -> Unit): BottomSheetDialogFragment() {
    companion object {
        const val TAG = "IncomingCallBottomSheetFragment"
    }

    private lateinit var binding: BottomSheetWithRecyclerViewBinding
    var adapter: IncomingInfoAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomSheetWithRecyclerViewBinding.inflate(inflater, container, false).also { binding = it }.apply {
            binding.recyclerView.adapter = adapter
            Log.d(TAG, "showIncomingCallBottomSheet adapter $adapter")
            imgClose.setOnClickListener { onBottomSheetDismissed(this@IncomingCallBottomSheetFragment) }
        }.root
    }

    class IncomingInfoAdapter(private val IncomingCallPickEvent: (Call?) -> Unit, private val incomingCallCancelEvent: (Call?) -> Unit) : RecyclerView.Adapter<IncomingInfoViewHolder>() {
        var info: MutableList<IncomingCallInfoModel> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingInfoViewHolder {
            return IncomingInfoViewHolder(ListItemCallMeetingBinding.inflate(LayoutInflater.from(parent.context), parent, false), IncomingCallPickEvent, incomingCallCancelEvent)
        }

        override fun getItemCount(): Int = info.size

        override fun onBindViewHolder(holder: IncomingInfoViewHolder, position: Int) {
            holder.bind(info[position])
        }
    }

    class IncomingInfoViewHolder(
        private val binding: ListItemCallMeetingBinding,
        private val IncomingCallPickEvent: (Call?) -> Unit,
        private val IncomingCallCancelEvent: (Call?) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        var item: IncomingCallInfoModel? = null
        val tag = "IncomingInfoViewHolder"
        init {
            binding.meetingJoinButton.setOnClickListener {
                item?.let { model ->
                    when (model) {
                        is MeetingInfoModel -> {
                            Log.d(tag, "JoinButton clicked meetingInfo: ${model.subject}")
                        }
                        is SpaceIncomingCallModel -> {
                            Log.d(tag, "JoinButton clicked SpaceCall")
                        }
                    }
                    IncomingCallPickEvent(model.call)
                    model.isEnabled = false
                    binding.meetingJoinButton.alpha = 0.5f
                    binding.meetingJoinButton.isEnabled = false
                }
            }

            binding.ivPickCall.setOnClickListener {
                item?.let { model ->
                    if (model is OneToOneIncomingCallModel) {
                        Log.d(tag, "ivPickCall clicked")
                        IncomingCallPickEvent(model.call)
                        model.isEnabled = false
                        binding.ivPickCall.alpha = 0.5f
                        binding.ivPickCall.isEnabled = false
                    }
                }
            }

            binding.ivCancelCall.setOnClickListener {
                item?.let { model ->
                    if (model is OneToOneIncomingCallModel) {
                        IncomingCallCancelEvent(model.call)
                    }
                }
            }
        }

        fun bind(model: IncomingCallInfoModel) {
            item = model

            when (model) {
                is MeetingInfoModel -> {
                    if (model.isEnabled) {
                        binding.meetingJoinButton.alpha = 1.0f
                        binding.meetingJoinButton.isEnabled = true
                    } else {
                        binding.meetingJoinButton.alpha = 0.5f
                        binding.meetingJoinButton.isEnabled = false
                    }

                    binding.titleTextView.text = model.subject
                    binding.meetingTimeTextView.text = model.timeString
                    binding.meetingTimeTextView.visibility = View.VISIBLE
                    binding.callingOneToOneButtonLayout.visibility = View.GONE
                    binding.meetingJoinButton.visibility = View.VISIBLE
                }
                is OneToOneIncomingCallModel -> {
                    if (model.isEnabled) {
                        binding.ivPickCall.alpha = 1.0f
                        binding.ivPickCall.isEnabled = true
                    } else {
                        binding.ivPickCall.alpha = 0.5f
                        binding.ivPickCall.isEnabled = false
                    }

                    binding.meetingJoinButton.visibility = View.GONE
                    binding.meetingTimeTextView.visibility = View.GONE
                    binding.callingOneToOneButtonLayout.visibility = View.VISIBLE
                    binding.titleTextView.text = model.call?.getTitle()
                }
                is SpaceIncomingCallModel -> {
                    if (model.isEnabled) {
                        binding.meetingJoinButton.alpha = 1.0f
                        binding.meetingJoinButton.isEnabled = true
                    } else {
                        binding.meetingJoinButton.alpha = 0.5f
                        binding.meetingJoinButton.isEnabled = false
                    }

                    binding.meetingTimeTextView.visibility = View.GONE
                    binding.titleTextView.text = model.call?.getTitle()
                    binding.callingOneToOneButtonLayout.visibility = View.GONE
                    binding.meetingJoinButton.visibility = View.VISIBLE
                }
            }
            binding.executePendingBindings()
        }
    }
}