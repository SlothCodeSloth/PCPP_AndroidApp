package com.example.pcpartpicker

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Represents a bundle and its associated components.
 *
 * This is used for many-to-many relationships between [BundleEntity] and [ComponentEntity]
 * via [BundleComponentCrossRef].
 *
 * @property bundle The [BundleEntity] object representing the bundle.
 * @property components List of [ComponentEntity]s associated with this bundle.
 */
data class BundleWithComponents(
    @Embedded val bundle: BundleEntity,
    @Relation(
        parentColumn = "bundleId",
        entityColumn = "url",
        associateBy = Junction(
            BundleComponentCrossRef::class,
            parentColumn = "bundleId",
            entityColumn =  "url"
        )
    )
    val components: List<ComponentEntity>
)
