package com.bekhruz.examai

import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bekhruz.examai.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaRecorder: MediaRecorder? = null
    private var filePath: String = ""
    private var permissionGranted = false
    private val viewModel: ExamaiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAndRequestPermissions()
        binding.btnStart.setOnClickListener {
            startRecording()
            binding.flScreenIntro.manageVisibility(false)
            binding.flScreenQ1.manageVisibility(true)
            binding.flScreenQ2.manageVisibility(false)
            binding.flScreenQ3.manageVisibility(false)
            binding.flResult.manageVisibility(false)
        }
        binding.btnNextQ1.setOnClickListener {
            lifecycleScope.launch {
                stopRecording()
                sendForReview(
                    prompt = "Let’s talk about your hometown. Where is your hometown?",
                    questionNo = 1
                )
                delay(2000)
                viewModel.isLoading.postValue(false)
                startRecording()
                binding.flScreenIntro.manageVisibility(false)
                binding.flScreenQ1.manageVisibility(false)
                binding.flScreenQ2.manageVisibility(true)
                binding.flScreenQ3.manageVisibility(false)
                binding.flResult.manageVisibility(false)
            }

        }
        binding.btnNextQ2.setOnClickListener {
            lifecycleScope.launch {
                stopRecording()
                sendForReview(
                    prompt = "Tell me what you like about it",
                    questionNo = 2
                )
                delay(2000)
                viewModel.isLoading.postValue(false)
                startRecording()
                binding.flScreenIntro.manageVisibility(false)
                binding.flScreenQ1.manageVisibility(false)
                binding.flScreenQ2.manageVisibility(false)
                binding.flScreenQ3.manageVisibility(true)
                binding.flResult.manageVisibility(false)
            }

        }
        binding.btnFinish.setOnClickListener {
            lifecycleScope.launch {
                stopRecording()
                sendForReview(
                    prompt = "Can you also tell about what you don't like about it?",
                    questionNo = 3
                )
                delay(3000)
                binding.flScreenIntro.manageVisibility(false)
                binding.flScreenQ1.manageVisibility(false)
                binding.flScreenQ2.manageVisibility(false)
                binding.flScreenQ3.manageVisibility(false)
                binding.flResult.manageVisibility(true)
            }
        }
        binding.btnExit.setOnClickListener {
            finish()
        }

        viewModel.speechResult.observe(this) {
            binding.tvFluency.text = getString(R.string.fluency, it.fluency.toActualResult())
            binding.tvGrammar.text = getString(R.string.grammar, it.grammar.toActualResult())
            binding.tvPronunciation.text = getString(R.string.pronunciation, it.pronunciation.toActualResult())
            binding.tvRelevance.text = getString(R.string.relevance, it.relevance.toActualResult())
            binding.tvSpeed.text = getString(R.string.speakingSpeed, it.speed.toActualResult())
            binding.tvLexicalRes.text = getString(R.string.lexicalResource, it.vocabulary.toActualResult())
            binding.tvOverall.text = getString(R.string.overAll, it.overAll.toOverAllBand())
            viewModel.isLoading.postValue(false)
        }
        viewModel.isLoading.observe(this) {
            binding.progress.manageVisibility(it)
        }
    }

    private fun sendForReview(prompt: String, questionNo: Int = 1) {
        val audioPath = "${externalCacheDir?.absolutePath}/audio.amr"
        val audioType = "amr"
        val audioSampleRate = "16000"
        val refText = "audio"
        val coreType = "speak.eval.pro"
        val testType = "ielts"
        val partNumber = 1

        // Call the httpAPI function
        viewModel.httpAPI(
            audioPath,
            audioType,
            audioSampleRate,
            refText,
            coreType,
            testType,
            partNumber,
            prompt,
            questionNo
        )
    }

    private fun onRecord(recording: Boolean) {
        if (recording) {
            startRecording()
        } else stopRecording()
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            filePath = "${externalCacheDir?.absolutePath}/audio.amr"
            setOutputFile(filePath)
            prepare()
            start()
        }
    }

    private fun checkAndRequestPermissions() {
        val readPermission = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
        val writePermission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
        val recordPermission = ContextCompat.checkSelfPermission(this, RECORD_AUDIO)

        val listPermissionsNeeded = mutableListOf<String>()

        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_EXTERNAL_STORAGE)
        }

        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE)
        }

        if (recordPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(RECORD_AUDIO)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[RECORD_AUDIO] = PackageManager.PERMISSION_GRANTED
                // Fill with the actual results from user
                for (i in permissions.indices) {
                    perms[permissions[i]] = grantResults[i]
                }
                // Check for both permissions
                if (perms[READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    && perms[WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    && perms[RECORD_AUDIO] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Do your file operations here
                    onPermissionsGranted()
                } else {
                    // Permission was denied; show a message to inform the user
                    Toast.makeText(this, "Permissions denied!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun onPermissionsGranted() {
        permissionGranted = true
    }

    private fun View.manageVisibility(visible: Boolean) {
        if (visible) this.visibility = View.VISIBLE
        else this.visibility = View.GONE
    }

    private fun Int.toOverAllBand():Int{
        return (this * 9) / 100
    }

    private fun Int.toActualResult():Int{
        return if(this>=20)this - 20 else this
    }

    companion object {
        private const val TAG = "SPEECH_SUPER"
        private const val PERMISSION_REQUEST_CODE = 1001
    }

}