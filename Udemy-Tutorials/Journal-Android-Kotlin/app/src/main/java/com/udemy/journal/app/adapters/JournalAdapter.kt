package com.udemy.journal.app.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.udemy.journal.app.databinding.ItemJournalBinding
import com.udemy.journal.app.models.Journal
import com.udemy.journal.app.viewmodels.JournalsViewModel

class JournalAdapter(val viewModel: JournalsViewModel) :
    RecyclerView.Adapter<JournalAdapter.ViewHolder>() {

    private var journalList = ArrayList<Journal>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJournalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val journal = journalList[position]
        holder.binding.journal = journal
        holder.binding.viewModel = viewModel
    }

    override fun getItemCount(): Int {
        return journalList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setJournals(journalList: List<Journal>) {
        this.journalList = ArrayList(journalList)
        notifyDataSetChanged()
    }

    class ViewHolder(itemJournalBinding: ItemJournalBinding) :
        RecyclerView.ViewHolder(itemJournalBinding.root) {
        val binding = itemJournalBinding
    }
}