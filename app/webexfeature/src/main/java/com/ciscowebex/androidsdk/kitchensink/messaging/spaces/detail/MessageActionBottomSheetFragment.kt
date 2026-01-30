package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceMessageModel
import com.ciscowebex.androidsdk.message.Message
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MessageActionBottomSheetFragment(val deleteMessageClickListener: (SpaceMessageModel) -> Unit,
                                       val markMessageAsReadClickListener: (SpaceMessageModel) -> Unit,
                                       val replyMessageClickListener: (SpaceMessageModel) -> Unit,
                                       val editMessageClickListener: (SpaceMessageModel) -> Unit,
                                       val fetchByIdClickListener: (SpaceMessageModel) -> Unit,
                                       val fetchByDateClickListener: (SpaceMessageModel) -> Unit) : BottomSheetDialogFragment() {
    companion object {
        val TAG = "MessageActionBottomSheetFragment"
        var selfPersonId : String? = null
    }

    lateinit var message: SpaceMessageModel
    
    private lateinit var deleteMessage: TextView
    private lateinit var markMessageAsRead: TextView
    private lateinit var editMessage: TextView
    private lateinit var replyMessageSeparator: View
    private lateinit var replyMessage: TextView
    private lateinit var fetchById: TextView
    private lateinit var fetchByDate: TextView
    private lateinit var cancel: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_message_options, container, false)
        
        // Initialize views
        deleteMessage = view.findViewById(R.id.deleteMessage)
        markMessageAsRead = view.findViewById(R.id.markMessageAsRead)
        editMessage = view.findViewById(R.id.editMessage)
        replyMessageSeparator = view.findViewById(R.id.replyMessageSeparator)
        replyMessage = view.findViewById(R.id.replyMessage)
        fetchById = view.findViewById(R.id.fetchById)
        fetchByDate = view.findViewById(R.id.fetchByDate)
        cancel = view.findViewById(R.id.cancel)

        if(message.personId == selfPersonId) {
            // Show delete message option for self messages
            deleteMessage.visibility = View.VISIBLE
            deleteMessage.setOnClickListener {
                dismiss()
                deleteMessageClickListener(message)
            }
            // Hide Mark Message Read option for self messages, as they would be in read status be default
            markMessageAsRead.visibility = View.GONE
            // Edit message allowed for self messages only
            editMessage.visibility = View.VISIBLE
            editMessage.setOnClickListener {
                dismiss()
                editMessageClickListener(message)
            }
        }else {
            editMessage.visibility = View.GONE
            deleteMessage.visibility = View.GONE
            replyMessageSeparator.visibility = View.GONE
            markMessageAsRead.visibility = View.VISIBLE
        }

        markMessageAsRead.setOnClickListener {
            dismiss()
            markMessageAsReadClickListener(message)
        }

        replyMessage.setOnClickListener {
            dismiss()
            replyMessageClickListener(message)
        }

        fetchById.setOnClickListener {
            dismiss()
            fetchByIdClickListener(message)
        }

        fetchByDate.setOnClickListener {
            dismiss()
            fetchByDateClickListener(message)
        }

        cancel.setOnClickListener { dismiss() }
        
        return view
    }
}