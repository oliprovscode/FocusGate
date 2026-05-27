package com.focusgate.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the set of app package names that FocusGate will block
 * until the daily task is complete.
 *
 * Backed by SharedPreferences (no DB needed — just a string set).
 */
object BlockedAppsManager {

    private const val PREFS_NAME = "focusgate_blocked_apps"
    private const val KEY_PACKAGES = "blocked_packages"

    // Default blocked apps shipped with the app
    private val DEFAULTS = setOf("com.instagram.android")

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBlockedPackages(context: Context): Set<String> {
        val stored = prefs(context).getStringSet(KEY_PACKAGES, null)
        return stored ?: DEFAULTS
    }

    fun setBlockedPackages(context: Context, packages: Set<String>) {
        prefs(context).edit()
            .putStringSet(KEY_PACKAGES, packages)
            .apply()
    }

    fun addPackage(context: Context, pkg: String) {
        val current = getBlockedPackages(context).toMutableSet()
        current.add(pkg)
        setBlockedPackages(context, current)
    }

    fun removePackage(context: Context, pkg: String) {
        val current = getBlockedPackages(context).toMutableSet()
        current.remove(pkg)
        setBlockedPackages(context, current)
    }

    fun isBlocked(context: Context, pkg: String): Boolean =
        getBlockedPackages(context).contains(pkg)
}
