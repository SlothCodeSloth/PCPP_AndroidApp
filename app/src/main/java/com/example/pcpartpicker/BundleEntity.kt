package com.example.pcpartpicker

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a bundle of components created by the user.
 *
 * @property bundleId Auto-generated ID for the bundle.
 * @property vendor The vendor associated with the bundle.
 * @property name The name of the bundle.
 * @property price Total price of the bundle as a formatted string.
 * @property url The URL linking to the bundle details or vendor page.
 * @property image Optional image URL representing the bundle.
 * @property listId The ID of the list this bundle belongs to.
 */
@Entity(tableName = "bundles")
data class BundleEntity (
    @PrimaryKey(autoGenerate = true) val bundleId: Int = 0,
    val vendor: String,
    val name: String,
    val price: String,
    val url: String,
    val image: String,
    val listId: Int
)