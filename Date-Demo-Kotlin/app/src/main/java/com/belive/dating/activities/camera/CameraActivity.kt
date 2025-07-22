package com.belive.dating.activities.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.activities.MixPanelActivity
import com.belive.dating.databinding.ActivityCameraBinding
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.setSystemBarColors
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.io.File

class CameraActivity : MixPanelActivity() {

    private var cameraController: LifecycleCameraController? = null
    private var isCapturing = false
    private var isCheckingLiveness = false
    private var hasBlinked = false
    private var previousLeftEyeOpen = false
    private var previousRightEyeOpen = false

    var file: File? = null

    val binding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(CameraActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(CameraActivity::class.java.simpleName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        ViewCompat.setOnApplyWindowInsetsListener(binding.rvGuidelines) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (v.layoutParams as ConstraintLayout.LayoutParams).bottomMargin =
                systemBars.bottom + getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.txtVerificationMessage) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (v.layoutParams as ConstraintLayout.LayoutParams).topMargin =
                systemBars.top + getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
            insets
        }

        initViews()

        file = if (intent.hasExtra("path")) {
            File(intent.getStringExtra("path")!!)
        } else {
            null
        }
    }

    var timer: CountDownTimer? = null

    private fun initViews() {
        showGuidelines()

        binding.isCountDown = true
        binding.countDown = 5

        binding.ovalLayout.showGuides = false

        binding.ovalLayout.post {
            timer = object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.isCountDown = true
                    binding.countDown = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    startCamera(true)

                    binding.isCountDown = false
                    binding.countDown = 0

                    binding.ovalLayout.showGuides = true

                    timer?.cancel()
                    timer = null
                }
            }
            timer?.start()
        }

        if (ContextCompat.checkSelfPermission(this@CameraActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(false)
        } else {
            ActivityCompat.requestPermissions(this@CameraActivity, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    override fun onStop() {
        timer?.cancel()
        timer = null

        cameraController?.unbind()

        super.onStop()
    }

    private fun showGuidelines() {
        val guideLineList = arrayListOf<String>()
        guideLineList.add("Once the countdown ends, face scanning will begin for verification.")
        guideLineList.add("Align your face within the oval guide on the screen.")
        guideLineList.add("Ensure your full face is visible.")

        val guideLineAdapter = SelfieGuidelineAdapter()
        guideLineAdapter.guidelineList = guideLineList

        binding.rvGuidelines.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvGuidelines.adapter = guideLineAdapter
    }

    private fun startCamera(startAnalysis: Boolean) {
        cameraController?.unbind()

        cameraController = LifecycleCameraController(this)
        cameraController?.bindToLifecycle(this)
        cameraController?.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        binding.previewView.controller = cameraController

        if (startAnalysis) {
            val faceDetectorOptions = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            val faceDetector = FaceDetection.getClient(faceDetectorOptions)

            val mlKitAnalyzer = MlKitAnalyzer(
                listOf(faceDetector),
                ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this),
            ) { result: MlKitAnalyzer.Result? ->
                val faces = result?.getValue(faceDetector)
                if (faces.isNullOrEmpty()) {
                    isCheckingLiveness = false
                    return@MlKitAnalyzer
                }

                val face = faces[0]
                val leftEyeOpen = face.leftEyeOpenProbability ?: 0f
                val rightEyeOpen = face.rightEyeOpenProbability ?: 0f

                // Initial face check
                if (!isCheckingLiveness && !isCapturing && checkFaceConditions(face)) {
                    isCheckingLiveness = true
                    previousLeftEyeOpen = leftEyeOpen > 0.60f
                    previousRightEyeOpen = rightEyeOpen > 0.60f
                    return@MlKitAnalyzer
                }

                // Check for blink during liveness phase
                if (isCheckingLiveness) {
                    val currentLeftClosed = leftEyeOpen < 0.2f
                    val currentRightClosed = rightEyeOpen < 0.2f

                    // Detect blink (eyes closed after being open)
                    if ((previousLeftEyeOpen && currentLeftClosed) || (previousRightEyeOpen && currentRightClosed)) {
                        hasBlinked = true
                    }

                    // Capture if blinked and eyes open again
                    if (hasBlinked && leftEyeOpen > 0.60f && rightEyeOpen > 0.60f) {
                        takePhoto()
                        isCheckingLiveness = false
                        hasBlinked = false
                    }

                    previousLeftEyeOpen = leftEyeOpen > 0.8f
                    previousRightEyeOpen = rightEyeOpen > 0.8f
                }
            }

            cameraController?.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(this), mlKitAnalyzer)
        }
    }

    private fun checkFaceConditions(face: Face): Boolean {
        val boundingBox = face.boundingBox
        val faceArea = boundingBox.width() * boundingBox.height()
        val imageArea = binding.previewView.width * binding.previewView.height
        val faceCoverage = faceArea.toFloat() / imageArea

        val requiredLandmarks = listOf(
            FaceLandmark.LEFT_EYE,
            FaceLandmark.RIGHT_EYE,
            FaceLandmark.NOSE_BASE,
            FaceLandmark.MOUTH_LEFT,
            FaceLandmark.MOUTH_RIGHT,
        )

        val allLandmarksPresent = requiredLandmarks.all { face.getLandmark(it) != null }

        return ((faceCoverage > 0.2f) &&
                ((face.leftEyeOpenProbability ?: 0f) > 0.8f) &&
                ((face.rightEyeOpenProbability ?: 0f) > 0.8f) &&
                allLandmarksPresent)
    }

    private fun takePhoto() {
        val outputOptions = file?.let { ImageCapture.OutputFileOptions.Builder(it) }?.build()

        outputOptions?.let {
            cameraController?.takePicture(
                it,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        isCapturing = false
                        finish()
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        isCapturing = false
                        setResult(RESULT_OK)
                        finish()
                    }
                })
        }
    }
}