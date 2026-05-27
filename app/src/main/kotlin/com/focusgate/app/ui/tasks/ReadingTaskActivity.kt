package com.focusgate.app.ui.tasks

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.focusgate.app.data.TaskRepository
import com.focusgate.app.data.TaskType
import com.focusgate.app.databinding.ActivityReadingTaskBinding
import kotlinx.coroutines.launch

/**
 * 5-minute reading task.
 * Uses proximity sensor — timer pauses if the phone is brought near the face (picked up).
 */
class ReadingTaskActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityReadingTaskBinding
    private lateinit var repo: TaskRepository
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null

    private var remainingMs = 5 * 60 * 1000L
    private var timer: CountDownTimer? = null
    private var isPaused = false
    private var phonePickedUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = TaskRepository(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        binding.btnCancelReading.setOnClickListener {
            timer?.cancel()
            finish()
        }

        startTimer()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(remainingMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!phonePickedUp) {
                    remainingMs = millisUntilFinished
                    val minutes = millisUntilFinished / 60000
                    val seconds = (millisUntilFinished % 60000) / 1000
                    binding.tvReadingTimer.text = String.format("%d:%02d", minutes, seconds)
                }
            }
            override fun onFinish() {
                onReadingComplete()
            }
        }.start()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            // Low value = phone is close (face/pocket)
            phonePickedUp = event.values[0] < event.sensor.maximumRange
            if (phonePickedUp) {
                timer?.cancel()
                binding.tvReadingTimer.alpha = 0.4f
            } else {
                binding.tvReadingTimer.alpha = 1.0f
                startTimer()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        proximitySensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        timer?.cancel()
    }

    private fun onReadingComplete() {
        lifecycleScope.launch {
            repo.recordCompletion(TaskType.READING)
            kotlinx.coroutines.delay(1000)
            setResult(RESULT_OK)
            finish()
        }
    }
}
