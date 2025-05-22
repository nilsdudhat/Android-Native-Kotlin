package com.vasundhara.pactical

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GridAdapter
    private var gridSize = 0 // N x N

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etInput = findViewById<EditText>(R.id.etInput)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        btnGenerate.setOnClickListener {
            val input = etInput.text.toString().toIntOrNull()
            if (input != null && input in 4..10) {
                gridSize = input
                adapter = GridAdapter(gridSize)
                recyclerView.layoutManager = GridLayoutManager(this, gridSize)
                recyclerView.adapter = adapter
            } else {
                Toast.makeText(this, "Please enter number between 4 and 10", Toast.LENGTH_SHORT).show()
            }
        }
    }
}