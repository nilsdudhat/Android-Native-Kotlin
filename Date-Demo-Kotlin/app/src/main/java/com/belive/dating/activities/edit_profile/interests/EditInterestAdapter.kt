package com.belive.dating.activities.edit_profile.interests

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.InterestData
import com.belive.dating.databinding.ItemInterestBinding
import com.belive.dating.extensions.getKoinContext
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * Adapter for displaying and editing interests in a RecyclerView.
 *
 * This adapter uses an [AsyncListDiffer] to efficiently update the list of interests.
 * It supports selecting/deselecting interests with a maximum selection limit.  It utilizes
 * a [FlexboxLayoutManager] to display interests in a flexible grid.
 *
 * @property callBack An [OnInterestListener] to handle interest selection events.
 */
class EditInterestAdapter(private val callBack: OnInterestListener) :
    RecyclerView.Adapter<EditInterestAdapter.ViewHolder>() {

    interface OnInterestListener {
        fun onInterestClick(list: ArrayList<Int>)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<InterestData>() {
        override fun areItemsTheSame(oldItem: InterestData, newItem: InterestData): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name) && (oldItem.isChecked == newItem.isChecked) && (oldItem.icon == newItem.icon)
        }

        override fun areContentsTheSame(oldItem: InterestData, newItem: InterestData): Boolean {
            return oldItem == newItem
        }
    }

    var asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemInterestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lp: ViewGroup.LayoutParams = holder.binding.main.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.flexGrow = 1.0f
            lp.alignSelf = AlignSelf.AUTO
        }

        holder.binding.interestData = asyncListDiffer.currentList[position]
        holder.binding.executePendingBindings()

        holder.binding.main.setOnClickListener {
            asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked =
                if (asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked == null) true else !asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked!!

            if (asyncListDiffer.currentList.count { it.isChecked == true } > 12) {
                asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked =
                    !asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked!!

                Toast.makeText(getKoinContext(), "You can select maximum of 12 interests", Toast.LENGTH_SHORT).show()
            } else {
                holder.binding.interestData = asyncListDiffer.currentList[holder.bindingAdapterPosition]
                callBack.onInterestClick(getSelected())
            }
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    private fun getSelected(): ArrayList<Int> {
        val selected = arrayListOf<Int>()
        for (i in asyncListDiffer.currentList.indices) {
            if (asyncListDiffer.currentList[i].isChecked == true) {
                selected.add(asyncListDiffer.currentList[i].id)
            }
        }
        return selected
    }

    class ViewHolder(val binding: ItemInterestBinding) : RecyclerView.ViewHolder(binding.root)
}