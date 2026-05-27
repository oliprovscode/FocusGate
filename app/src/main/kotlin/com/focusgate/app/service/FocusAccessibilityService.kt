package com.focusgate.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.focusgate.app.data.TaskRepository
import com.focusgate.app.ui.BlockedActivity
import kotlinx.coroutines.*

/**
 * Monitors foreground app changes.
 * When Instagram is opened and today's task isn't complete,
 * it launches BlockedActivity over it.
 */
class FocusAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repo: TaskRepository

    companion object {
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        repo = TaskRepository(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        if (pkg == INSTAGRAM_PACKAGE) {
            scope.launch {
                val done = repo.isCompletedToday()
                if (!done) {
                    val intent = Intent(this@FocusAccessibilityService, BlockedActivity::class.java).apply {
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
