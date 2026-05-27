package com.focusgate.app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.focusgate.app.databinding.ActivityBlockedBinding

/**
 * Full-screen blocking overlay shown when a blocked app is launched
 * and the daily task hasn't been completed.
 *
 * Receives EXTRA_BLOCKED_PACKAGE to personalise the message.
 */
class BlockedActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }

    private lateinit var binding: ActivityBlockedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Optionally show which app is being blocked
        val blockedPkg = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE)
        if (blockedPkg != null) {
            try {
                val pm = packageManager
                val appLabel = pm.getApplicationLabel(
                    pm.getApplicationInfo(blockedPkg, 0)
                ).toString()
                binding.tvBlockedAppName.text = appLabel
                binding.tvBlockedAppName.visibility = View.VISIBLE
                // Load and show the blocked app's icon
                binding.ivBlockedAppIcon.setImageDrawable(pm.getApplicationIcon(blockedPkg))
                binding.ivBlockedAppIcon.visibility = View.VISIBLE
            } catch (e: PackageManager.NameNotFoundException) {
                // App not found — fall back to generic UI
                binding.ivBlockedAppIcon.visibility = View.GONE
                binding.tvBlockedAppName.visibility = View.GONE
            }
        } else {
            binding.ivBlockedAppIcon.visibility = View.GONE
            binding.tvBlockedAppName.visibility = View.GONE
        }

        binding.btnChooseTask.setOnClickListener {
            startActivity(Intent(this, TaskPickerActivity::class.java))
        }

        binding.btnDismiss.setOnClickListener {
            goHome()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        goHome()
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}
