package com.example.pcpartpicker

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Data class representing a [ListEntity] and all of its associated items.
 *
 * Includes:
 * - Components linked through the many-to-many [ListComponentCrossRef].
 * - Bundles that directly reference the list via a foreign key.
 *
 * Provides [toListItems] to display both in a unified list.
 */
data class ListWithItems(
    @Embedded val list: ListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "url",
        associateBy = Junction(
            ListComponentCrossRef::class,
            parentColumn = "listId",
            entityColumn = "componentUrl"
            )
    )
    val components: List<ComponentEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "listId"
    )
    val bundles: List<BundleEntity>
) {
    /**
     * Converts components and bundles into a single [ListItem] list.
     */
    fun toListItems(): List<ListItem> {
        return components.map { ListItem.ComponentItem(it) } + bundles.map { ListItem.BundleItem(it) }
    }
}
