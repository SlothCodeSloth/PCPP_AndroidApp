package com.example.pcpartpicker

import androidx.room.Entity

/**
 * Cross-reference entity representing a many-to-many relationship between
 * [BundleEntity] and [ComponentEntity].
 *
 * Each row represents the association between a bundle and a component.
 *
 * @property bundleId The ID of the bundle.
 * @property url The URL of the component.
 *
 * @see ComponentEntity
 * @see BundleEntity
 */
@Entity(primaryKeys = ["bundleId", "url"])
data class BundleComponentCrossRef(
    val bundleId: Int,
    val url: String
)
