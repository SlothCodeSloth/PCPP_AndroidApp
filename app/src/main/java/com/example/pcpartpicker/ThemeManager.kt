package com.example.pcpartpicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.edit

object ThemeManager {
    private const val PREF_THEME_KEY = "selected_app_theme"
    private const val THEME_PCPARTPICKER = 0
    private const val THEME_DEFAULT = 1
    private val appThemes = mapOf(
        THEME_PCPARTPICKER to R.style.Theme_PCPartPicker,
        THEME_DEFAULT to R.style.Theme_Default
    )

    fun applyTheme(activity: Activity) {
        val savedThemeIndex = getSavedThemeIndex(activity)
        val themeResId = appThemes[savedThemeIndex] ?: R.style.Theme_PCPartPicker
        activity.setTheme(themeResId)
        Log.d("ThemeManager", "Applied theme: ${activity.resources.getResourceEntryName(themeResId)}")
    }

    fun getSavedThemeIndex(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_THEME_KEY, THEME_PCPARTPICKER)
    }

    fun recreateActivity(activity: Activity) {
        activity.recreate()
    }

    fun saveThemeIndex(context: Context, themeIndex: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putInt(PREF_THEME_KEY, themeIndex)
            }
        Log.d("ThemeManager", "Saved theme index: $themeIndex")
    }


}