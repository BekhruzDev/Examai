package com.bekhruz.examai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bekhruz.examai.databinding.ActivityCameraBinding
import com.bekhruz.examai.databinding.ActivityWritingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
    private lateinit var binding:ActivityCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}