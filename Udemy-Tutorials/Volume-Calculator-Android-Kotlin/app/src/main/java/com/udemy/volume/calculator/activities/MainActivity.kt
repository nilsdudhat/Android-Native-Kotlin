package com.udemy.volume.calculator.activities

import android.content.Intent
import android.os.Bundle
import android.widget.GridView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.volume.calculator.R
import com.udemy.volume.calculator.adapters.ShapeAdapter
import com.udemy.volume.calculator.models.Shape

class MainActivity : AppCompatActivity() {

    private lateinit var gridView: GridView

    private lateinit var shapeArrayList: ArrayList<Shape>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gridView = findViewById(R.id.grid_view)

        getShapes()

        setUpGridView()
    }

    private fun setUpGridView() {
        val shapeAdapter = ShapeAdapter(this@MainActivity, shapeArrayList)
        gridView.adapter = shapeAdapter

        gridView.setOnItemClickListener { _, _, position: Int, _ ->
            run {
                when (position) {
                    0 -> {
                        val intent = Intent(this@MainActivity, SphereActivity::class.java)
                        startActivity(intent)
                    }

                    1 -> {
                        val intent = Intent(this@MainActivity, CylinderActivity::class.java)
                        startActivity(intent)
                    }

                    2 -> {
                        val intent = Intent(this@MainActivity, PrismActivity::class.java)
                        startActivity(intent)
                    }

                    3 -> {
                        val intent = Intent(this@MainActivity, CubeActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun getShapes() {
        shapeArrayList = ArrayList()

        val sphere = Shape(R.drawable.sphere, "Sphere")
        val cylinder = Shape(R.drawable.cylinder, "Cylinder")
        val prism = Shape(R.drawable.prism, "Prism")
        val cube = Shape(R.drawable.cube, "Cube")

        shapeArrayList.add(sphere)
        shapeArrayList.add(cylinder)
        shapeArrayList.add(prism)
        shapeArrayList.add(cube)
    }
}