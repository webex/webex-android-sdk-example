package com.ciscowebex.androidsdk.kitchensink.messaging.composer

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.BaseAdapter
import android.widget.ListPopupWindow
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemMentionBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipModel
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.utf8Offset
import com.ciscowebex.androidsdk.message.Mention

class MessageContent() {
    var text: String = ""
    var messageInputMentions: ArrayList<Mention>? = null
}

data class Filter(val text: String, val position: Int)

interface AutoCompletePlugin {
    fun onFilterChanged(text: String)
    fun shouldTrigger(filter: Filter): Boolean
    fun getAdapter(): BaseAdapter
    fun itemSelected(position: Int, editText: MentionsAutoCompleteEditText, filter: Filter)
    fun hasItems(): Boolean
}

interface BackPressedListener {
    fun onImeBack(editText: MentionsAutoCompleteEditText)
}

interface CreateInputConnectionListener {
    fun onCreateInputConnection(outAttrs: EditorInfo, inputConnection: InputConnection?): InputConnection
}

class MentionSpan(val context: Context, val mention: Mention) : ClickableSpan() {
    override fun onClick(widget: View) {
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = getMentionTextColor()
        ds.isFakeBoldText = false
        ds.bgColor = ContextCompat.getColor(context, R.color.blue_40) // add mentions highlight
    }

    @ColorInt
    private fun getMentionTextColor() = ContextCompat.getColor(context, R.color.white)
}

class MentionsAutoCompleteEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        AppCompatEditText(context, attrs, defStyle) {

    companion object {
        val MAX_LINES = 4
        val TAG = MentionsAutoCompleteEditText::class.java.simpleName
    }

    var backPressListener: BackPressedListener? = null
    var createInputConnectionListener: CreateInputConnectionListener? = null

    var popup: ListPopupWindow? = null
    val plugins: MutableList<AutoCompletePlugin> = arrayListOf()

    init {
    }

    fun getMessageContent(): MessageContent {
        return MessageContent().apply {
            text = getText().toString()
            if(getText().getSpans(0, getText().length, MentionSpan::class.java).isNotEmpty()){
                messageInputMentions = ArrayList()
            }
            for (span in getText().getSpans(0, getText().length, MentionSpan::class.java)) {
                try {
                    span.mention.start = this.text.utf8Offset(span.mention.start)
                    span.mention.end = this.text.utf8Offset(span.mention.end)
                    messageInputMentions?.add(span.mention)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e(TAG, e.message.orEmpty())
                    // Remove mentions that exist outside the bounds of the message text
                    continue
                }
            }
        }
    }

    override fun getText(): Editable {
        return super.getText() ?: Editable.Factory.getInstance().newEditable("")
    }

    private fun dismissPopup() {
        popup?.dismiss()
        popup = null
    }

