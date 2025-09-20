package com.example.pcpartpicker

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * ViewModel for handling part search and retrieval logic.
 *
 * Responsibilities:
 * - Manages the state of search results, new parts, loading, and errors.
 * - Handles paging logic (current page, total pages).
 * - Exposes LiveData for UI to observe.
 * - Preserves search state across configuration changes (like theme switches).
 *
 * @param api Retrofit API client for fetching data from the backend.
 */
class PartViewModel(private val api: PyPartPickerApi) : ViewModel() {

    // LiveData to hold the list of parts
    private val _parts = MutableLiveData<List<Component.Part>>()
    val parts: LiveData<List<Component.Part>> get() = _parts

    // LiveData to emit newly loaded components
    private val _newParts = MutableLiveData<List<Component.Part>>()
    val newParts: LiveData<List<Component.Part>> get() = _newParts

    // LiveData to handle loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData to handle errors
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Track query and current page
    private var currentQuery: String = ""
    private var currentPage: Int = 1
    private var totalPages: Int = 1
    private val pageSize: Int = 5
    private var currentProductType: String? = null

    // Add flag to track if we have existing results
    private var hasExistingResults: Boolean = false

    // Start a new search
    fun startSearch(query: String, productType: String?, context: Context) {
        // Only trigger new search if query/product type actually changed
        if (query != currentQuery || productType != currentProductType) {
            currentQuery = query
            currentProductType = productType
            currentPage = 1
            totalPages = 1
            hasExistingResults = false
            _parts.value = emptyList()
            loadPage(context)
        } else if (!hasExistingResults) {
            // If same query but no existing results (like after theme change), load first page
            loadPage(context)
        }
    }

    // Load in new pages
    fun loadPage(context: Context) {
        if (_isLoading.value == true || currentPage > totalPages) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val regionCode = SettingsDataManager.getRegionCode(context)
                val response =  if (currentProductType != null) {
                    api.getPartsByCategory(
                        product_type = currentProductType!!,
                        limit = pageSize,
                        page = currentPage,
                        region = regionCode
                    )
                }
                else {
                    api.searchParts(
                        query = currentQuery,
                        limit = pageSize,
                        page = currentPage,
                        region = regionCode
                    )
                }

                totalPages = response.total_pages
                val newParts = response.results.map { part ->
                    Component.Part(
                        name = part.name,
                        url = part.url,
                        price = part.price?.toString() ?: "N/A",
                        image = part.image
                    )
                }

                val currentList = _parts.value?.toMutableList() ?: mutableListOf()
                currentList.addAll(newParts)
                _parts.value = currentList
                _newParts.value = newParts
                currentPage++
                hasExistingResults = true
            }
            catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            }
            finally {
                _isLoading.value = false
            }
        }
    }

    // Method to check if we have current search results
    fun hasCurrentResults(): Boolean {
        return hasExistingResults && !_parts.value.isNullOrEmpty()
    }

    // Clear all search data
    fun clearSearch() {
        currentQuery = ""
        currentProductType = null
        currentPage = 1
        totalPages = 1
        hasExistingResults = false
        _parts.value = emptyList()
    }

    // Fetch product details
    suspend fun fetchProduct(url: String): ProductResponse {
        return api.fetchProduct(url)
    }

    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}