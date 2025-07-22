package com.belive.dating.activities.edit_profile.lifestyles.smoking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditSmokingBinding

/**
 * Adapter for displaying a list of smoking habits in a RecyclerView.
 *
 * This adapter is used in the context of editing smoking information, allowing the user
 * to select their current smoking habit from a list.  It interacts with an
 * [EditSmokingViewModel] to retrieve and manage the list of smoking habits and the currently
 * selected habit.  It also uses a callback interface, [OnSmokingChangeListener], to
 * notify the host activity or fragment when a smoking habit item is clicked.
 *
 * @property callBack An instance of [OnSmokingChangeListener] that will be notified when a
 *                  smoking habit item is clicked.  This is typically the activity or
 *                  fragment that hosts the RecyclerView.
 * @property viewModel An instance of [EditSmokingViewModel] that provides the data (list of
 *                   smoking habits) and manages the currently selected habit.
 */
class EditSmokingAdapter(
    private var callBack: OnSmokingChangeListener,
    private val viewModel: EditSmokingViewModel,
) : RecyclerView.Adapter<EditSmokingAdapter.ViewHolder>() {

    interface OnSmokingChangeListener {
        fun onSmokingClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditSmokingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val smokingHabit = viewModel.smokingList[position]
        holder.binding.position = position
        holder.binding.smokingHabit = smokingHabit
        holder.binding.isSelected = viewModel.selectedSmoking.get() == smokingHabit
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callBack.onSmokingClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.smokingList.size
    }

    class ViewHolder(val binding: ItemEditSmokingBinding) : RecyclerView.ViewHolder(binding.root)
}