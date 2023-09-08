package com.bekhruz.examai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.bekhruz.examai.databinding.ActivityWritingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WritingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWritingBinding
    private val viewModel by viewModels<WritingViewModel>()
    val prefix =
        "I have written an essay for ielts writing task 2. Please give a band score for each essay component according to IELTS evaluation criteria with a more detailed explanation. " +
                "Band score number should come inside {} curly brackets e.g. {7} or {8.5}. " +
                "Coherence and cohesion, " +
                "Lexical resource, " +
                "Grammatical range, " +
                "Task achievement, " +
                "Don't show overall the band score. " +
                "Give me comments about my essay. Give me suggestions on how to improve my essay."
    val topic =
        "Topic: Some people think that parents should teach children how to be good members of society. Others, however, believe that school is the place to learn this. " +
                "Discuss both these views and give your own opinion."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnEvaluate.setOnClickListener {
            val text = binding.textInputEditText.text.toString()
            if (text.isNotEmpty()) {
                viewModel.askQuestion(prefix + topic + "Essay: " + text)
            } else {
                Toast.makeText(this, "Please, provide the essay!", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.responseLiveData.observe(this) {
            viewModel.isLoading.postValue(false)
            val intent = Intent(this, WritingResultActivity::class.java)
            intent.putExtra("result", it.answer)
            startActivity(intent)
            finish()
        }

        viewModel.isLoading.observe(this) {
            binding.progress.manageVisibility(it)
        }


    }


    private fun View.manageVisibility(visible: Boolean) {
        if (visible) this.visibility = View.VISIBLE
        else this.visibility = View.GONE
    }

}