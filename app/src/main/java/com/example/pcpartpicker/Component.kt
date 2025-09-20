package com.example.pcpartpicker

/**
 * Represents different component related data models for the app.
 */
class Component {
    /**
     * Represents a simple part/component with basic info.
     *
     * @property name Name of the part/component.
     * @property url URL to the part details.
     * @property price The listed price as a string.
     * @property image Optional image URL.
     * @property customPrice Optional custom price set by the user.
     */
    data class Part(
        val name: String,
        val url: String,
        val price: String,
        val image: String?,
        val customPrice: String? = null
    )

    data class Product(
        val name: String,
        val specs: Map<String, String>,
        val priceList: List<Vendor>,
        val image: String?,
        val rating: Rating?
    )

    data class Spec(
        val key: String,
        val value: String
    )

    data class Price(
        val seller: String,
        val value: Double
    )

}