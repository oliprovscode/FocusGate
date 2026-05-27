package com.focusgate.app.ui.tasks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.focusgate.app.data.TaskRepository
import com.focusgate.app.data.TaskType
import com.focusgate.app.databinding.ActivityPushupTaskBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
class PushupTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPushupTaskBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var repo: TaskRepository

    private var repCount = 0
    private var isDown = false  // Tracks pushup bottom position

    private val poseDetector by lazy {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
        PoseDetection.getClient(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPushupTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = TaskRepository(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnCancelPushup.setOnClickListener { finish() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(cameraExecutor, ::analyzeFrame) }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
                val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)

                if (leftShoulder != null && leftElbow != null &&
                    leftWrist != null && leftHip != null) {

                    val elbowAngle = calculateAngle(
                        leftShoulder.position.y, leftShoulder.position.x,
                        leftElbow.position.y, leftElbow.position.x,
                        leftWrist.position.y, leftWrist.position.x
                    )

                    // Detect pushup rep: down = elbowAngle < 90, up = elbowAngle > 160
                    if (elbowAngle < 90 && !isDown) {
                        isDown = true
                        runOnUiThread { binding.tvPushupStatus.text = "Down" }
                    } else if (elbowAngle > 160 && isDown) {
                        isDown = false
                        repCount++
                        runOnUiThread {
                            binding.tvRepsCount.text = "$repCount / 10"
                            binding.tvPushupStatus.text = "Up"
                            if (repCount >= 10) onPushupsComplete()
                        }
                    }
                }
            }
            .addOnFailureListener { Log.e("PushupTask", "Pose detection failed", it) }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun calculateAngle(
        ay: Float, ax: Float,
        by: Float, bx: Float,
        cy: Float, cx: Float
    ): Double {
        val angle = Math.toDegrees(
            (Math.atan2((cy - by).toDouble(), (cx - bx).toDouble()) -
                    Math.atan2((ay - by).toDouble(), (ax - bx).toDouble()))
        )
        return Math.abs(if (angle < 0) angle + 360 else angle)
    }

    private fun onPushupsComplete() {
        binding.tvPushupStatus.text = "Complete!"
        binding.btnCancelPushup.isEnabled = false
        lifecycleScope.launch {
            repo.recordCompletion(TaskType.PUSHUPS)
            // Brief delay so user sees the completion state
            kotlinx.coroutines.delay(1500)
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        poseDetector.close()
    }
}
