package com.belive.dating.activities.edit_profile.lifestyles.drinking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditDrinkingBinding

/**
 * Adapter for displaying and interacting with a list of drinking habit options in a RecyclerView.
 *
 * This adapter is responsible for:
 *  - Inflating the layout for each item in the RecyclerView.
 *  - Binding data from the `EditDrinkingViewModel` to each item view.
 *  - Handling user clicks on drinking habit items and notifying the listener.
 *  - Visually highlighting the currently selected drinking habit.
 *
 * @param callBack An instance of [OnDrinkingChangeListener] to receive click events.
 * @param viewModel The [EditDrinkingViewModel] providing the data and selection state.
 */
class EditDrinkingAdapter(
    private var callBack: OnDrinkingChangeListener,
    private val viewModel: EditDrinkingViewModel,
) : RecyclerView.Adapter<EditDrinkingAdapter.ViewHolder>() {

    interface OnDrinkingChangeListener {
        fun onDrinkingClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditDrinkingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drinkingHabit = viewModel.drinkingList[position]
        holder.binding.position = position
        holder.binding.drinkingHabit = drinkingHabit
        holder.binding.isSelected = viewModel.selectedDrinking.get() == drinkingHabit
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callBack.onDrinkingClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.drinkingList.size
    }

    class ViewHolder(val binding: ItemEditDrinkingBinding) : RecyclerView.ViewHolder(binding.root)
}