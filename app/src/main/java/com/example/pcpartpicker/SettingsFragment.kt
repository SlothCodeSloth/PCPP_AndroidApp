package com.example.pcpartpicker

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Settings screen fragment.
 * - Lets users change name, region, theme, and toggle custom price display.
 */
class SettingsFragment : Fragment() {

    // Available theme options (drawable circles). TO DO - ADD MORE
    private val themeColors = listOf(
        R.drawable.theme_circle1,
        R.drawable.theme_circle2
    )

    private var selectedThemeIndex = 0
    private var currentRegion: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val themeContainer = view.findViewById<LinearLayout>(R.id.themeContainer)
        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val regionSpinner: Spinner = view.findViewById(R.id.regionSpinner)
        val regions = listOf("Australia", "Austria", "Belgium", "Canada", "Czech Republic",
            "Denmark", "Finland", "France", "Germany", "Hungary", "Ireland", "Italy", "Netherlands",
            "New Zealand", "Norway", "Portugal", "Romania", "Saudi Arabia", "Slovakia", "Spain",
            "Sweden","United Kingdom", "United States")
        val switch: SwitchCompat = view.findViewById(R.id.settingsSwitch)

        // Region spinner setup
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, regions)
        regionSpinner.adapter = adapter

        // Load Data
        nameEditText.setText(SettingsDataManager.getSavedName(requireContext()))
        val savedRegion = SettingsDataManager.getSavedRegion(requireContext())
        val switchState = SettingsDataManager.getSavedSwitchState(requireContext())
        currentRegion = savedRegion
        switch.isChecked = switchState
        if (switch.isChecked) {
            switch.setText("Enabled      ")
        }

        // Select saved region in spinner
        val savedRegionPosition = regions.indexOf(savedRegion)
        if (savedRegionPosition != -1) {
            regionSpinner.setSelection(savedRegionPosition)
        }

        // Save region when changed
        regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedRegion = regions[position]
                if (selectedRegion != currentRegion) {
                    SettingsDataManager.saveRegion(requireContext(), selectedRegion)
                    Toast.makeText(requireContext(), "Region set to $selectedRegion", Toast.LENGTH_SHORT).show()
                    currentRegion = selectedRegion
                    ThemeManager.recreateActivity(requireActivity())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        // Save name when user leaves the text field
        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val name = nameEditText.text.toString()
                SettingsDataManager.saveName(requireContext(), name)
                Toast.makeText(requireContext(), "Name Saved", Toast.LENGTH_SHORT).show()
            }
        }

        // Theme selection buttons
        themeColors.forEachIndexed { index, drawableRes ->
            val imageView = ImageView(requireContext()).apply {
                setImageResource(drawableRes)
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    marginEnd = 16
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(16, 16, 16, 16)

                setOnClickListener {
                    if (selectedThemeIndex != index) {
                        selectedThemeIndex = index
                        ThemeManager.saveThemeIndex(requireContext(), index)
                        highlightSelectedTheme(themeContainer, index)
                        Toast.makeText(requireContext(), "Theme ${index + 1} selected", Toast.LENGTH_SHORT).show()

                        // Recreate activity
                        ThemeManager.recreateActivity(requireActivity())
                    }
                }
            }
            themeContainer.addView(imageView)
        }
        // Highlight saved theme
        selectedThemeIndex = ThemeManager.getSavedThemeIndex(requireContext())
        highlightSelectedTheme(themeContainer, selectedThemeIndex)

        // Handle toggle switch
        switch.setOnCheckedChangeListener{_, isChecked ->
            if (isChecked) {
                switch.setText("Enabled      ")
                SettingsDataManager.saveSwitchState(requireContext(), true)
            }
            else {
                switch.setText("Disabled     ")
                SettingsDataManager.saveSwitchState(requireContext(), false)
            }

        }

        return view;
    }

    // Highlights the currently selected theme
    private fun highlightSelectedTheme(container: LinearLayout, selectedTheme: Int) {
        for (i in 0 until container.childCount) {
            val imageView = container.getChildAt(i) as ImageView
            imageView.scaleX = if (i == selectedTheme) 1.15f else 1f
            imageView.scaleY = if (i == selectedTheme) 1.15f else 1f
            imageView.alpha = if (i == selectedTheme) 1f else 0.6f
        }
    }
}