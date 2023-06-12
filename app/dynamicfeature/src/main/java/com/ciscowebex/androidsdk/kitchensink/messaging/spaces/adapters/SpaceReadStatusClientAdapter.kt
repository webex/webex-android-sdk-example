package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemSpacesReadClientBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceReadStatusModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.readStatusDetails.SpaceReadStatusDetailActivity

class SpaceReadStatusClientAdapter : RecyclerView.Adapter<SpacesReadClientViewHolder>() {
    var spaceReadStatusList: List<SpaceReadStatusModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpacesReadClientViewHolder {
        return SpacesReadClientViewHolder(ListItemSpacesReadClientBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = spaceReadStatusList.size

    override fun onBindViewHolder(holder: SpacesReadClientViewHolder, position: Int) {
        holder.bind(spaceReadStatusList[position])
    }

}

class SpacesReadClientViewHolder(private val binding: ListItemSpacesReadClientBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(spaceReadStatus: SpaceReadStatusModel) {
        binding.spaceReadStatus = spaceReadStatus

        binding.spaceReadStatusClientLayout.setOnClickListener {view ->
            ContextCompat.startActivity(view.context ,SpaceReadStatusDetailActivity.getIntent(view.context, spaceReadStatus.spaceId), null)
        }

        binding.executePendingBindings()
    }
}