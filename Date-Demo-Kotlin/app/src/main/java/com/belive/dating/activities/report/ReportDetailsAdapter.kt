package com.belive.dating.activities.report

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemReportReasonDetailsBinding

class ReportDetailsAdapter(val onDetailsClickListener: DetailsClickListener) :
    RecyclerView.Adapter<ReportDetailsAdapter.ViewHolder>() {

    interface DetailsClickListener {
        fun onDetailsClicked()
    }

    var detailsList = hashMapOf<String, ArrayList<String>>()
        set(value) {
            field = value
            notifyItemRangeChanged(0, value.size)
        }

    var sequenceList = hashMapOf<Int, String>()
        set(value) {
            field = value
            notifyItemRangeChanged(0, value.size)
        }

    var selectedPosition = "-1"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReportReasonDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        sequenceList[position].let {
            holder.binding.question = it

            val adapter = ReportReasonAdapter(object : ReportReasonAdapter.OnReasonClickListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onReasonClicked(reasonPosition: Int) {
                    if (selectedPosition != "${holder.bindingAdapterPosition}$reasonPosition") {
                        selectedPosition = "${holder.bindingAdapterPosition}$reasonPosition"
                        notifyDataSetChanged()

                        onDetailsClickListener.onDetailsClicked()
                    }
                }
            }).apply {
                reasonList = detailsList[it]!!
                if (this@ReportDetailsAdapter.selectedPosition != "-1") {
                    if (this@ReportDetailsAdapter.selectedPosition[0].digitToInt() == holder.bindingAdapterPosition) {
                        setSelection(this@ReportDetailsAdapter.selectedPosition[1].digitToInt())
                    }
                }
            }
            holder.binding.rvReasons.apply {
                layoutManager = LinearLayoutManager(
                    holder.binding.root.context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                this.adapter = adapter
            }
        }
    }

    override fun getItemCount(): Int {
        return detailsList.size
    }

    class ViewHolder(val binding: ItemReportReasonDetailsBinding) : RecyclerView.ViewHolder(binding.root)
}