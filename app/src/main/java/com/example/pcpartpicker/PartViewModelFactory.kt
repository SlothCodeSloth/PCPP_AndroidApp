package com.example.pcpartpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating [PartViewModel] with a [PyPartPickerApi] dependency.
 *
 * @param api API client to inject into the ViewModel.
 */
class PartViewModelFactory (private val api: PyPartPickerApi) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create (modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PartViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}