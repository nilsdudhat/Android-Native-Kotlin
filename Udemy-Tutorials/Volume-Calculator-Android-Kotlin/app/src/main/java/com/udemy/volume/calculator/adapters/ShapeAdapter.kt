package com.udemy.volume.calculator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.udemy.volume.calculator.R
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
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false)

            val myVieHolder = MyViewHolder()

            myVieHolder.imgShape = view.findViewById(R.id.img_shape)
            myVieHolder.txtShapeName = view.findViewById(R.id.txt_shape_name)

            myVieHolder.imgShape.setImageResource(shape.imgShape)
            myVieHolder.txtShapeName.text = shape.shapeName

            return view
        }

        return super.getView(position, convertView, parent)
    }

    private class MyViewHolder {
        lateinit var txtShapeName: TextView
        lateinit var imgShape: ImageView
    }
}