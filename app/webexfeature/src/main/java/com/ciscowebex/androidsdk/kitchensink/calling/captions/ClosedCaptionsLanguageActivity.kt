package com.ciscowebex.androidsdk.kitchensink.calling.captions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.Constants.Intent.CLOSED_CAPTION_LANGUAGE_ITEM

class ClosedCaptionsLanguageActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closed_captions)
        val recyclerView = findViewById<RecyclerView>(R.id.ccRecyclerView)
        val noDataView = findViewById<TextView>(R.id.ccNoData)
        findViewById<TextView>(R.id.title).text = getString(R.string.cc_select_language)

        val languages =
            intent?.getParcelableArrayListExtra<LanguageData>(Constants.Intent.CLOSED_CAPTION_LANGUAGES)
        if (languages.isNullOrEmpty()) {
            noDataView.visibility = View.VISIBLE
            recyclerView.visibility = View.INVISIBLE
        } else {
            noDataView.visibility = View.INVISIBLE
            recyclerView.visibility = View.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = CaptionLanguageAdapter(languages) {
                sendResult(it)
            }
        }
    }

    private fun sendResult(languageItem: LanguageData) {
        val resultIntent = Intent()
        resultIntent.putExtra(CLOSED_CAPTION_LANGUAGE_ITEM, languageItem)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    class CaptionLanguageAdapter(
        private val languages: List<LanguageData>,
        private val onItemClickListener: (LanguageData) -> Unit
    ) :
        RecyclerView.Adapter<CaptionLanguageAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val primaryTextView: TextView = itemView.findViewById(R.id.primaryText)
            val subTextView: TextView = itemView.findViewById(R.id.subText)
            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val language = languages[position]
                        onItemClickListener(language)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_closed_caption, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val language = languages[position]
            holder.primaryTextView.text = "${language.titleInEnglish}"
            holder.subTextView.text = language.title
        }

        override fun getItemCount(): Int {
            return languages.size
        }
    }
}