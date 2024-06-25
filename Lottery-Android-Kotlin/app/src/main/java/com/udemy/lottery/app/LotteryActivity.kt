package com.udemy.lottery.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.lottery.app.databinding.ActivityLotteryBinding

class LotteryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLotteryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLotteryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val username = intent.getStringExtra("username")

        val lotteryNumber = generateLotteryNumber()
        binding.txtLotteryNumber.text = StringBuilder()
            .append(username)
            .append(", your 12 digit lottery number is")
            .append("\n")
            .append(lotteryNumber)

        binding.btnShare.setOnClickListener {
            shareLotteryNumber(username, lotteryNumber)
        }
    }

    private fun shareLotteryNumber(username: String?, lotteryNumber: Long) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Lottery Number")
        intent.putExtra(Intent.EXTRA_TEXT, "$username has assigned lottery number: $lotteryNumber")
        startActivity(Intent.createChooser(intent, "Share lottery number"))
    }

    private fun generateLotteryNumber(): Long {
        val lotteryNumbers = mutableListOf<Int>()

        var firstNumber = getRandomNumber()

        while (firstNumber == 0) {
            firstNumber = getRandomNumber()
        }

        lotteryNumbers.add(firstNumber)

        for (i in 0 until 11) {
            val number = getRandomNumber()
            lotteryNumbers.add(number)
        }

        val lotteryNumber = lotteryNumbers.joinToString("").toLong()

        Log.d("--lottery--", "generateLotteryNumber: $lotteryNumber")

        return lotteryNumber
    }

    private fun getRandomNumber(): Int {
        return (0..9).random()
    }
}