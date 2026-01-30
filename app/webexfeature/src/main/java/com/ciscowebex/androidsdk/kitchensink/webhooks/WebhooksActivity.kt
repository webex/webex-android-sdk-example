package com.ciscowebex.androidsdk.kitchensink.webhooks

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.webhook.Webhook
import com.google.android.play.core.splitcompat.SplitCompat
import org.koin.android.ext.android.inject


class WebhooksActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }
    var tag = "WebhooksActivity"
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var addWebhookButton: Button
    private lateinit var webhookRecyclerView: RecyclerView
    private lateinit var webhookAdapter: WebhookListAdapter
    private val webhookModel : WebhooksViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webhooks)
        
        swipeContainer = findViewById(R.id.swipeContainer)
        addWebhookButton = findViewById(R.id.addWebhookButton)
        webhookRecyclerView = findViewById(R.id.webhook_recycler_view)
        
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

        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        webhookRecyclerView.addItemDecoration(dividerItemDecoration)
        webhookAdapter = WebhookListAdapter(optionsDialogFragment, supportFragmentManager)
        webhookRecyclerView.adapter = webhookAdapter

        webhookModel.webhooksList.observe(this, Observer {
            it?.let {
                swipeContainer.isRefreshing = false
                webhookAdapter.webhookList.clear()
                webhookAdapter.webhookList.addAll(it)
                webhookAdapter.notifyDataSetChanged()
            }
        })

        webhookModel.webhooksError.observe(this, Observer {
            it?.let {
                showDialogWithMessage(this@WebhooksActivity, R.string.webhook_error, it)
            } ?: run {
                showDialogWithMessage(this@WebhooksActivity, R.string.webhook_error, "")
            }
        })

        webhookModel.webhookData.observe(this, Observer {
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

        webhookModel.deleteWebhook.observe(this, Observer { delete ->
            delete?.let {
                updateList()
            }
        })
    }

    private fun updateList() {
        webhookModel.list(resources.getInteger(R.integer.webhook_list_max))
    }

    private fun createWebhookDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_webhook_create, null)
        
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val targetUrlEditText = dialogView.findViewById<EditText>(R.id.targetUrlEditText)
        val resourceEditText = dialogView.findViewById<EditText>(R.id.resourceEditText)
        val eventEditText = dialogView.findViewById<EditText>(R.id.eventEditText)
        val filterEditText = dialogView.findViewById<EditText>(R.id.filterEditText)
        val secretEditText = dialogView.findViewById<EditText>(R.id.secretEditText)
        
        builder.setView(dialogView)
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

    private fun updateWebhookDialog(webhookId: String, model: Webhook?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_webhook_update, null)
        
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val targetUrlEditText = dialogView.findViewById<EditText>(R.id.targetUrlEditText)
        val statusEditText = dialogView.findViewById<EditText>(R.id.statusEditText)
        val secretEditText = dialogView.findViewById<EditText>(R.id.secretEditText)
        
        builder.setView(dialogView)
        
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

    private fun webhookDetails(_webhook: Webhook) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.fragment_dialog_webhook_details, null)
        
        // Set webhook details to the dialog views
        dialogView.findViewById<TextView>(R.id.IdTextView)?.text = _webhook.id
        dialogView.findViewById<TextView>(R.id.NameTextView)?.text = _webhook.name
        dialogView.findViewById<TextView>(R.id.UrlTextView)?.text = _webhook.targetUrl
        dialogView.findViewById<TextView>(R.id.resourceTextView)?.text = _webhook.resource
        dialogView.findViewById<TextView>(R.id.eventTextView)?.text = _webhook.event
        dialogView.findViewById<TextView>(R.id.filterTextView)?.text = _webhook.filter ?: ""
        dialogView.findViewById<TextView>(R.id.secretTextView)?.text = _webhook.secret ?: ""
        dialogView.findViewById<TextView>(R.id.statusTextView)?.text = _webhook.status ?: ""
        dialogView.findViewById<TextView>(R.id.createdTextView)?.text = _webhook.created?.toString() ?: ""
        
        builder.setView(dialogView)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            updateList()
            dialog.dismiss()
        }
        builder.show()
    }

    class WebhookListAdapter(private val optionsDialogFragment: WebhookActionBottomSheetFragment, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var webhookList: MutableList<Webhook> = mutableListOf()
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_webhook, parent, false)
            return WebhookViewHolder(view, optionsDialogFragment, fragmentManager)
        }

        override fun getItemCount(): Int {
            return webhookList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as WebhookViewHolder).bind(webhookList[position])
        }

        inner class WebhookViewHolder(itemView: View, private val optionsDialogFragment: WebhookActionBottomSheetFragment, private val fragmentManager: FragmentManager): RecyclerView.ViewHolder(itemView) {
            var webhook: Webhook? = null
            private val rootLayout: View = itemView.findViewById(R.id.rootListItemLayout)
            private val nameTextView: TextView = itemView.findViewById(R.id.name)
            private val pathTextView: TextView = itemView.findViewById(R.id.path)

            init {
                rootLayout.setOnLongClickListener { _ ->
                    optionsDialogFragment.webhookId = webhook?.id ?: ""
                    optionsDialogFragment.webhookModel = webhook

                    optionsDialogFragment.show(fragmentManager, "People Options")

                    true
                }
            }

            fun bind(webhook: Webhook) {
                this.webhook = webhook
                nameTextView.text = webhook.name
                pathTextView.text = webhook.targetUrl
            }
        }
    }
}
