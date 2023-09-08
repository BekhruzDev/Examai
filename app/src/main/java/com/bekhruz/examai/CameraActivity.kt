package com.bekhruz.examai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/*
@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
    private lateinit var binding:ActivityCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

}*/
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bekhruz.examai.databinding.ActivityCameraBinding
import com.bekhruz.examai.ml.GraphicOverlay
import com.bekhruz.examai.ml.PreferenceUtils
import com.bekhruz.examai.ml.VisionImageProcessor
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.demo.kotlin.textdetector.TextRecognitionProcessor
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var imageCapture: ImageCapture
    private val cameraPermissionCode = 101
    private var previewView: PreviewView? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var selectedModel = TEXT_RECOGNITION_LATIN
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null
    private val viewModel by viewModels<WritingViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        imageCapture = ImageCapture.Builder().build()
        previewView = binding.previewView
        graphicOverlay = binding.graphicOverlay
        // Request camera permission
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionCode
            )
        }

        binding.capture.setOnClickListener { takePicture() }
    }


    public override fun onResume() {
        super.onResume()
        bindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()

        imageProcessor?.run { this.stop() }
    }

    public override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run { this.stop() }
    }


    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CameraXViewModel::class.java]
            .processCameraProvider
            .observe(this) { provider: ProcessCameraProvider? ->
                cameraProvider = provider
                bindAllCameraUseCases()
            }
    }

    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider!!.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return
        }
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        previewUseCase = builder.build()
        previewUseCase!!.setSurfaceProvider(previewView!!.getSurfaceProvider())
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector!!,
            previewUseCase
        )
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }
        imageProcessor =
            try {
                when (selectedModel) {

                    TEXT_RECOGNITION_LATIN -> {
                        Log.i(TAG, "Using on-device Text recognition Processor for Latin")
                        TextRecognitionProcessor(this, TextRecognizerOptions.Builder().build())
                    }
                    else -> throw IllegalStateException("Invalid model name")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Can not create image processor: $selectedModel", e)
                Toast.makeText(
                    applicationContext,
                    "Can not create image processor: " + e.localizedMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
                return
            }

        val builder = ImageAnalysis.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay!!.setImageSourceInfo(
                            imageProxy.width,
                            imageProxy.height,
                            isImageFlipped
                        )
                    } else {
                        graphicOverlay!!.setImageSourceInfo(
                            imageProxy.height,
                            imageProxy.width,
                            isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    imageProcessor!!.processImageProxy(imageProxy, graphicOverlay)
                } catch (e: MlKitException) {
                    Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
                    Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
        cameraProvider?.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector!!,
            analysisUseCase
        )
    }


    private fun takePicture() {

    /*    val imageCapture = imageCapture ?: return

        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            SimpleDateFormat(
                "yyyyMMddHHmmss",
                Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Image saved successfully
                    val image = InputImage.fromFilePath(this@CameraActivity, photoFile.toUri())
                    val options = TextRecognizerOptions.Builder().build()

                    val recognizer = TextRecognition.getClient(options)

                    recognizer.process(image)
                        .addOnSuccessListener { texts ->
                            // Handle text recognition results
                            processTextRecognitionResult(texts)
                        }
                        .addOnFailureListener { e ->
                            // Handle text recognition error
                            e.printStackTrace()
                        }
                }

                override fun onError(exception: ImageCaptureException) {
                    // Handle image capture error
                }
            }
        )*/
        lifecycleScope.launch {
            cameraProvider?.unbindAll()
            imageProcessor?.run { this.stop() }
            viewModel.isLoading.postValue(true)
            viewModel.askQuestion(prefix + topic + "Essay: " + getResponse())

          /* val intent = Intent(this@CameraActivity, WritingResultActivity::class.java)
            intent.putExtra("result", getResponse())
            startActivity(intent)
            finish()*/
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


    private fun processTextRecognitionResult(texts: Text) {
        Log.d("CAMERA", "processTextRecognitionResult: $texts")
        // Process recognized text here
        // texts.textBlocks contains a list of text blocks
        // texts.textBlocks[0].text contains the recognized text from the first text block
    }
    private fun getResponse(): String {
        return getString(R.string.essay)
    }


    private fun View.manageVisibility(visible: Boolean) {
        if (visible) this.visibility = View.VISIBLE
        else this.visibility = View.GONE
    }
    companion object {
        private const val TEXT_RECOGNITION_LATIN = "Text Recognition Latin"
        private const val TAG = "CameraActivity"
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

    }
}
