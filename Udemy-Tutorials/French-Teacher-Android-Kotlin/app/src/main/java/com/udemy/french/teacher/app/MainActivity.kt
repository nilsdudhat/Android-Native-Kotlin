package com.udemy.french.teacher.app

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnBlack: Button = findViewById(R.id.btnBlack)
        val btnGreen: Button = findViewById(R.id.btnGreen)
        val btnRed: Button = findViewById(R.id.btnRed)
        val btnPurple: Button = findViewById(R.id.btnPurple)
        val btnYellow: Button = findViewById(R.id.btnYellow)

        btnBlack.setOnClickListener(this@MainActivity)
        btnGreen.setOnClickListener(this@MainActivity)
        btnRed.setOnClickListener(this@MainActivity)
        btnPurple.setOnClickListener(this@MainActivity)
        btnYellow.setOnClickListener(this@MainActivity)
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