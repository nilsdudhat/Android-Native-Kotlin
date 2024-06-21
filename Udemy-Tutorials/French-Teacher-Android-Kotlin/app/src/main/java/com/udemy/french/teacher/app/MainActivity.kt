package com.udemy.french.teacher.app

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.udemy.french.teacher.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBlack.setOnClickListener(this@MainActivity)
        binding.btnGreen.setOnClickListener(this@MainActivity)
        binding.btnRed.setOnClickListener(this@MainActivity)
        binding.btnPurple.setOnClickListener(this@MainActivity)
        binding.btnYellow.setOnClickListener(this@MainActivity)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBlack -> {
                play(R.raw.black)
            }
            R.id.btnGreen -> {
                play(R.raw.green)
            }
            R.id.btnRed -> {
                play(R.raw.red)
            }
            R.id.btnPurple -> {
                play(R.raw.purple)
            }
            R.id.btnYellow -> {
                play(R.raw.yellow)
            }
        }
    }

    private fun play(rawId: Int) {
        val mediaPlayer = MediaPlayer.create(this@MainActivity, rawId)
        mediaPlayer.start()
    }
}