package com.focusgate.app.ui.tasks

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.focusgate.app.data.TaskRepository
import com.focusgate.app.data.TaskType
import com.focusgate.app.databinding.ActivityStepsTaskBinding
import kotlinx.coroutines.launch

class StepsTaskActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityStepsTaskBinding
    private lateinit var repo: TaskRepository
    private lateinit var sensorManager: SensorManager

    private var stepSensor: Sensor? = null
    private var initialSteps = -1
    private var stepsTaken = 0
    private val TARGET_STEPS = 250

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = TaskRepository(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        binding.btnCancelSteps.setOnClickListener { finish() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 101
            )
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            if (initialSteps == -1) { initialSteps = totalSteps }
            stepsTaken = totalSteps - initialSteps
            binding.tvStepsCount.text = "$stepsTaken / $TARGET_STEPS"
            if (stepsTaken >= TARGET_STEPS) onStepsComplete()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun onStepsComplete() {
        sensorManager.unregisterListener(this)
        lifecycleScope.launch {
            repo.recordCompletion(TaskType.STEPS)
            kotlinx.coroutines.delay(1000)
            setResult(RESULT_OK)
            finish()
        }
    }
}
