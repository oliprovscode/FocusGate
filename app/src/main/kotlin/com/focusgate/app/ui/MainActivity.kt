package com.focusgate.app.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.focusgate.app.data.TaskRepository
import com.focusgate.app.databinding.ActivityMainBinding
import com.focusgate.app.service.FocusAccessibilityService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repo: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = TaskRepository(this)

        // Task history
        lifecycleScope.launch {
            repo.recentHistory.collect { records ->
                val adapter = HistoryAdapter(records)
                binding.rvHistory.adapter = adapter
                binding.rvHistory.layoutManager =
                    androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
            }
        }

        // Unlock button
        binding.btnUnlock.setOnClickListener {
            startActivity(Intent(this, TaskPickerActivity::class.java))
        }

        // Manage blocked apps
        binding.btnManageApps.setOnClickListener {
            startActivity(Intent(this, BlockedAppsActivity::class.java))
        }

        // Accessibility enable button
        binding.btnEnableAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        refreshUI()
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        lifecycleScope.launch {
            val doneToday = repo.isCompletedToday()
            val streak = repo.getCurrentStreak()

            binding.tvStreakCount.text = streak.toString()
            binding.tvTodayStatus.text = if (doneToday) "Done" else "Not done"

            if (doneToday) {
                binding.btnUnlock.visibility = android.view.View.GONE
                binding.layoutTodayDone.visibility = android.view.View.VISIBLE
            } else {
                binding.btnUnlock.visibility = android.view.View.VISIBLE
                binding.layoutTodayDone.visibility = android.view.View.GONE
            }

            val isServiceEnabled = isAccessibilityServiceEnabled()
            binding.layoutAccessibilityWarning.visibility =
                if (isServiceEnabled) android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(packageName + "/" + FocusAccessibilityService::class.java.name)
    }
}
