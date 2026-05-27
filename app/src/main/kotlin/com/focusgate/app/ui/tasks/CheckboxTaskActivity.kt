package com.focusgate.app.ui.tasks

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.focusgate.app.R
import com.focusgate.app.data.TaskRepository
import com.focusgate.app.data.TaskType
import com.focusgate.app.databinding.ActivityCheckboxTaskBinding
import kotlinx.coroutines.launch

/**
 * Handles tasks that only need a time-delayed confirmation:
 * SHOWER (time-restricted 08-10), WATER, BREATHING.
 */
class CheckboxTaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_TYPE = "extra_task_type"
        // Delay in ms before confirm button is enabled
        private const val CONFIRM_DELAY_MS = 30_000L
    }

    private lateinit var binding: ActivityCheckboxTaskBinding
    private lateinit var repo: TaskRepository
    private lateinit var taskType: TaskType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckboxTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = TaskRepository(this)
        taskType = TaskType.valueOf(
            intent.getStringExtra(EXTRA_TASK_TYPE) ?: TaskType.WATER.name
        )

        binding.tvCheckboxTitle.setText(taskType.titleRes)
        binding.tvCheckboxDesc.setText(taskType.descRes)
        binding.ivCheckboxIcon.setImageResource(taskType.iconRes)

        binding.btnCancelCheckbox.setOnClickListener { finish() }

        // Countdown to enable the confirm button
        object : CountDownTimer(CONFIRM_DELAY_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secs = millisUntilFinished / 1000
                binding.tvCountdown.text = "Confirm available in ${secs}s"
            }
            override fun onFinish() {
                binding.tvCountdown.text = "Ready to confirm"
                binding.btnConfirm.isEnabled = true
                binding.btnConfirm.alpha = 1f
            }
        }.start()

        binding.btnConfirm.setOnClickListener {
            lifecycleScope.launch {
                repo.recordCompletion(taskType)
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
