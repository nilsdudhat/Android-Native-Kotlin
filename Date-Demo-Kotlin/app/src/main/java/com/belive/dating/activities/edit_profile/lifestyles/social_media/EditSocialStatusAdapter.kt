package com.belive.dating.activities.edit_profile.lifestyles.social_media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditSocialStatusBinding

/**
 * Adapter for displaying and selecting social status options in a RecyclerView.
 *
 * This adapter populates a RecyclerView with a list of social status options. It allows the user
 * to select a single social status, highlighting the currently selected option.  Interaction
 * is handled through an [OnSocialStatusChangeListener] interface.
 *
 * @property callBack An instance of [OnSocialStatusChangeListener] to handle social status selection events.
 * @property viewModel The [EditSocialStatusViewModel] that provides the list of social statuses and the currently selected status.
 */
class EditSocialStatusAdapter(
    private var callBack: OnSocialStatusChangeListener,
    private val viewModel: EditSocialStatusViewModel,
) : RecyclerView.Adapter<EditSocialStatusAdapter.ViewHolder>() {

    interface OnSocialStatusChangeListener {
        fun onSocialStatusClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditSocialStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val socialStatus = viewModel.statusList[position]
        holder.binding.position = position
        holder.binding.socialStatus = socialStatus
        holder.binding.isSelected = viewModel.selectedStatus.get() == socialStatus
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callBack.onSocialStatusClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.statusList.size
    }

    class ViewHolder(val binding: ItemEditSocialStatusBinding) : RecyclerView.ViewHolder(binding.root)
}