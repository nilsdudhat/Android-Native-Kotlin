package com.demo.architecture.mvc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.demo.architecture.mvc.databinding.ActivityMainBinding
import java.util.Observable
import java.util.Observer

class MainActivity : AppCompatActivity(), Observer {

    // creating object of Model class
    private var model: Model? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // creating relationship between the
        // observable Model and the
        // observer Activity
        model = Model()
        model!!.addObserver(this)

        // calling setValueAtIndex() method
        // by passing appropriate arguments
        binding.button.setOnClickListener {
            model!!.setValueAtIndex(0)
        }
        binding.button2.setOnClickListener {
            model!!.setValueAtIndex(1)
        }
        binding.button3.setOnClickListener {
            model!!.setValueAtIndex(2)
        }
    }

    // function to update the view after
    // the values are modified by the model
    override fun update(p0: Observable?, p1: Any?) {

        // changing text of the buttons
        // according to updated values
        binding.button.text = StringBuilder().append("Count :").append(model!!.getValueAtIndex(0))
        binding.button2.text = StringBuilder().append("Count :").append(model!!.getValueAtIndex(1))
        binding.button3.text = StringBuilder().append("Count :").append(model!!.getValueAtIndex(2))
    }
}