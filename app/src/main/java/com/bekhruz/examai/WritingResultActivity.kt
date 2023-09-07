package com.bekhruz.examai

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



    }
}