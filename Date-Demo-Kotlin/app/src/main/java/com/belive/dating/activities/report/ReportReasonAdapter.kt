package com.belive.dating.activities.report

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.databinding.ItemReportReasonBinding

class ReportReasonAdapter(private val onReasonClickListener: OnReasonClickListener) :
    RecyclerView.Adapter<ReportReasonAdapter.ViewHolder>() {

    interface OnReasonClickListener {
        fun onReasonClicked(reasonPosition: Int)
    }

    var reasonList = arrayListOf<String>()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReportReasonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.itemReason.style.borderColor = if (selectedPosition == position) {
            holder.binding.root.context.getColor(R.color.primary_color)
        } else {
            holder.binding.root.context.getColor(R.color.boxBorder)
        }
        holder.binding.isSelected = selectedPosition == position
        holder.binding.reason = reasonList[position]

        holder.binding.itemReason.setOnClickListener {
            onReasonClickListener.onReasonClicked(holder.bindingAdapterPosition)
        }
    }

    fun setSelection(position: Int) {
        if (selectedPosition == -1) {
            selectedPosition = position
            notifyItemChanged(selectedPosition)
        } else {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int {
        return reasonList.size
    }

    class ViewHolder(val binding: ItemReportReasonBinding) : RecyclerView.ViewHolder(binding.root)
}