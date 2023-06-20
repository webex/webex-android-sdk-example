package com.ciscowebex.androidsdk.kitchensink.webhooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetWebhookActionBinding
import com.ciscowebex.androidsdk.webhook.Webhook
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class WebhookActionBottomSheetFragment(val getDetails: (String) -> Unit,
                                       val delete: (String) -> Unit,
                                       val update: (String, Webhook?) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetWebhookActionBinding
    lateinit var webhookId: String
    var webhookModel: Webhook? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetWebhookActionBinding.inflate(inflater, container, false).also { binding = it }.apply {

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
        }.root
    }

}