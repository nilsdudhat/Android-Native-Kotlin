package com.belive.dating.activities.edit_profile.lifestyles.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditWorkoutBinding

/**
 * Adapter for the RecyclerView displaying a list of workouts for editing.
 *
 * This adapter handles the display of workout items, including highlighting the selected workout
 * and handling click events to notify the listener.  It utilizes Data Binding for efficient
 * view updates.
 *
 * @property callBack An interface listener to handle workout item click events.
 * @property viewModel The [EditWorkoutViewModel] providing the workout data and selected state.
 */
class EditWorkoutAdapter(
    private var callBack: OnWorkoutChangeListener,
    private val viewModel: EditWorkoutViewModel,
) : RecyclerView.Adapter<EditWorkoutAdapter.ViewHolder>() {

    interface OnWorkoutChangeListener {
        fun onWorkoutClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workoutHabit = viewModel.workoutList[position]
        holder.binding.position = position
        holder.binding.workoutHabit = workoutHabit
        holder.binding.isSelected = viewModel.selectedWorkout.get() == workoutHabit
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callBack.onWorkoutClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.workoutList.size
    }

    class ViewHolder(val binding: ItemEditWorkoutBinding) : RecyclerView.ViewHolder(binding.root)
}