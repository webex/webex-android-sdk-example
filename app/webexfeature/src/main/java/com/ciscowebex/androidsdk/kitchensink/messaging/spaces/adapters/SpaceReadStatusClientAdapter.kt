package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceReadStatusModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.readStatusDetails.SpaceReadStatusDetailActivity

class SpaceReadStatusClientAdapter : RecyclerView.Adapter<SpacesReadClientViewHolder>() {
    var spaceReadStatusList: List<SpaceReadStatusModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpacesReadClientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_spaces_read_client, parent, false)
        return SpacesReadClientViewHolder(view)
    }

    override fun getItemCount(): Int = spaceReadStatusList.size

    override fun onBindViewHolder(holder: SpacesReadClientViewHolder, position: Int) {
        holder.bind(spaceReadStatusList[position])
    }

}

class SpacesReadClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val spaceReadStatusClientLayout: ConstraintLayout = itemView.findViewById(R.id.spaceReadStatusClientLayout)
    private val spaceIdTextView: TextView = itemView.findViewById(R.id.spaceIdTextView)
    private val spaceTypeTextView: TextView = itemView.findViewById(R.id.spaceTypeTextView)
    private val spaceUnreadIndicator: View = itemView.findViewById(R.id.spaceUnreadIndicator)
    
    fun bind(spaceReadStatus: SpaceReadStatusModel) {
        spaceIdTextView.text = spaceReadStatus.spaceId ?: ""
        spaceTypeTextView.text = spaceReadStatus.spaceType?.name ?: ""
        spaceUnreadIndicator.visibility = if (spaceReadStatus.isSpaceUnread) View.VISIBLE else View.GONE

        spaceReadStatusClientLayout.setOnClickListener { view ->
            ContextCompat.startActivity(view.context, SpaceReadStatusDetailActivity.getIntent(view.context, spaceReadStatus.spaceId), null)
        }
    }
}
