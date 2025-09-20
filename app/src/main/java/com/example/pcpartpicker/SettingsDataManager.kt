package com.example.pcpartpicker

import android.content.Context
import android.preference.PreferenceManager

/**
 * Handles saving and retrieving app settings using SharedPreferences.
 * - Stores region, username, custom price toggle, etc.
 * - Provides utility functions for currency/region mapping.
 */
object SettingsDataManager {
    // Preference keys and defaults
    private const val PREF_REGION_KEY = "selected_region"
    private const val PREF_NAME_KEY = "user_name"
    private const val DEFAULT_REGION = "United States"
    private const val DEFAULT_NAME = ""
    private const val PREF_SWITCH_STATE = "switch_state"
    private const val DEFAULT_SWITCH_STATE = false

    // Map region -> currency symbol
    private val regionToCurrencyMap = mapOf(
        "Australia" to "$",
        "Belgium" to "€",
        "Canada" to "$",
        "Czech Republic" to "Kč",
        "Denmark" to "kr",
        "Finland" to "€",
        "France" to "€",
        "Germany" to "€",
        "Hungary" to "Ft",
        "Ireland" to "€",
        "Italy" to "€",
        "Netherlands" to "€",
        "New Zealand" to "$",
        "Norway" to "kr",
        "Portugal" to "€",
        "Romania" to "lei",
        "Saudi Arabia" to "﷼",
        "Slovakia" to "€",
        "Spain" to "€",
        "Sweden" to "kr",
        "United Kingdom" to "£",
        "United States" to "$"
    )

    // Map region -> country code
    private val regionToCodeMap = mapOf(
        "Australia" to "au",
        "Austria" to "at",
        "Belgium" to "be",
        "Canada" to "ca",
        "Czech Republic" to "cz",
        "Denmark" to "dk",
        "Finland" to "fi",
        "France" to "fr",
        "Germany" to "de",
        "Hungary" to "hu",
        "Ireland" to "ie",
        "Italy" to "it",
        "Netherlands" to "nl",
        "New Zealand" to "nz",
        "Norway" to "no",
        "Portugal" to "pt",
        "Romania" to "ro",
        "Saudi Arabia" to "sa",
        "Slovakia" to "sk",
        "Spain" to "es",
        "Sweden" to "se",
        "United Kingdom" to "uk",
        "United States" to "us"
    )

    // Region Management
    fun saveRegion(context: Context, region: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PREF_REGION_KEY, region)
            .apply()
    }

    fun getSavedRegion(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_REGION_KEY, DEFAULT_REGION) ?: DEFAULT_REGION
    }

    // User Management
    fun saveName(context: Context, name: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PREF_NAME_KEY, name)
            .apply()
    }

    fun saveSwitchState(context: Context, isChecked: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(PREF_SWITCH_STATE, isChecked)
            .apply()
    }

    fun getSavedName(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_NAME_KEY, DEFAULT_NAME) ?: DEFAULT_NAME
    }

    // Currency Retreival
    fun getCurrencySymbol(context: Context): String {
        val savedRegion = getSavedRegion(context)
        return regionToCurrencyMap[savedRegion] ?: "$"
    }

    fun getRegionCode(context: Context): String {
        val savedRegion = getSavedRegion(context)
        return regionToCodeMap[savedRegion] ?: "us"
    }

    fun getSavedSwitchState(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_SWITCH_STATE, DEFAULT_SWITCH_STATE) ?: DEFAULT_SWITCH_STATE
    }

    // Price Formatting
    fun formatPrice(context: Context, price: String): String {
        val currencySymbol = getCurrencySymbol(context)
        return "$currencySymbol$price"
    }

    fun getDisplayPrice(context: Context, originalPrice: String, customPrice: String?): String {
        val currencySymbol = getCurrencySymbol(context)
        val useCustom = getSavedSwitchState(context) && !customPrice.isNullOrBlank()

        return if (useCustom) {
            "${currencySymbol}${customPrice} (${currencySymbol}${originalPrice})"
        }
        else {
            "${currencySymbol}${originalPrice}"
        }
    }

    fun getDisplayPriceForList(context: Context, originalPrice: String, customPrice: String?): String {
        val currencySymbol = getCurrencySymbol(context)
        val useCustom = getSavedSwitchState(context) && !customPrice.isNullOrBlank()

        return if (useCustom && !customPrice.isNullOrEmpty()) {
            "${currencySymbol}${customPrice}"
        }
        else {
            "${currencySymbol}${originalPrice}"
        }
    }

    fun getTotalPrice(context: Context, originalPrice: String, customPrice: String?): Double {
        val useCustom = getSavedSwitchState(context) && !customPrice.isNullOrBlank()
        val priceStr = if (useCustom) customPrice else originalPrice
        return priceStr?.replace(Regex("[^\\d.]"), "")?.toDoubleOrNull() ?: 0.0
    }

}
