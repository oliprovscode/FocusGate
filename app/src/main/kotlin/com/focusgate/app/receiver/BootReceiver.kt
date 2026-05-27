package com.focusgate.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives BOOT_COMPLETED to ensure the app is ready
 * to intercept Instagram after device restart.
 * WorkManager handles rescheduling automatically.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // AccessibilityService restarts automatically.
            // Nothing to schedule — daily reset happens via date check in TaskRepository.
        }
    }
}
