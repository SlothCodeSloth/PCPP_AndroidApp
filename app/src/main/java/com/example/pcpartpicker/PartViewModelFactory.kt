package com.example.pcpartpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PartViewModelFactory (private val api: PyPartPickerApi) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create (modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PartViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}