package com.example.pcpartpicker

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

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
    fun toListItems(): List<ListItem> {
        return components.map { ListItem.ComponentItem(it) } + bundles.map { ListItem.BundleItem(it) }
    }
}
