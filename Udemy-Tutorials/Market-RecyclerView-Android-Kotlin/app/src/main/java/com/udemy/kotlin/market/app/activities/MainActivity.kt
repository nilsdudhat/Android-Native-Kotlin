package com.udemy.kotlin.market.app.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.udemy.kotlin.market.app.R
import com.udemy.kotlin.market.app.adapters.ItemAdapter
import com.udemy.kotlin.market.app.interfaces.ItemClickListener
import com.udemy.kotlin.market.app.models.Item

class MainActivity : AppCompatActivity(), ItemClickListener {

    private lateinit var rvList: RecyclerView

    private lateinit var itemList: ArrayList<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvList = findViewById(R.id.rv_list)

        getItemList()

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(this@MainActivity)
        rvList.layoutManager = layoutManager

        val itemAdapter = ItemAdapter(itemList, this@MainActivity)
        rvList.adapter = itemAdapter
    }

    private fun getItemList() {
        itemList = ArrayList()

        val fruit = Item(R.drawable.fruit, "Fruits", "Fresh Fruits from the Garden")
        val vegetables = Item(R.drawable.vegitables, "Vegetables", "Delicious Vegetables ")
        val bread = Item(R.drawable.bread, "Bakery", "Bread, Wheat and Beans")
        val beverage = Item(R.drawable.beverage, "Beverage", "Juice, Tea, Coffee and Soda")
        val milk = Item(R.drawable.milk, "Milk", "Milk, Shakes and Yogurt")
        val popcorn = Item(R.drawable.popcorn, "Snacks", "Pop Corn, Donut and Drinks")

        itemList.add(fruit)
        itemList.add(vegetables)
        itemList.add(bread)
        itemList.add(beverage)
        itemList.add(milk)
        itemList.add(popcorn)
    }

    override fun onClick(view: View, position: Int) {
        Toast.makeText(this, "You clicked - " + itemList[position].itemName, Toast.LENGTH_SHORT)
            .show()
    }
}