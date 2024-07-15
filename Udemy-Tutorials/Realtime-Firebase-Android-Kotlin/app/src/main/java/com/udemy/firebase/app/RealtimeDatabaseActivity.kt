package com.udemy.firebase.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.udemy.firebase.app.databinding.ActivityRealtimeDatabaseBinding

class RealtimeDatabaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRealtimeDatabaseBinding

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRealtimeDatabaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance().reference
        /*database.child("gold price").setValue("75300 Rupees")

        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue<String>()
                binding.txtGoldPrice.text = post
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        database.child("gold price").addValueEventListener(valueListener)*/

        val user = User("Nilesh", "nils@gmail.com")
        database.child("kotlin-users").setValue(user)

        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<User>()
                binding.txtGoldPrice.text = "${user?.userName}: ${user?.email}"
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        database.child("kotlin-users").addValueEventListener(userListener)
    }
}