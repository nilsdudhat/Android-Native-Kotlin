package com.udemy.menu.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.menu.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.include.toolbar.setNavigationOnClickListener {
            Toast.makeText(
                this@MainActivity,
                "Menu Item Clicked",
                Toast.LENGTH_SHORT
            ).show()
        }
        setSupportActionBar(binding.include.toolbar)

        // making home button visible as menu button
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // handling clicks on toolbar buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle action bar item clicks here.
        // the action bar will automatically handle clicks on the Home Up/Down button,
        // so as long as specify a parent activity in AndroidManifest.xml

        val id = item.itemId

        if (id == android.R.id.home) {
            Toast.makeText(this@MainActivity, "Menu Clicked", Toast.LENGTH_SHORT).show()
        }
        if (id == R.id.action_notification) {
            Toast.makeText(this@MainActivity, "Notification Clicked", Toast.LENGTH_SHORT).show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}