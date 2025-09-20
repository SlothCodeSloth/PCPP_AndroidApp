package com.example.pcpartpicker

import androidx.room.Entity

/**
 * Cross reference entity to represent the many-to-many relationship
 * between [ListEntity] and [ComponentEntity].
 *
 * Each entry links a list (via [listId]) to a component (via [componentUrl]).
 */
@Entity(primaryKeys = ["listId", "componentUrl"])
data class ListComponentCrossRef(
    val listId: Int,
    val componentUrl: String
)
