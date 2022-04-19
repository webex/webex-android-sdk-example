package com.ciscowebex.androidsdk.kitchensink.messaging.composer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityMessageComposerBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogPostMessageHandlerBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemUploadAttachmentBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.composer.MessageComposerViewModel.Companion.MINIMUM_MEMBERS_REQUIRED_FOR_MENTIONS
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.ReplyMessageModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.FileUtils.getPath
import com.ciscowebex.androidsdk.kitchensink.utils.PermissionsHelper
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.hideKeyboard
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.utils.EmailAddress
import com.ciscowebex.androidsdk.utils.internal.MimeUtils
import org.koin.android.ext.android.inject
import java.io.File


class MessageComposerActivity : AppCompatActivity() {

    companion object {
        enum class ComposerType {
            POST_SPACE,
            POST_PERSON_ID,
            POST_PERSON_EMAIL
        }

        enum class StyleType {
            PLAIN_TEXT,
            MARKDOWN_TEXT,
            HTML_TEXT
        }

        fun getIntent(context: Context, type: ComposerType, id: String, replyParentMessage: ReplyMessageModel?, messageId: String? = null): Intent {
            val intent = Intent(context, MessageComposerActivity::class.java)
            intent.putExtra(Constants.Intent.COMPOSER_TYPE, type)
            intent.putExtra(Constants.Intent.COMPOSER_ID, id)
            intent.putExtra(Constants.Intent.COMPOSER_REPLY_PARENT_MESSAGE, replyParentMessage)
            intent.putExtra(Constants.Intent.MESSAGE_ID, messageId)
            return intent
        }
    }

    private val tag = "MessageComposerActivity"
    private val PICKFILE_REQUEST_CODE = 1005
    private lateinit var binding: ActivityMessageComposerBinding
    private val messageComposerViewModel: MessageComposerViewModel by inject()
    private val permissionsHelper: PermissionsHelper by inject()
    private lateinit var composerType: ComposerType
    private var id: String? = null
    private var styleType = StyleType.PLAIN_TEXT
    private lateinit var attachmentAdapter: UploadAttachmentsAdapter
    private var isMentionsEnabled: Boolean = false
    private var replyParentMessage: ReplyMessageModel? = null
    // MessageId is not null in case of edit feature.
    private var messageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        composerType = intent.getSerializableExtra(Constants.Intent.COMPOSER_TYPE) as ComposerType
        id = intent.getStringExtra(Constants.Intent.COMPOSER_ID)
        replyParentMessage = intent.getParcelableExtra(Constants.Intent.COMPOSER_REPLY_PARENT_MESSAGE)
        messageId = intent.getStringExtra(Constants.Intent.MESSAGE_ID)

