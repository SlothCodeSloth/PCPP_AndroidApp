package com.example.pcpartpicker

class Component {
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