package com.example.pcpartpicker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for communicating with the backend PartPicker API.
 */
interface PyPartPickerApi {

    /**
     * Search for parts by a query (String)
     */
    @GET("/pcpp/search")
    suspend fun searchParts(
        @Query("query") query: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int = 1,
        @Query("region") region: String = "us",
    ): SearchResponse

    /**
     * Fetch parts by specific category/product type
     */
    @GET("/pcpp/parts_by_type")
    suspend fun getPartsByCategory(
        @Query("product_type") product_type: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int = 1,
        @Query("region") region: String = "us"
    ): SearchResponse

    /**
     * Fetch detailed product information by its [url]
     */
    @GET("/pcpp/product")
    suspend fun fetchProduct(@Query("url") url: String): ProductResponse
}

/**
 * Response for search endpoints
 */
data class SearchResponse(
    val results: List<Component.Part>,
    val page: Int,
    val total_pages: Int
)

/**
 * Response for product details
 */
data class ProductResponse(
    val name: String,
    val specs: Map<String, String>,
    val price_list: List<Vendor>,
    val image: String?,
    val rating: Rating?
)

/**
 * Represents a vendor
 */
data class Vendor(
    val seller: String,
    val value: Double,
    val in_stock: Boolean
)

/**
 * Represents a rating
 */
data class Rating(
    val average: Double?,
    val count: Int?
)