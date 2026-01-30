package com.ciscowebex.androidsdk.kitchensink.webhooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.webhook.Webhook
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class WebhookActionBottomSheetFragment(val getDetails: (String) -> Unit,
                                       val delete: (String) -> Unit,
                                       val update: (String, Webhook?) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var webhookGetDetails: TextView
    private lateinit var webhookDelete: TextView
    private lateinit var webhookUpdate: TextView
    private lateinit var cancel: TextView
    
    lateinit var webhookId: String
    var webhookModel: Webhook? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_webhook_action, container, false)
        
        webhookGetDetails = view.findViewById(R.id.webhookGetDetails)
        webhookDelete = view.findViewById(R.id.webhookDelete)
        webhookUpdate = view.findViewById(R.id.webhookUpdate)
        cancel = view.findViewById(R.id.cancel)

        webhookGetDetails.setOnClickListener {
            dismiss()
            getDetails(webhookId)
        }

        webhookDelete.setOnClickListener {
            dismiss()
            delete(webhookId)
        }

        webhookUpdate.setOnClickListener {
            dismiss()
            update(webhookId, webhookModel)
        }

        cancel.setOnClickListener { dismiss() }
        
        return view
    }

}