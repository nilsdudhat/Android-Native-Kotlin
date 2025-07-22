package com.belive.dating.activities.edit_profile.lifestyles.pets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditPetBinding
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * Adapter for displaying a list of pets in a RecyclerView, specifically designed for an editing context.
 * Each pet is represented by a [Pet] object and rendered using the [ItemEditPetBinding] layout.
 *
 * This adapter uses a [FlexboxLayoutManager] for flexible layout, allowing pets to wrap and grow within their container.
 * It also manages the visual selection state of pets and handles click events.
 *
 * @property viewModel The [EditPetViewModel] providing the list of pets and selected pet state.  It is assumed that `viewModel.petList`
 *                   holds the list of [Pet] objects and `viewModel.selectedPet` is a [androidx.databinding.ObservableField]
 *                   or similar observable that holds the currently selected [Pet].  Changes to `selectedPet` should
 *                   trigger a UI update in the adapter.
 * @property listener A [PetClickListener] to handle click events on individual pet items.  The listener will be notified
 *                   with the adapter position of the clicked pet.
 */
class PetAdapter(val viewModel: EditPetViewModel, val listener: PetClickListener) : RecyclerView.Adapter<PetAdapter.ViewHolder>() {

    interface PetClickListener {
        fun onPetClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lp: ViewGroup.LayoutParams = holder.binding.main.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.flexGrow = 1.0f
            lp.alignSelf = AlignSelf.AUTO
        }

        val pet = viewModel.petList[position]
        holder.binding.pet = pet
        holder.binding.isSelected = viewModel.selectedPet.get() == pet
        holder.binding.executePendingBindings()

        holder.binding.main.setOnClickListener {
            listener.onPetClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.petList.size
    }

    class ViewHolder(val binding: ItemEditPetBinding) : RecyclerView.ViewHolder(binding.root)
}