package com.udemy.kotlin.market.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.udemy.kotlin.market.app.databinding.ItemMarketBinding
import com.udemy.kotlin.market.app.interfaces.ItemClickListener
import com.udemy.kotlin.market.app.models.Item

class ItemAdapter(
    private val itemList: List<Item>,
    private val itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMarketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Item = itemList[position]

        holder.binding.imgItem.setImageResource(item.itemImg)
        holder.binding.txtTitle.text = item.itemName
        holder.binding.txtDesc.text = item.itemDescription

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(holder.itemView, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(val binding: ItemMarketBinding) : RecyclerView.ViewHolder(binding.root)
}