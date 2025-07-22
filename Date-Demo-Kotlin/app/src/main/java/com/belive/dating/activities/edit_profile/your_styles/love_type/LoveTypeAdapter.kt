package com.belive.dating.activities.edit_profile.your_styles.love_type

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditLoveTypeBinding

/**
 * Adapter for displaying a list of love types in a RecyclerView.
 *  Each item displays a love type and visually indicates if it is selected.
 *  Handles user clicks on love type items via a listener.
 *
 * @property viewModel The ViewModel (`EditLoveTypeViewModel`) that holds the list of love types (`loveTypeList`)
 *                   and manages the selection state (`selectedLoveTypeList`).  The adapter observes and reflects changes
 *                   to the selected love types managed by the ViewModel.
 * @property listener The listener (`LoveTypeClickListener`) to handle clicks on individual love type items.
 *                   When an item is clicked, the listener's `onLoveTypeClick` method is invoked with the position
 *                   of the clicked item in the list.
 */
class LoveTypeAdapter(val viewModel: EditLoveTypeViewModel, val listener: LoveTypeClickListener) :
    RecyclerView.Adapter<LoveTypeAdapter.ViewHolder>() {

    interface LoveTypeClickListener {
        fun onLoveTypeClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditLoveTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loveType = viewModel.loveTypeList[position]
        holder.binding.loveType = loveType
        holder.binding.isSelected = viewModel.selectedLoveTypeList.get()?.contains(loveType) ?: false
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            listener.onLoveTypeClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.loveTypeList.size
    }

    class ViewHolder(val binding: ItemEditLoveTypeBinding) : RecyclerView.ViewHolder(binding.root)
}