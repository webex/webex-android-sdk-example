package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.phone.Call
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class IncomingCallBottomSheetFragment(val onBottomSheetDismissed: (BottomSheetDialogFragment) -> Unit): BottomSheetDialogFragment() {
    companion object {
        const val TAG = "IncomingCallBottomSheetFragment"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var imgClose: ImageView
    
    var adapter: IncomingInfoAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.bottom_sheet_with_recycler_view, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        imgClose = view.findViewById(R.id.imgClose)
        
        recyclerView.adapter = adapter
        Log.d(TAG, "showIncomingCallBottomSheet adapter $adapter")
        imgClose.setOnClickListener { onBottomSheetDismissed(this@IncomingCallBottomSheetFragment) }
        
        return view
    }

    class IncomingInfoAdapter(private val IncomingCallPickEvent: (Call?) -> Unit, private val incomingCallCancelEvent: (Call?) -> Unit) : RecyclerView.Adapter<IncomingInfoViewHolder>() {
        var info: MutableList<IncomingCallInfoModel> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingInfoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_call_meeting, parent, false)
            return IncomingInfoViewHolder(view, IncomingCallPickEvent, incomingCallCancelEvent)
        }

        override fun getItemCount(): Int = info.size

        override fun onBindViewHolder(holder: IncomingInfoViewHolder, position: Int) {
            holder.bind(info[position])
        }
    }

    class IncomingInfoViewHolder(
        itemView: View,
        private val IncomingCallPickEvent: (Call?) -> Unit,
        private val IncomingCallCancelEvent: (Call?) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        var item: IncomingCallInfoModel? = null
        val tag = "IncomingInfoViewHolder"
        
        private val meetingJoinButton: Button = itemView.findViewById(R.id.meetingJoinButton)
        private val ivPickCall: ImageView = itemView.findViewById(R.id.iv_pick_call)
        private val ivCancelCall: ImageView = itemView.findViewById(R.id.iv_cancel_call)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val meetingTimeTextView: TextView = itemView.findViewById(R.id.meetingTimeTextView)
        private val callingOneToOneButtonLayout: LinearLayout = itemView.findViewById(R.id.callingOneToOneButtonLayout)
        
        init {
            meetingJoinButton.setOnClickListener {
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
                    meetingJoinButton.alpha = 0.5f
                    meetingJoinButton.isEnabled = false
                }
            }

            ivPickCall.setOnClickListener {
                item?.let { model ->
                    if (model is OneToOneIncomingCallModel) {
                        Log.d(tag, "ivPickCall clicked")
                        IncomingCallPickEvent(model.call)
                        model.isEnabled = false
                        ivPickCall.alpha = 0.5f
                        ivPickCall.isEnabled = false
                    }
                }
            }

            ivCancelCall.setOnClickListener {
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
                        meetingJoinButton.alpha = 1.0f
                        meetingJoinButton.isEnabled = true
                    } else {
                        meetingJoinButton.alpha = 0.5f
                        meetingJoinButton.isEnabled = false
                    }

                    titleTextView.text = model.subject
                    meetingTimeTextView.text = model.timeString
                    meetingTimeTextView.visibility = View.VISIBLE
                    callingOneToOneButtonLayout.visibility = View.GONE
                    meetingJoinButton.visibility = View.VISIBLE
                }
                is OneToOneIncomingCallModel -> {
                    if (model.isEnabled) {
                        ivPickCall.alpha = 1.0f
                        ivPickCall.isEnabled = true
                    } else {
                        ivPickCall.alpha = 0.5f
                        ivPickCall.isEnabled = false
                    }

                    meetingJoinButton.visibility = View.GONE
                    meetingTimeTextView.visibility = View.GONE
                    callingOneToOneButtonLayout.visibility = View.VISIBLE
                    titleTextView.text = model.call?.getTitle()
                }
                is SpaceIncomingCallModel -> {
                    if (model.isEnabled) {
                        meetingJoinButton.alpha = 1.0f
                        meetingJoinButton.isEnabled = true
                    } else {
                        meetingJoinButton.alpha = 0.5f
                        meetingJoinButton.isEnabled = false
                    }

                    meetingTimeTextView.visibility = View.GONE
                    titleTextView.text = model.call?.getTitle()
                    callingOneToOneButtonLayout.visibility = View.GONE
                    meetingJoinButton.visibility = View.VISIBLE
                }
            }
        }
    }
}
