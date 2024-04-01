package com.udemy.kotlin.market.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.udemy.kotlin.market.app.R
import com.udemy.kotlin.market.app.interfaces.ItemClickListener
import com.udemy.kotlin.market.app.models.Item

class ItemAdapter(
    private val itemList: List<Item>,
    private val itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_market, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Item = itemList[position]

        holder.imgItem.setImageResource(item.itemImg)
        holder.txtTitle.text = item.itemName
        holder.txtDescription.text = item.itemDescription

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(holder.itemView, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgItem: ImageView = itemView.findViewById(R.id.img_item)
        val txtTitle: TextView = itemView.findViewById(R.id.txt_title)
        val txtDescription: TextView = itemView.findViewById(R.id.txt_desc)
    }
}