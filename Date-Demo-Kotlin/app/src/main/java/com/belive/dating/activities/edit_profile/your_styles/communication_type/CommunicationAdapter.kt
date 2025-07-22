package com.belive.dating.activities.edit_profile.your_styles.communication_type

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditCommunicationBinding

/**
 *  A RecyclerView adapter for displaying and handling interactions with a list of communication types.
 *  It allows users to select communication types from a list, visually indicating the selection state.
 *
 *  @property viewModel The [EditCommunicationViewModel] that holds the data (communication list and selected items) for the adapter.
 *  @property listener  An implementation of the [CommunicationClickListener] interface to handle communication item clicks.
 */
class CommunicationAdapter(val viewModel: EditCommunicationViewModel, val listener: CommunicationClickListener) :
    RecyclerView.Adapter<CommunicationAdapter.ViewHolder>() {

    interface CommunicationClickListener {
        fun onCommunicationClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditCommunicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val communicationType = viewModel.communicationList[position]
        holder.binding.communication = communicationType
        holder.binding.isSelected = viewModel.selectedCommunicationList.get()?.contains(communicationType) ?: false
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            listener.onCommunicationClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.communicationList.size
    }

    class ViewHolder(val binding: ItemEditCommunicationBinding) : RecyclerView.ViewHolder(binding.root)
}