        if (composerType == ComposerType.POST_SPACE) {
            isMentionsEnabled = true
            messageComposerViewModel.fetchAllMembersInSpace(id)
        }
        DataBindingUtil.setContentView<ActivityMessageComposerBinding>(this, R.layout.activity_message_composer)
                .also { binding = it }
                .apply {

                    plainRadioButton.isChecked = true

                    sendButton.setOnClickListener {
                        sendButtonClicked()
                    }

                    setUpObservers()

                    if (messageId == null) {
                        attachmentButton.setOnClickListener {
                            val checkingPermission = checkReadStoragePermissions()
                            if (!checkingPermission) {
                                openFileExplorer()
                            }
                        }
                    } else {
                        // In case of edit we do not support editing attachments
                      attachmentButton.visibility = View.GONE
                    }

                    radioGroup.setOnCheckedChangeListener { _, checkedId ->
                        when (checkedId) {
                            R.id.plainRadioButton -> {
                                styleType = StyleType.PLAIN_TEXT
                            }
                            R.id.markdownRadioButton -> {
                                styleType = StyleType.MARKDOWN_TEXT
                            }
                            R.id.htmlRadioButton -> {
                                styleType = StyleType.HTML_TEXT
                            }
                        }
                    }

                    val onAttachmentCrossClick: (File) -> Unit = { file ->
                        Log.d(tag, "onAttachmentCrossClick path: ${file.absolutePath}")
                        val position = attachmentAdapter.attachedFiles.indexOf(file)
                        attachmentAdapter.attachedFiles.removeAt(position)
                        attachmentAdapter.notifyItemRemoved(position)
                    }

                    val dividerItemDecoration = DividerItemDecoration(this@MessageComposerActivity, LinearLayoutManager.VERTICAL)
                    attachmentRecyclerView.addItemDecoration(dividerItemDecoration)
                    attachmentAdapter = UploadAttachmentsAdapter(onAttachmentCrossClick)
                    attachmentRecyclerView.adapter = attachmentAdapter
                }

    }

    private fun setUpObservers() {
        messageComposerViewModel.postMessages.observe(this@MessageComposerActivity, Observer {
            it?.let {
                displayPostMessageHandler(it)
            } ?: run {
                showDialogWithMessage(this@MessageComposerActivity, R.string.post_message_internal_error, "")
            }
            resetView()
        })

        messageComposerViewModel.postMessageError.observe(this@MessageComposerActivity, Observer {
            it?.let {
                showDialogWithMessage(this@MessageComposerActivity, R.string.post_message_internal_error, it)
            } ?: run {
                showDialogWithMessage(this@MessageComposerActivity, R.string.post_message_internal_error, "")
            }
            resetView()
        })

        messageComposerViewModel.fetchMembershipsLiveData.observe(this@MessageComposerActivity, Observer {  memberships ->
            memberships?.let {
                if(isMentionsEnabled && it.size > MINIMUM_MEMBERS_REQUIRED_FOR_MENTIONS ) {
                    binding.message.addAutoCompletePlugin(MentionsPlugin(this@MessageComposerActivity, this, messageComposerViewModel))
                }
            }
        })

        messageComposerViewModel.editMessage.observe(this@MessageComposerActivity, Observer {
            it?.let {
                showDialogWithMessage(this@MessageComposerActivity, null, getString(R.string.message_edit_successful))
            } ?: run {
                showDialogWithMessage(this@MessageComposerActivity, null, getString(R.string.edit_message_internal_error))
            }
            resetView()
        })
    }

    private fun openFileExplorer() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICKFILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleActivityResult(requestCode, resultCode, data)
    }

    private fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                intent.clipData?.let { data ->
                    for (index in 0 until data.itemCount) {
                        val uri = data.getItemAt(index).uri
                        addUriToList(uri)
                    }
                } ?: run {
                    intent.data?.let { uri ->
                        addUriToList(uri)
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addUriToList(uri: Uri) {
        val filePath = getPath(this@MessageComposerActivity, uri)
        val file = File(filePath)
        Log.d(tag, "PICKFILE_REQUEST_CODE filePath: $filePath")
        Log.d(tag, "PICKFILE_REQUEST_CODE file Exist: ${file.exists()}")

        attachmentAdapter.attachedFiles.add(file)
        attachmentAdapter.notifyDataSetChanged()
    }

    private fun processAttachmentFiles(): ArrayList<LocalFile> {
        val files = ArrayList<LocalFile>()

        for (file in attachmentAdapter.attachedFiles) {
            var thumbnail: LocalFile.Thumbnail? = null
            if (MimeUtils.getContentTypeByFilename(file.name) == MimeUtils.ContentType.IMAGE) {
                thumbnail = LocalFile.Thumbnail(file, null, resources.getInteger(R.integer.attachment_thumbnail_width), resources.getInteger(R.integer.attachment_thumbnail_height))
            }
            val localFile = LocalFile(file, null, thumbnail, null)
            files.add(localFile)
        }

        return files
    }

    private fun sendButtonClicked() {
        if (binding.message.text.isEmpty() && attachmentAdapter.attachedFiles.isEmpty()) {
            showDialogWithMessage(this@MessageComposerActivity, R.string.post_message_error, getString(R.string.post_message_empty_error))
        } else {
            messageId?.let {
                // Edit message flow
                editMessage(it) }
                    ?: composerType.let { type ->
                        id?.let {
                            val files = processAttachmentFiles()
                            when (type) {
                                ComposerType.POST_SPACE -> {
                                    postToSpace(it, files)
                                }
                                ComposerType.POST_PERSON_ID -> {
                                    postPersonById(it, files)
                                }
                                ComposerType.POST_PERSON_EMAIL -> {
                                    postPersonByEmail(it, files)
                                }
                            }
                        }
                    }
        }
    }

    private fun buildMessageText(message: String) : Message.Text{
        return when(styleType){
            StyleType.HTML_TEXT -> Message.Text.html(message)
            StyleType.MARKDOWN_TEXT -> Message.Text.markdown(message)
            else -> {
                Message.Text.plain(message)
            }
        }
    }

    private fun editMessage(messageId: String) {
        val str = binding.message.text.toString()
        val messageContent = binding.message.getMessageContent()
        val text: Message.Text = buildMessageText(str);

        messageComposerViewModel.editMessage(messageId, text, messageContent.messageInputMentions)
    }

    private fun displayPostMessageHandler(message: Message) {
        val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(this)

        builder.setTitle(R.string.message_details)

        DialogPostMessageHandlerBinding.inflate(layoutInflater)
                .apply {
                    messageData = message
                    val msg = message.getTextAsObject()

                    msg.getHtml()?.let {
                        messageBodyTextView.text = Html.fromHtml(msg.getHtml(), Html.FROM_HTML_MODE_LEGACY)
                    } ?: run {
                        msg.getPlain()?.let {
                            messageBodyTextView.text = msg.getPlain()
                        }
                    }
                    builder.setView(this.root)
                    builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                    }

                    builder.show()
                }
    }

    private fun postPersonByEmail(email: String, files: ArrayList<LocalFile>?) {
        val emailAddress = EmailAddress.fromString(email)
        emailAddress?.let {
            val text: Message.Text = buildMessageText(binding.message.text.toString());
            messageComposerViewModel.postToPerson(emailAddress, text, files)
            showProgress()
        } ?: run {
            showDialogWithMessage(this@MessageComposerActivity, R.string.post_message_error, getString(R.string.post_message_email_empty))
        }
    }

    private fun postPersonById(personId: String, files: ArrayList<LocalFile>?) {
        val text: Message.Text = buildMessageText(binding.message.text.toString());
        messageComposerViewModel.postToPerson(personId, text, files)
        showProgress()
    }

    private fun postToSpace(spaceId: String, files: ArrayList<LocalFile>?) {
        val messageContent = binding.message.getMessageContent()

        var progress = true
        val text: Message.Text = buildMessageText(binding.message.text.toString());
        replyParentMessage?.let { replyMessage ->
            text.let { msgTxt ->
                val draft = Message.draft(msgTxt)

                messageContent.messageInputMentions?.let { mentionsArray ->
                    for (item in mentionsArray) {
                        draft.addMentions(item)
                    }
                }

                files?.let { filesArray ->
                    for (item in filesArray) {
                        draft.addAttachments(item)
                    }
                }

                draft.setParent(replyMessage.getMessage())

                messageComposerViewModel.postMessageDraft(spaceId, draft)
            }
        } ?: run {
            messageComposerViewModel.postToSpace(spaceId, text, messageContent.messageInputMentions, files)
        }

        if (progress) {
            showProgress()
        }
    }

    private fun showProgress() {
        binding.progressLayout.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressLayout.visibility = View.GONE
    }

    private fun resetView() {
        binding.message.text.clear()
        hideKeyboard(binding.message)
        attachmentAdapter.attachedFiles.clear()
        attachmentAdapter.notifyDataSetChanged()
        hideProgress()
    }

    private fun checkReadStoragePermissions(): Boolean {
        if (!permissionsHelper.hasReadStoragePermission()) {
            Log.d(tag, "requesting read permission")
            requestPermissions(PermissionsHelper.permissionForStorage(), PermissionsHelper.PERMISSIONS_STORAGE_REQUEST)
            return true
        } else {
            Log.d(tag, "read permission granted")
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PermissionsHelper.PERMISSIONS_STORAGE_REQUEST -> {
                if (PermissionsHelper.resultForCallingPermissions(permissions, grantResults)) {
                    Log.d(tag, "read permission granted")
                    openFileExplorer()
                } else {
                    Toast.makeText(this@MessageComposerActivity, getString(R.string.post_message_attach_permission_error), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    class UploadAttachmentsAdapter(private val onAttachmentCrossClick: (File) -> Unit) : RecyclerView.Adapter<UploadAttachmentsAdapter.AttachmentViewHolder>() {
        var attachedFiles: MutableList<File> = mutableListOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadAttachmentsAdapter.AttachmentViewHolder {
            val binding = ListItemUploadAttachmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AttachmentViewHolder(binding, onAttachmentCrossClick)
        }

        override fun getItemCount(): Int {
            return attachedFiles.size
        }

        override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
            holder.bind(attachedFiles[position])
        }

        inner class AttachmentViewHolder(private val binding: ListItemUploadAttachmentBinding, private val onAttachmentCrossClick: (File) -> Unit) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.buttonLayout.setOnClickListener {
                    onAttachmentCrossClick(attachedFiles[adapterPosition])
                }
            }

            fun bind(file: File) {
                binding.name.text = file.name
                binding.path.text = file.path
            }
        }
    }
}