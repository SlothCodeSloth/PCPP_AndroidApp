package com.example.pcpartpicker

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a single PC component.
 * Stored in the "components" table.
 */
@Entity(tableName = "components")
data class ComponentEntity(
    @PrimaryKey val url: String, // Unique identifier (Component URL)
    val name: String, // Component Name
    val price: String, // Component Price
    val image: String?, // Component Image URL (optional)

    // Custom values (user overrides)
    val customVendor: String? = null,
    val customPrice: String? = null,
    val customUrl: String? = null
)