    private fun getFilter(): Filter? {
        val position = selectionStart
        var start = 0
        for (i in (position - 1) downTo 0) {
            if (Character.isWhitespace(text[i])) {
                start = Math.min(i + 1, position)
                break
            }
        }
        if (text.getSpans(position, position, MentionSpan::class.java).isEmpty()) {
            val s = text.subSequence(start, position).toString()
            if (s.isNotEmpty()) {
                return Filter(s, start)
            }
        }
        return null
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        Log.e(TAG, "text : $text")
        // Shift mention span ranges as needed when text is edited
        if (text is Spannable) {
            Log.e(TAG, "text is Spannable")
            text.getSpans(0, text.length, MentionSpan::class.java).filter { span ->
                span.mention.end.toInt() >= start
            }.forEach { span ->
                if (span.mention.start <= start && span.mention.end > start) {

                    Log.e(TAG, "text.removeSpan")
                    text.removeSpan(span)
                } else {
                    // Don't shift the start position if the user is editing a mention
                    if (span.mention.start.toInt() > start) {
                        span.mention.start += lengthAfter - lengthBefore
                    }

                    // Don't shift the end of a span if that's the character that's being removed
                    if (span.mention.end.toInt() != start) {
                        span.mention.end += lengthAfter - lengthBefore
                    }
                }
            }
        }

        val filter = getFilter()

        Log.e(TAG, "filter : ${filter?.text}")
        if (filter == null) {
            Log.e(TAG, "filter is null")
            dismissPopup()
            return
        }

        val plugin = plugins.find { it.shouldTrigger(filter) }

        Log.e(TAG, "plugin : $plugin")
        plugin?.apply {
            onFilterChanged(filter.text)
            if (plugin.hasItems()) {

                Log.e(TAG, "plugin hasItems()")
                if (popup == null) {

                    Log.e(TAG, "popup null showing popup")
                    popup = ListPopupWindow(context).apply {
                        anchorView = this@MentionsAutoCompleteEditText
                        setAdapter(plugin.getAdapter())
                        setOnItemClickListener { _, _, position, _ ->
                            getFilter()?.apply {
                                plugin.itemSelected(position, this@MentionsAutoCompleteEditText, this)
                            }
                            dismissPopup()
                        }
                        show()
                    }
                }
            } else {

                Log.e(TAG, "plugin has no items, dismissPopup()")
                dismissPopup()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dismissPopup()
    }

    fun hasAutoCompletePlugin(): Boolean = plugins.size > 0

    fun addAutoCompletePlugin(plugin: AutoCompletePlugin) {
        plugins.removeAll {
            it.javaClass.isInstance(plugin)
        }
        plugins.add(plugin)
    }

    fun updateEditTextMaxLines(hasText: Boolean) {
        maxLines = if (hasText) MAX_LINES else 1
        ellipsize = if (hasText) null else TextUtils.TruncateAt.END
    }

    fun isEmpty(): Boolean {
        return text.isEmpty()
    }

    fun reset() {
        setText("")
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            backPressListener?.apply {
                onImeBack(this@MentionsAutoCompleteEditText)
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val ic = super.onCreateInputConnection(outAttrs)
        createInputConnectionListener?.apply {
            return onCreateInputConnection(outAttrs, ic)
        }
        return ic
    }
}

class MentionsPlugin(
        val lifecycleOwner: LifecycleOwner,
        val context: Context,
        val messageComposerViewModel: MessageComposerViewModel
) : AutoCompletePlugin {
    val adapter = MentionsPopupAdapter()

    override fun getAdapter(): BaseAdapter = adapter
    override fun hasItems(): Boolean = adapter.count > 0

    override fun onFilterChanged(text: String) {
        adapter.filter(text)
    }

    override fun shouldTrigger(filter: Filter): Boolean {
        val shouldTrigger = filter.text[0] == '@'

        Log.e(MentionsAutoCompleteEditText.TAG, "shouldTrigger : $shouldTrigger")
        return shouldTrigger
    }

    override fun itemSelected(position: Int, editText: MentionsAutoCompleteEditText, filter: Filter) {
        val mentionable = adapter.getItem(position) as MembershipModel? ?: return
        val shortText = mentionable.personFirstName
        // Temporary work around for checking if there are duplicate first names, this would hopefully be something that would be returned to us
        // by the view model
        val text = if (adapter.firstNameCheck(shortText).size > 1) getFullNameIfLastFirstFormat(mentionable.personDisplayName) else shortText
        editText.text.replace(filter.position, filter.position + filter.text.length, text)
        val endMention = filter.position + text.length
        if (editText.text.length == endMention || editText.text.length > endMention && !Character.isWhitespace(editText.text[endMention])) {
            editText.text.insert(endMention, " ")
        }

        val mention = when (position) {
            0 -> {
                Mention.All(filter.position, endMention)
            }
            else -> {
                Mention.Person(mentionable.personId).apply {
                    start = filter.position
                    end = endMention
                }
            }
        }
        val span = MentionSpan(context, mention)

        editText.text.setSpan(span, mention.start, mention.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun getFullNameIfLastFirstFormat(displayName: String): String {
        return if (displayName.contains(",")) {
            displayName.split(",")[1].trim() + " " + displayName.split(",")[0].trim()
        } else {
            displayName
        }
    }

    inner class MentionsPopupAdapter : BaseAdapter() {
        val inflator = LayoutInflater.from(context)
        var mentions = mutableListOf<MembershipModel>()
        var currentSearch = ""

        override fun getItem(position: Int): Any? {
            if (position >= mentions.size) {
                return null
            }
            return mentions[position]
        }

        override fun getItemId(position: Int): Long {
            if (position >= mentions.size) {
                return 0L
            }
            return mentions[position].hashCode().toLong()
        }

        override fun getCount(): Int = mentions.size

        fun filter(filter: String) {
            mentions.clear()
            notifyDataSetChanged()
            currentSearch = filter
            mentions = messageComposerViewModel.search(filter).toMutableList()

            Log.e(MentionsAutoCompleteEditText.TAG, "mentions.size : ${mentions.size}")
            notifyDataSetChanged()
        }

        fun firstNameCheck(firstName: String): MutableList<MembershipModel> = messageComposerViewModel.search(firstName).toMutableList()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val viewHolder = if (convertView == null) {
                ViewHolder(ListItemMentionBinding.inflate(inflator))
            } else {
                convertView.tag as ViewHolder
            }
            return viewHolder.bind(mentions[position])
        }

        inner class ViewHolder(val binding: ListItemMentionBinding) {
            init {
                binding.root.tag = this
            }

            fun bind(item: MembershipModel): View {
                binding.apply {
                    lifecycleOwner = this@MentionsPlugin.lifecycleOwner
                    membership = item
                }
                binding.executePendingBindings()
                return binding.root
            }
        }
    }
}