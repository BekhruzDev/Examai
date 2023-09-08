package com.bekhruz.examai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bekhruz.examai.databinding.ActivityWritingResultBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WritingResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWritingResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritingResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val receivedMessage = intent.getStringExtra("result")

        if (receivedMessage != null) {
            binding.tvResult.text = receivedMessage
        }
        binding.btnToSpeaking.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}