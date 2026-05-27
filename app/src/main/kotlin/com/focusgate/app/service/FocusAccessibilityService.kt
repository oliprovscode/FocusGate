package com.focusgate.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.focusgate.app.data.BlockedAppsManager
import com.focusgate.app.data.TaskRepository
import com.focusgate.app.ui.BlockedActivity
import kotlinx.coroutines.*

/**
 * Monitors foreground app changes.
 * When a blocked app is opened and today's task isn't complete,
 * it launches BlockedActivity over it.
 *
 * The blocked app list is managed by BlockedAppsManager and persisted
 * in SharedPreferences — no hardcoded package names here.
 */
class FocusAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repo: TaskRepository

    override fun onServiceConnected() {
        super.onServiceConnected()
        repo = TaskRepository(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        // Skip our own package to avoid blocking FocusGate itself
        if (pkg == applicationContext.packageName) return

        if (BlockedAppsManager.isBlocked(this, pkg)) {
            scope.launch {
                val done = repo.isCompletedToday()
                if (!done) {
                    val intent = Intent(this@FocusAccessibilityService, BlockedActivity::class.java).apply {
                        // Pass the blocked package name so the UI can show which app was blocked
                        putExtra(BlockedActivity.EXTRA_BLOCKED_PACKAGE, pkg)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
