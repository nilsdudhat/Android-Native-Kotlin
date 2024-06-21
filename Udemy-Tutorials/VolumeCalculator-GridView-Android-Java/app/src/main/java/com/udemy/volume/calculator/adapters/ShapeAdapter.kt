package com.udemy.volume.calculator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.udemy.volume.calculator.R
import com.udemy.volume.calculator.databinding.ItemGridBinding
import com.udemy.volume.calculator.models.Shape

/**
 * This class will be responsible for creating the view for each item and binding data to it
 */
class ShapeAdapter(context: Context, objects: ArrayList<Shape>) :
    ArrayAdapter<Shape>(context, R.layout.item_grid, objects) {

    /** getView method:
     * used to create and return a view for a specific item in listing
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // get the object of model for the current position
        val shape = getItem(position)

        if (shape != null) {
            val binding = ItemGridBinding.inflate(LayoutInflater.from(context), parent, false)

            binding.imgShape.setImageResource(shape.imgShape)
            binding.txtShapeName.text = shape.shapeName

            return binding.root
        }

        return super.getView(position, convertView, parent)
    }
}