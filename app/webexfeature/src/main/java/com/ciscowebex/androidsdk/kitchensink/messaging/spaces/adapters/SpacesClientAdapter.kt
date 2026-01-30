package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceActionBottomSheetFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail.SpaceDetailActivity


class SpacesClientAdapter(private val optionsDialogFragment: SpaceActionBottomSheetFragment, val supportFragmentManager: FragmentManager) : RecyclerView.Adapter<SpacesClientViewHolder>() {
    var spaces: MutableList<SpaceModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpacesClientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_spaces_client, parent, false)
        return SpacesClientViewHolder(view, optionsDialogFragment, supportFragmentManager)
    }

    override fun getItemCount(): Int = spaces.size

    override fun onBindViewHolder(holder: SpacesClientViewHolder, position: Int) {
        holder.bind(spaces[position])
    }

    fun getPositionById(spaceId: String): Int {
        return spaces.indexOfFirst { it.id == spaceId }
    }

}

class SpacesClientViewHolder(
    itemView: View,
    private val optionsDialogFragment: SpaceActionBottomSheetFragment,
    private val supportFragmentManager: FragmentManager
) : RecyclerView.ViewHolder(itemView) {
    
    private val spaceTitleLabel: TextView = itemView.findViewById(R.id.spaceTitleLabel)
    private val spaceTitleTextView: TextView = itemView.findViewById(R.id.spaceTitleTextView)

    fun bind(space: SpaceModel) {
        spaceTitleTextView.text = space.title ?: ""
        
        spaceTitleLabel.setOnClickListener { view ->
            startSpaceDetailActivity(view, space)
        }
        spaceTitleTextView.setOnClickListener { view ->
            startSpaceDetailActivity(view, space)
        }
        spaceTitleLabel.setOnLongClickListener { view ->
            showSpaceOptions(space, view)
        }
        spaceTitleTextView.setOnLongClickListener { view ->
            showSpaceOptions(space, view)
        }
    }

    private fun showSpaceOptions(space: SpaceModel, view: View): Boolean {
        optionsDialogFragment.spaceId = space.id
        optionsDialogFragment.spaceTitle = space.title
        optionsDialogFragment.space = space
        optionsDialogFragment.show(supportFragmentManager, "Space Options")

        return true
    }

    private fun startSpaceDetailActivity(view: View, space: SpaceModel) {
        ContextCompat.startActivity(view.context, SpaceDetailActivity.getIntent(view.context, space.id), null)
    }
}
