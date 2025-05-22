package com.vasundhara.pactical

import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GridAdapter(
    private val gridSize: Int,
) : RecyclerView.Adapter<GridAdapter.CellViewHolder>() {

    private val totalSize = gridSize * gridSize
    private val blinkingPositions = mutableSetOf<Int>()

    inner class CellViewHolder(val view: FrameLayout) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cell, parent, false) as FrameLayout

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val cellSize =
            (screenWidth - parent.context.resources.getDimensionPixelOffset(R.dimen.margin) * 2) / gridSize
        view.layoutParams = ViewGroup.LayoutParams(cellSize, cellSize)

        return CellViewHolder(view)
    }

    override fun getItemCount(): Int = totalSize

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val isBlinking = blinkingPositions.contains(position)
        holder.view.setBackgroundResource(if (isBlinking) R.drawable.red else R.drawable.blank)

        holder.view.findViewById<TextView>(R.id.txt_position).text = position.toString()

        holder.view.setOnClickListener {
            val affected = getAffectedPositions(position)
            blinkingPositions.clear()
            blinkingPositions.addAll(affected)
            notifyDataSetChanged()

            Handler(Looper.getMainLooper()).postDelayed({
                blinkingPositions.clear()
                notifyDataSetChanged()
            }, 1000)
        }
    }

    private fun getAffectedPositions(position: Int): Set<Int> {
        val result = mutableSetOf<Int>()

        val selectedRow = position / gridSize
        val selectedCol = position % gridSize

        for (i in 0 until totalSize) {
            val row = i / gridSize
            val col = i % gridSize

            if (row == selectedRow || // same row
                col == selectedCol || // same column
                row - col == selectedRow - selectedCol || // main diagonal
                row + col == selectedRow + selectedCol    // anti diagonal
            ) {
                result.add(i)
            }
        }

        return result
    }
}