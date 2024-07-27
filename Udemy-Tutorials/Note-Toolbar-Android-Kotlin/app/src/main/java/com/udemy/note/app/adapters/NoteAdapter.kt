package com.udemy.note.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.udemy.note.app.clicks.NoteItemClickListener
import com.udemy.note.app.database.Note
import com.udemy.note.app.databinding.ItemNoteBinding
import java.util.Random

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {
    private val random = Random()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = differ.currentList[position]

        val randomColor = Color.argb(255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256))

        holder.binding.apply {
            holder.binding.color = randomColor
            holder.binding.note = note
            holder.binding.click = NoteItemClickListener()
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setNoteList(noteList: ArrayList<Note>) {
        this.differ.submitList(noteList)
        notifyDataSetChanged()
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return (oldItem.id == newItem.id) &&
                    (oldItem.title == newItem.title) &&
                    (oldItem.body == newItem.body)
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    class ViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)
}