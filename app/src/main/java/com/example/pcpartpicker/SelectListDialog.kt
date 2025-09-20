package com.example.pcpartpicker

import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog


/**
 * A reusable dialog for selecting a list by name.
 * - Displays a list of names (passed in as `listNames`).
 * - Calls `onListSelected` with the chosen item when tapped.
 */
class SelectListDialog (
    context: Context,
    listNames: List<String>,
    private val title: String,
    private val onListSelected: (String) -> Unit
) {
    // Builder used to create the dialog UI
    private val builder = AlertDialog.Builder(context)
    init {
        builder.setTitle(title)
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, listNames)
        builder.setAdapter(adapter) { _, which ->
            onListSelected(listNames[which])
        }
        builder.setNegativeButton("Cancel", null)
    }

    // Show the dialog
    fun show() {
        builder.show()
    }
}