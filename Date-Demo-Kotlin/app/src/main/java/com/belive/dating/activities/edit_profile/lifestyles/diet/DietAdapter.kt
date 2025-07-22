package com.belive.dating.activities.edit_profile.lifestyles.diet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditDietBinding
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * Adapter for displaying a list of diets in a RecyclerView.
 *
 * This adapter handles displaying diet options and managing user selection.  It uses a FlexboxLayoutManager
 * to allow for flexible arrangement of items within the layout.  Clicking a diet item triggers a callback
 * to the provided [DietClickListener].
 *
 * @property viewModel The [EditDietViewModel] that provides the list of diets and the currently selected diet.
 * @property listener The [DietClickListener] to handle clicks on individual diet items.
 */
class DietAdapter(val viewModel: EditDietViewModel, val listener: DietClickListener) : RecyclerView.Adapter<DietAdapter.ViewHolder>() {

    interface DietClickListener {
        fun onDietClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditDietBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lp: ViewGroup.LayoutParams = holder.binding.main.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.flexGrow = 1.0f
            lp.alignSelf = AlignSelf.AUTO
        }

        val diet = viewModel.dietList[position]
        holder.binding.diet = diet
        holder.binding.isSelected = viewModel.selectedDiet.get() == diet
        holder.binding.executePendingBindings()

        holder.binding.main.setOnClickListener {
            listener.onDietClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.dietList.size
    }

    class ViewHolder(val binding: ItemEditDietBinding) : RecyclerView.ViewHolder(binding.root)
}