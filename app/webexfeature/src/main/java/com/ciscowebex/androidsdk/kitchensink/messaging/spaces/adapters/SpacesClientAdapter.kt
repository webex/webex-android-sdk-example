package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemSpacesClientBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceActionBottomSheetFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail.SpaceDetailActivity


class SpacesClientAdapter(private val optionsDialogFragment: SpaceActionBottomSheetFragment, val supportFragmentManager: FragmentManager) : RecyclerView.Adapter<SpacesClientViewHolder>() {
    var spaces: MutableList<SpaceModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpacesClientViewHolder {
        return SpacesClientViewHolder(ListItemSpacesClientBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                optionsDialogFragment, supportFragmentManager)
    }

    override fun getItemCount(): Int = spaces.size

    override fun onBindViewHolder(holder: SpacesClientViewHolder, position: Int) {
        holder.bind(spaces[position])
    }

    fun getPositionById(spaceId: String): Int {
        return spaces.indexOfFirst { it.id == spaceId }
    }

}

class SpacesClientViewHolder(private val binding: ListItemSpacesClientBinding,
                             private val optionsDialogFragment: SpaceActionBottomSheetFragment,
                             private val supportFragmentManager: FragmentManager) : RecyclerView.ViewHolder(binding.root) {

    fun bind(space: SpaceModel) {
        binding.space = space
        binding.spaceTitleLabel.setOnClickListener { view ->
            startSpaceDetailActivity(view, space)
        }
        binding.spaceTitleTextView.setOnClickListener { view ->
            startSpaceDetailActivity(view, space)
        }
        binding.spaceTitleLabel.setOnLongClickListener { view ->
            showSpaceOptions(space, view)
        }
        binding.spaceTitleTextView.setOnLongClickListener { view ->
            showSpaceOptions(space, view)
        }
        binding.executePendingBindings()
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