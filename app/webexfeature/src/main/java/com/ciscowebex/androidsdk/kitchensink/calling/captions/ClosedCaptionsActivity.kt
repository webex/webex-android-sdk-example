package com.ciscowebex.androidsdk.kitchensink.calling.captions

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
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.ArrayList
import java.util.Collections

class ClosedCaptionsActivity : BaseActivity() {

    val captionsViewModel: ClosedCaptionsViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closed_captions)
        val recyclerView = findViewById<RecyclerView>(R.id.ccRecyclerView)
        findViewById<TextView>(R.id.title).text = getString(R.string.closed_captions)

        var captions = intent?.getParcelableArrayListExtra<CaptionData>(Constants.Intent.CLOSED_CAPTION_DATA)
        captionsViewModel.captions.observe(this@ClosedCaptionsActivity) { caption ->
            if (captions == null) {
                captions = ArrayList(1)
                updateViews(captions)
            }

            captions?.let {
                if (it[it.size - 1] != caption)
                    it.add(caption)
                    recyclerView.adapter?.let { recyclerView ->
                        recyclerView.notifyItemInserted(recyclerView.itemCount - 1)
                }
            }
        }
        updateViews(captions)
    }

    fun updateViews(captions: ArrayList<CaptionData>?) {
        val recyclerView = findViewById<RecyclerView>(R.id.ccRecyclerView)
        val noDataView = findViewById<TextView>(R.id.ccNoData)
        if (captions.isNullOrEmpty()) {
            noDataView.visibility = View.VISIBLE
            recyclerView.visibility = View.INVISIBLE
        } else {
            noDataView.visibility = View.INVISIBLE
            recyclerView.visibility = View.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = CaptionAdapter(captions)
        }
    }

    class CaptionAdapter(private val captions: ArrayList<CaptionData>) :
        RecyclerView.Adapter<CaptionAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val primaryTextView: TextView = itemView.findViewById(R.id.primaryText)
            val subTextView: TextView = itemView.findViewById(R.id.subText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_closed_caption, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val caption = captions[position]
            holder.primaryTextView.text = "${caption.name} ${caption.timestamp}"
            holder.subTextView.text = caption.content
        }

        override fun getItemCount(): Int {
            return captions.size
        }
    }
}