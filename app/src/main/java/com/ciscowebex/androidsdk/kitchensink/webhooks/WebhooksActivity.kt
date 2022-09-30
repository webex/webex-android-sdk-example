package com.ciscowebex.androidsdk.kitchensink.webhooks

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityWebhooksBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogWebhookCreateBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogWebhookUpdateBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentDialogWebhookDetailsBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemWebhookBinding
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.webhook.Webhook
import org.koin.android.ext.android.inject


class WebhooksActivity : AppCompatActivity() {
    var tag = "WebhooksActivity"
    private lateinit var binding: ActivityWebhooksBinding
    private lateinit var webhookAdapter: WebhookListAdapter
    private val webhookModel : WebhooksViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityWebhooksBinding>(this, R.layout.activity_webhooks)
                .also { binding = it }
                .apply {
                    val optionsDialogFragment = WebhookActionBottomSheetFragment (
                            { webhookId -> webhookModel.get(webhookId)},
                            { webhookId -> webhookModel.delete(webhookId)},
                            { webhookId, model -> updateWebhookDialog(webhookId, model)})

                    swipeContainer.setOnRefreshListener {
                        updateList()
                    }

                    addWebhookButton.setOnClickListener {
                        createWebhookDialog()
                    }

                    val dividerItemDecoration = DividerItemDecoration(this@WebhooksActivity, LinearLayoutManager.VERTICAL)
                    webhookRecyclerView.addItemDecoration(dividerItemDecoration)
                    webhookAdapter = WebhookListAdapter(optionsDialogFragment, supportFragmentManager)
                    webhookRecyclerView.adapter = webhookAdapter

                    webhookModel.webhooksList.observe(this@WebhooksActivity, Observer {
                        it?.let {
                            swipeContainer.isRefreshing = false
                            webhookAdapter.webhookList.clear()
                            webhookAdapter.webhookList.addAll(it)
                            webhookAdapter.notifyDataSetChanged()
                        }
                    })

                    webhookModel.webhooksError.observe(this@WebhooksActivity, Observer {
                        it?.let {
                            showDialogWithMessage(this@WebhooksActivity, R.string.webhook_error, it)
                        } ?: run {
                            showDialogWithMessage(this@WebhooksActivity, R.string.webhook_error, "")
                        }
                    })

                    webhookModel.webhookData.observe(this@WebhooksActivity, Observer {
                        it?.let {
                            when (WebhooksViewModel.WebhookEvent.valueOf(it.first.name)) {
                                WebhooksViewModel.WebhookEvent.CREATE -> {
                                    Log.d(tag, "WebhookEvent.CREATE")
                                    updateList()
                                }
                                WebhooksViewModel.WebhookEvent.GET -> {
                                    Log.d(tag, "WebhookEvent.GET")
                                    webhookDetails(it.second)
                                }
                                WebhooksViewModel.WebhookEvent.UPDATE -> {
                                    Log.d(tag, "WebhookEvent.UPDATE")
                                    webhookDetails(it.second)
                                }
                            }
                        }
                    })

                    webhookModel.deleteWebhook.observe(this@WebhooksActivity, Observer { delete ->
                        delete?.let {
                            updateList()
                        }
                    })

                }
    }

    private fun updateList() {
        webhookModel.list(resources.getInteger(R.integer.webhook_list_max))
    }

    private fun createWebhookDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        DialogWebhookCreateBinding.inflate(layoutInflater)
                .apply {
                    builder.setView(this.root)

                    builder.setPositiveButton(getString(R.string.create)) { dialog, _ ->
                        val name = nameEditText.text.toString()
                        val targetUrl = targetUrlEditText.text.toString()
                        val resource = resourceEditText.text.toString()
                        val event = eventEditText.text.toString()
                        val filter: String? = if (filterEditText.text.isNotEmpty()) filterEditText.text.toString() else null
                        val secret: String? = if (secretEditText.text.isNotEmpty()) secretEditText.text.toString() else null

                        webhookModel.create(name, targetUrl, resource, event, filter, secret)
                        dialog.dismiss()
                    }

                    builder.show()
                }
    }

    private fun updateWebhookDialog(webhookId: String, model: Webhook?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        DialogWebhookUpdateBinding.inflate(layoutInflater)
                .apply {
                    builder.setView(this.root)

                    model?.let { webhook ->
                        nameEditText.text = Editable.Factory.getInstance().newEditable(webhook.name)
                        targetUrlEditText.text = Editable.Factory.getInstance().newEditable(webhook.targetUrl)

                        webhook.status?.let {
                            statusEditText.text = Editable.Factory.getInstance().newEditable(webhook.status)
                        }

                        webhook.secret?.let {
                            secretEditText.text = Editable.Factory.getInstance().newEditable(webhook.secret)
                        }
                    }

                    builder.setPositiveButton(getString(R.string.update)) { dialog, _ ->

                        val name = nameEditText.text.toString()
                        val targetUrl = targetUrlEditText.text.toString()
                        val status: String? = if (statusEditText.text.isNotEmpty()) statusEditText.text.toString() else null
                        val secret: String? = if (secretEditText.text.isNotEmpty()) secretEditText.text.toString() else null

                        webhookModel.update(webhookId, name, targetUrl, secret, status)
                        dialog.dismiss()
                    }

                    builder.show()
                }
    }

    private fun webhookDetails(_webhook: Webhook) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        FragmentDialogWebhookDetailsBinding.inflate(layoutInflater)
                .apply {
                    webhook = _webhook

                    builder.setView(this.root)
                    builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                        updateList()
                        dialog.dismiss()
                    }

                    builder.show()
                }
    }

    class WebhookListAdapter(private val optionsDialogFragment: WebhookActionBottomSheetFragment, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var webhookList: MutableList<Webhook> = mutableListOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = ListItemWebhookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return webhookViewHolder(binding, optionsDialogFragment, fragmentManager)
        }

        override fun getItemCount(): Int {
            return webhookList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as webhookViewHolder).bind(webhookList[position])
        }

        inner class webhookViewHolder(private val binding: ListItemWebhookBinding, private val optionsDialogFragment: WebhookActionBottomSheetFragment, private val fragmentManager: FragmentManager): RecyclerView.ViewHolder(binding.root) {
            var webhook: Webhook? = null

            init {
                binding.rootListItemLayout.setOnLongClickListener { _ ->
                    optionsDialogFragment.webhookId = webhook?.id ?: ""
                    optionsDialogFragment.webhookModel = webhook

                    optionsDialogFragment.show(fragmentManager, "People Options")

                    true
                }
            }

            fun bind(webhook: Webhook) {
                this.webhook = webhook
                binding.name.text = webhook.name
                binding.path.text = webhook.targetUrl
            }
        }
    }
}