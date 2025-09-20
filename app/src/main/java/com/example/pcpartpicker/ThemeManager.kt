package com.example.pcpartpicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.edit

/**
 * Manages theme switching.
 * - Saves selected theme index.
 * - Applies theme before activity creation.
 * - Allows forcing activity recreation after a theme change to ensure effective swapping.
 */
object ThemeManager {
    private const val PREF_THEME_KEY = "selected_app_theme"

    // Theme ID's
    private const val THEME_PCPARTPICKER = 0
    private const val THEME_DEFAULT = 1

    // Maps theme index -> style resource
    private val appThemes = mapOf(
        THEME_PCPARTPICKER to R.style.Theme_PCPartPicker,
        THEME_DEFAULT to R.style.Theme_Default
    )

    // Apply saved theme to activity before it’s created
    fun applyTheme(activity: Activity) {
        val savedThemeIndex = getSavedThemeIndex(activity)
        val themeResId = appThemes[savedThemeIndex] ?: R.style.Theme_PCPartPicker
        activity.setTheme(themeResId)
        Log.d("ThemeManager", "Applied theme: ${activity.resources.getResourceEntryName(themeResId)}")
    }

    // Retrieve saved theme index
    fun getSavedThemeIndex(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_THEME_KEY, THEME_PCPARTPICKER)
    }

    // Recreate the activity to apply any theme changes.
    fun recreateActivity(activity: Activity) {
        activity.recreate()
    }

    // Save chosen theme index
    fun saveThemeIndex(context: Context, themeIndex: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putInt(PREF_THEME_KEY, themeIndex)
            }
        Log.d("ThemeManager", "Saved theme index: $themeIndex")
    }


}