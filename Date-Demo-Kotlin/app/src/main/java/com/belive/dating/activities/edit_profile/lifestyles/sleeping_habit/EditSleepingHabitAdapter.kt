package com.belive.dating.activities.edit_profile.lifestyles.sleeping_habit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditSleepingHabitBinding

/**
 * Adapter for displaying and interacting with a list of sleeping habits in a RecyclerView.
 *
 * This adapter is used in the "Edit Sleeping Habit" screen to allow users to view and select
 * from a predefined list of sleeping habits. It uses data binding to connect the UI elements
 * in each item view to the corresponding data in the `EditSleepingHabitViewModel`.
 *
 * @param callBack An instance of `OnSleepingHabitChangeListener` to handle click events on sleeping habit items.  This allows communication with the activity or fragment hosting the RecyclerView.
 * @param viewModel The `EditSleepingHabitViewModel` instance containing the list of sleeping habits and the currently selected habit.
 */
class EditSleepingHabitAdapter(
    private var callBack: OnSleepingHabitChangeListener,
    private val viewModel: EditSleepingHabitViewModel,
) : RecyclerView.Adapter<EditSleepingHabitAdapter.ViewHolder>() {

    interface OnSleepingHabitChangeListener {
        fun onSleepingHabitClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditSleepingHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sleepingHabit = viewModel.sleepingHabitList[position]
        holder.binding.position = position
        holder.binding.sleepingHabit = sleepingHabit
        holder.binding.isSelected = viewModel.selectedSleepingHabit.get() == sleepingHabit
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callBack.onSleepingHabitClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.sleepingHabitList.size
    }

    class ViewHolder(val binding: ItemEditSleepingHabitBinding) : RecyclerView.ViewHolder(binding.root)
}