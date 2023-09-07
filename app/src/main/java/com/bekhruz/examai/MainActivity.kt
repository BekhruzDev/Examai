package com.bekhruz.examai

import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bekhruz.examai.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var filePath: String = ""
    private var permissionGranted = false
    private val viewModel: ExamaiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAndRequestPermissions()

        binding.btnPlay.apply {
            var isPlaying = true
            setOnClickListener {
                text = if (isPlaying) "Stop Playing" else "Start Playing"
                onPlay(isPlaying)
                isPlaying = !isPlaying
            }
        }
        binding.btnRecord.apply {
            var isRecording = true
            setOnClickListener {
                text = if (isRecording) "Stop Recording" else "Start Recording"
                onRecord(isRecording)
                isRecording = !isRecording
            }
        }
        binding.btnCheck.setOnClickListener {
            val audioPath = "${externalCacheDir?.absolutePath}/audio.amr"
            val audioType = "amr"
            val audioSampleRate = "16000"
            val refText = "audio"
            val coreType = "speak.eval.pro"
            val testType = "ielts"
            val partNumber = 1
            val questionPrompt = "Where do you live"

            // Call the httpAPI function
            viewModel.httpAPI(
                audioPath,
                audioType,
                audioSampleRate,
                refText,
                coreType,
                testType,
                partNumber,
                questionPrompt
            )
        }

        viewModel.speechResult.observe(this) {
            Log.d(TAG, "speechSuper: ${it.body()}")
        }
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

    private fun onPlay(playing: Boolean) {
        if (playing) startPlaying()
        else stopPlaying()
    }

    private fun stopPlaying() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startPlaying() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed")
            }
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


    companion object {
        private const val TAG = "SPEECH_SUPER"
        private const val PERMISSION_REQUEST_CODE = 1001
    }

}