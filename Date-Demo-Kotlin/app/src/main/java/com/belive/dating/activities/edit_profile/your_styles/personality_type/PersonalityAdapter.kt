package com.belive.dating.activities.edit_profile.your_styles.personality_type

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemEditPersonalityBinding
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * Adapter for displaying and interacting with a list of personalities in a RecyclerView.
 *
 * This adapter is used in the context of editing user personalities, allowing the user
 * to select or deselect personalities from a list. It utilizes FlexboxLayoutManager for
 * flexible layout of personality items.
 *
 * @property viewModel The ViewModel that holds the personality data and selection state.
 * @property listener A listener to handle clicks on personality items.
 */
class PersonalityAdapter(val viewModel: EditPersonalityViewModel, val listener: PersonalityClickListener) :
    RecyclerView.Adapter<PersonalityAdapter.ViewHolder>() {

    interface PersonalityClickListener {
        fun onPersonalityClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditPersonalityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lp: ViewGroup.LayoutParams = holder.binding.main.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.flexGrow = 1.0f
            lp.alignSelf = AlignSelf.AUTO
        }

        val personality = viewModel.personalityList[position]
        holder.binding.personality = personality
        holder.binding.isSelected = viewModel.selectedPersonalityList.get()?.contains(personality) ?: false
        holder.binding.executePendingBindings()

        holder.binding.main.setOnClickListener {
            listener.onPersonalityClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.personalityList.size
    }

    class ViewHolder(val binding: ItemEditPersonalityBinding) : RecyclerView.ViewHolder(binding.root)
}